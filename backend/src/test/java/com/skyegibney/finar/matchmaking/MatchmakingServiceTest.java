package com.skyegibney.finar.matchmaking;

import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.matchmaking.events.LobbyDisbandedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerKickedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerLeftEvent;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.websockets.ConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakingServiceTest {

    @Mock
    Random random;

    @Mock
    GameService gameService;

    @Mock
    ConnectionService connectionService;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        // Default: no active game for any player.
        when(gameService.getGameIdByPlayer(anyString())).thenReturn(Optional.empty());
    }

    // -------------------------------------------------------------------------
    // queuePlayer
    // -------------------------------------------------------------------------

    @Test
    void queuePlayer_addsPlayerToQueue() {
        matchmakingService.queuePlayer("alice");

        assertThat(matchmakingService.isPlayerInQueue("alice")).isTrue();
    }

    @Test
    void queuePlayer_duplicatePlayer_notAddedTwice() {
        matchmakingService.queuePlayer("alice");
        matchmakingService.queuePlayer("alice");

        // matchPlayers drains the queue; if alice appears twice matchPlayers would
        // return her as both players in the same match.
        assertThat(matchmakingService.matchPlayers()).isEmpty();
    }

    @Test
    void queuePlayer_playerAlreadyInGame_sendsMatchFoundNotSelf() {
        long existingGameId = 42L;
        when(gameService.getGameIdByPlayer("alice")).thenReturn(Optional.of(existingGameId));

        matchmakingService.queuePlayer("alice");

        assertThat(matchmakingService.isPlayerInQueue("alice")).isFalse();
        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("alice"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("matchFound");
    }

    // -------------------------------------------------------------------------
    // removePlayerFromQueue
    // -------------------------------------------------------------------------

    @Test
    void removePlayerFromQueue_removesPlayer() {
        matchmakingService.queuePlayer("alice");
        matchmakingService.removePlayerFromQueue("alice");

        assertThat(matchmakingService.isPlayerInQueue("alice")).isFalse();
    }

    @Test
    void removePlayerFromQueue_sendsAck() {
        matchmakingService.queuePlayer("alice");
        // Reset so we only capture the remove-ack, not the queue-ack.
        clearInvocations(connectionService);

        matchmakingService.removePlayerFromQueue("alice");

        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("alice"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("ack");
    }

    // -------------------------------------------------------------------------
    // matchPlayers
    // -------------------------------------------------------------------------

    @Test
    void matchPlayers_twoActivePlayers_returnsBoth() {
        when(connectionService.hasActiveConnection("alice")).thenReturn(true);
        when(connectionService.hasActiveConnection("bob")).thenReturn(true);

        matchmakingService.queuePlayer("alice");
        matchmakingService.queuePlayer("bob");

        assertThat(matchmakingService.matchPlayers()).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void matchPlayers_onePlayerInactive_returnsEmpty_andRequeuesActive() {
        when(connectionService.hasActiveConnection("alice")).thenReturn(true);
        when(connectionService.hasActiveConnection("bob")).thenReturn(false);

        matchmakingService.queuePlayer("alice");
        matchmakingService.queuePlayer("bob");

        assertThat(matchmakingService.matchPlayers()).isEmpty();
        // The active player should have been re-queued.
        assertThat(matchmakingService.isPlayerInQueue("alice")).isTrue();
    }

    @Test
    void matchPlayers_onlyOnePlayer_returnsEmpty_andRequeues() {
        when(connectionService.hasActiveConnection("alice")).thenReturn(true);

        matchmakingService.queuePlayer("alice");

        assertThat(matchmakingService.matchPlayers()).isEmpty();
        assertThat(matchmakingService.isPlayerInQueue("alice")).isTrue();
    }

    // -------------------------------------------------------------------------
    // Lobby – createLobby / playerJoinLobby
    // -------------------------------------------------------------------------

    @Test
    void createLobby_returnsLobbyId_andOwnerIsInLobby() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(1234);

        int lobbyId = matchmakingService.createLobby("alice");

        assertThat(lobbyId).isEqualTo(1234);
        assertThat(matchmakingService.getPlayersByLobbyId(1234)).contains("alice");
    }

    @Test
    void createLobby_playerAlreadyInGame_sendsMatchFound_returnsMinusOne() {
        when(gameService.getGameIdByPlayer("alice")).thenReturn(Optional.of(99L));

        int result = matchmakingService.createLobby("alice");

        assertThat(result).isEqualTo(-1);
        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("alice"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("matchFound");
    }

    @Test
    void playerJoinLobby_secondPlayer_isAddedToLobby() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(5678);
        matchmakingService.createLobby("alice");

        matchmakingService.playerJoinLobby("bob", 5678);

        assertThat(matchmakingService.getPlayersByLobbyId(5678)).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void playerJoinLobby_nonExistentLobby_sendsLobbyNotFound() {
        matchmakingService.playerJoinLobby("bob", 9999);

        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("bob"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("lobbyNotFound");
    }

    @Test
    void playerJoinLobby_fullLobby_sendsLobbyFull() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(1111);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 1111);
        clearInvocations(connectionService);

        matchmakingService.playerJoinLobby("carol", 1111);

        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("carol"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("lobbyFull");
    }

    @Test
    void playerJoinLobby_existingPlayer_sendsLobbyInfo() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(2222);
        matchmakingService.createLobby("alice");
        clearInvocations(connectionService);

        // alice re-joins her own lobby (e.g. after a reconnect).
        matchmakingService.playerJoinLobby("alice", 2222);

        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService).sendMessage(eq("alice"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("lobbyInfo");
    }

    // -------------------------------------------------------------------------
    // kickPlayer
    // -------------------------------------------------------------------------

    @Test
    void kickPlayer_byOwner_publishesPlayerKickedEvent() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(3333);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 3333);

        matchmakingService.kickPlayer("alice", 3333, "bob");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(PlayerKickedEvent.class);
        assertThat(((PlayerKickedEvent) captor.getValue()).player()).isEqualTo("bob");
    }

    @Test
    void kickPlayer_byNonOwner_isIgnored() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(4444);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 4444);

        matchmakingService.kickPlayer("bob", 4444, "alice");

        verifyNoInteractions(publisher);
    }

    @Test
    void kickPlayer_ownerCannotKickSelf() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(5555);
        matchmakingService.createLobby("alice");

        matchmakingService.kickPlayer("alice", 5555, "alice");

        verifyNoInteractions(publisher);
    }

    // -------------------------------------------------------------------------
    // togglePlayerReady / startGame
    // -------------------------------------------------------------------------

    @Test
    void togglePlayerReady_broadcastsReadyStatusToAllPlayers() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(6666);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 6666);
        clearInvocations(connectionService);

        matchmakingService.togglePlayerReady("alice", 6666);

        // Both alice and bob should receive a playerReadyStatus message.
        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService, times(2)).sendMessage(anyString(), captor.capture());
        assertThat(captor.getAllValues()).allMatch(m -> m.type().equals("playerReadyStatus"));
    }

    @Test
    void startGame_requiresBothPlayersReady() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(7777);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 7777);

        // Only alice is ready – game should not start.
        matchmakingService.togglePlayerReady("alice", 7777);
        matchmakingService.startGame("alice", 7777);

        verify(gameService, never()).createGame(anyString(), anyString());
    }

    @Test
    void startGame_bothPlayersReady_createsGameAndNotifiesPlayers() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(8888);
        when(gameService.createGame(anyString(), anyString())).thenReturn(100L);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 8888);
        matchmakingService.togglePlayerReady("alice", 8888);
        matchmakingService.togglePlayerReady("bob", 8888);
        clearInvocations(connectionService);

        matchmakingService.startGame("alice", 8888);

        verify(gameService).createGame(anyString(), anyString());
        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(connectionService, times(2)).sendMessage(anyString(), captor.capture());
        assertThat(captor.getAllValues()).allMatch(m -> m.type().equals("matchFound"));
    }

    @Test
    void startGame_byNonOwner_isIgnored() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(9999);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 9999);
        matchmakingService.togglePlayerReady("alice", 9999);
        matchmakingService.togglePlayerReady("bob", 9999);

        matchmakingService.startGame("bob", 9999); // bob is not the owner

        verify(gameService, never()).createGame(anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // Owner / non-owner leaving
    // -------------------------------------------------------------------------

    @Test
    void queuePlayer_ownerLeavingViaRequeue_disbands_lobby() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(1010);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 1010);

        // Queuing alice again removes her from lobbies (she's the owner → disband).
        matchmakingService.queuePlayer("alice");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(LobbyDisbandedEvent.class);
    }

    @Test
    void queuePlayer_nonOwnerLeavingViaRequeue_publishesPlayerLeftEvent() {
        when(random.nextInt(Integer.MAX_VALUE)).thenReturn(2020);
        matchmakingService.createLobby("alice");
        matchmakingService.playerJoinLobby("bob", 2020);

        // bob (non-owner) re-queues, removing himself from the lobby.
        matchmakingService.queuePlayer("bob");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(PlayerLeftEvent.class);
        assertThat(((PlayerLeftEvent) captor.getValue()).player()).isEqualTo("bob");
    }
}
