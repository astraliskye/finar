package com.skyegibney.finar.game;

import com.skyegibney.finar.game.events.FinarGameOverEvent;
import com.skyegibney.finar.game.events.GameOverEvent;
import com.skyegibney.finar.game.events.MoveMadeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    GameResultRepository gameResultRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    GameService gameService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Reflectively retrieves the live Game object from the service's private activeGames map.
     */
    private Game getGame(long gameId) throws Exception {
        Field field = GameService.class.getDeclaredField("activeGames");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Game> activeGames = (Map<Long, Game>) field.get(gameService);
        return activeGames.get(gameId);
    }

    // -------------------------------------------------------------------------
    // createGame / getGameIdByPlayer / getPlayersByGameId
    // -------------------------------------------------------------------------

    @Test
    void createGame_storesGame_andReturnsUniqueId() {
        long id1 = gameService.createGame("alice", "bob");
        long id2 = gameService.createGame("carol", "dave");

        assertThat(gameService.getGameIdByPlayer("alice")).contains(id1);
        assertThat(gameService.getGameIdByPlayer("carol")).contains(id2);
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void getGameIdByPlayer_playerNotInAnyGame_returnsEmpty() {
        assertThat(gameService.getGameIdByPlayer("nobody")).isEmpty();
    }

    @Test
    void getPlayersByGameId_unknownId_returnsEmptyList() {
        assertThat(gameService.getPlayersByGameId(999L)).isEmpty();
    }

    @Test
    void getPlayersByGameId_knownId_returnsBothPlayers() {
        long gameId = gameService.createGame("alice", "bob");

        assertThat(gameService.getPlayersByGameId(gameId)).containsExactlyInAnyOrder("alice", "bob");
    }

    // -------------------------------------------------------------------------
    // makeMove – guard conditions
    // -------------------------------------------------------------------------

    @Test
    void makeMove_unknownGameId_isIgnored() {
        gameService.makeMove(999L, "alice", (byte) 0);

        verifyNoInteractions(publisher);
    }

    @Test
    void makeMove_wrongPlayer_isIgnored() {
        long gameId = gameService.createGame("alice", "bob");
        var players = gameService.getPlayersByGameId(gameId);
        // players.get(0) is the current player after shufflePlayers() in createGame.
        String notCurrentPlayer = players.get(1);

        gameService.makeMove(gameId, notCurrentPlayer, (byte) 0);

        verifyNoInteractions(publisher);
    }

    @Test
    void makeMove_negativeIndex_isIgnored() {
        long gameId = gameService.createGame("alice", "bob");
        var players = gameService.getPlayersByGameId(gameId);
        String currentPlayer = players.get(0);

        gameService.makeMove(gameId, currentPlayer, (byte) -1);

        verifyNoInteractions(publisher);
    }

    // -------------------------------------------------------------------------
    // makeMove – happy path
    // -------------------------------------------------------------------------

    @Test
    void makeMove_validMove_publishesMoveMadeEvent() {
        long gameId = gameService.createGame("alice", "bob");
        var players = gameService.getPlayersByGameId(gameId);
        String currentPlayer = players.get(0);

        gameService.makeMove(gameId, currentPlayer, (byte) 0);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(e -> e instanceof MoveMadeEvent evt && evt.gameId() == gameId);
    }

    @Test
    void makeMove_fillsEntireBoard_resultsInDraw() throws Exception {
        long gameId = gameService.createGame("alice", "bob");
        var players = gameService.getPlayersByGameId(gameId);

        // Stub repository so cleanupGame doesn't blow up.
        when(gameResultRepository.save(any())).thenReturn(null);

        // Fill all 100 cells with moves that do not produce a five-in-a-row.
        // We lay moves in row-major order but alternate players naturally.
        // To avoid accidentally triggering a finar we set the board directly via
        // reflection and then make the last move through the service.
        Game game = getGame(gameId);
        int[] board = game.getBoard();
        // Fill 99 cells manually (alternating 1 and 2 so no run of five for either).
        // Pattern: two player-1, two player-2, repeat – guarantees no five-in-a-row.
        int player1Val = 1, player2Val = 2;
        for (int i = 0; i < 99; i++) {
            board[i] = ((i / 2) % 2 == 0) ? player1Val : player2Val;
            game.getMoves().add(i);
        }
        // Advance currentMove to reflect 99 moves (odd → player 2's turn next).
        game.setCurrentMove(99 % 2);

        // Ensure the last empty cell won't complete a finar.
        // Cell 99 is already empty (value 0). After our move it becomes the 100th move.
        String currentTurn = game.getCurrentTurn();
        gameService.makeMove(gameId, currentTurn, (byte) 99);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(e -> e instanceof GameOverEvent evt && evt.result().equals("DRAW"));
    }

    @Test
    void makeMove_causesFinar_publishesFinarGameOverEvent() throws Exception {
        long gameId = gameService.createGame("alice", "bob");
        when(gameResultRepository.save(any())).thenReturn(null);

        Game game = getGame(gameId);

        // Manually set up a board so the current player has four in a row at cells 0-3.
        // The current player is players.get(0) with value 1 (first to move).
        // We place pieces via makeMove to keep currentMove in sync.
        var players = gameService.getPlayersByGameId(gameId);
        String p1 = players.get(0);
        String p2 = players.get(1);

        // Interleave moves: p1 builds cells 0,1,2,3 while p2 plays elsewhere.
        gameService.makeMove(gameId, p1, (byte) 0);
        gameService.makeMove(gameId, p2, (byte) 50);
        gameService.makeMove(gameId, p1, (byte) 1);
        gameService.makeMove(gameId, p2, (byte) 51);
        gameService.makeMove(gameId, p1, (byte) 2);
        gameService.makeMove(gameId, p2, (byte) 52);
        gameService.makeMove(gameId, p1, (byte) 3);
        gameService.makeMove(gameId, p2, (byte) 53);

        // p1 plays cell 4 – completing five in a row (0-4).
        gameService.makeMove(gameId, p1, (byte) 4);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(e -> e instanceof FinarGameOverEvent evt
                        && evt.result().equals("FINAR")
                        && evt.winner().equals(p1));
    }

    // -------------------------------------------------------------------------
    // quitPlayer
    // -------------------------------------------------------------------------

    @Test
    void quitPlayer_lessThanTwoMoves_resultsInAbort() {
        long gameId = gameService.createGame("alice", "bob");
        when(gameResultRepository.save(any())).thenReturn(null);

        // No moves have been made yet (moves.size() == 0 < 2).
        var players = gameService.getPlayersByGameId(gameId);
        gameService.quitPlayer(gameId, players.get(0));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue())
                .isInstanceOf(GameOverEvent.class)
                .matches(e -> ((GameOverEvent) e).result().equals("ABORT"));
    }

    @Test
    void quitPlayer_twoOrMoreMoves_resultsInAbandonWithOtherPlayerAsWinner() {
        long gameId = gameService.createGame("alice", "bob");
        when(gameResultRepository.save(any())).thenReturn(null);

        var players = gameService.getPlayersByGameId(gameId);
        String p1 = players.get(0);
        String p2 = players.get(1);

        // Make two moves so moves.size() >= 2.
        gameService.makeMove(gameId, p1, (byte) 0);
        gameService.makeMove(gameId, p2, (byte) 1);

        // p1 quits – p2 should be the winner.
        gameService.quitPlayer(gameId, p1);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(e -> e instanceof GameOverEvent evt
                        && evt.result().equals("ABANDON")
                        && evt.winner().equals(p2));
    }

    @Test
    void quitPlayer_unknownGameId_isIgnored() {
        // Should not throw; publisher must not be called for the quit path.
        gameService.quitPlayer(999L, "alice");
        verifyNoInteractions(publisher);
    }

    @Test
    void quitPlayer_playerNotInGame_isIgnored() {
        long gameId = gameService.createGame("alice", "bob");
        gameService.quitPlayer(gameId, "stranger");

        verifyNoInteractions(publisher);
    }

    // -------------------------------------------------------------------------
    // Timeout (via makeMove)
    // -------------------------------------------------------------------------

    @Test
    void makeMove_playerTimeExpired_cleanupWithTimeout() throws Exception {
        long gameId = gameService.createGame("alice", "bob");
        when(gameResultRepository.save(any())).thenReturn(null);

        var players = gameService.getPlayersByGameId(gameId);
        String p1 = players.get(0);
        String p2 = players.get(1);

        // Make two moves so both clocks are active.
        gameService.makeMove(gameId, p1, (byte) 0);
        gameService.makeMove(gameId, p2, (byte) 1);

        // Now it's p1's turn. Force the clock to have expired.
        Game game = getGame(gameId);
        game.setPlayer1Time(1);
        game.setLastTimeUpdate(System.currentTimeMillis() - 10_000); // 10 s in the past

        gameService.makeMove(gameId, p1, (byte) 2);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(e -> e instanceof GameOverEvent evt && evt.result().equals("TIMEOUT"));
    }
}
