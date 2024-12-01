package com.skyegibney.finar.services;

import com.skyegibney.finar.core.game.ResultType;
import com.skyegibney.finar.dtos.messages.server.*;
import com.skyegibney.finar.errors.InvalidMoveException;
import com.skyegibney.finar.errors.OutOfTurnException;
import com.skyegibney.finar.core.game.Game;
import com.skyegibney.finar.models.GameResult;
import com.skyegibney.finar.repositories.GameResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameService {
    private final GameResultRepository gameResultRepository;
    private final ConnectionService connectionService;
    Map<Integer, Game> activeGames = new HashMap<>();

    public GameService(GameResultRepository gameResultRepository,
                       ConnectionService connectionService) {
        this.gameResultRepository = gameResultRepository;
        this.connectionService = connectionService;
    }

    public long createGame(String player1, String player2) {
        var game = new Game(player1, player2);
        activeGames.put(game.getId(), game);
        game.shufflePlayers();
        return game.getId();
    }

    public Game getGameByPlayer(String player) {
        for (var game : activeGames.values()) {
            if (player.equals(game.getP1()) || player.equals(game.getP2())) {
                return game;
            }
        }

        return null;
    }

    public void cleanupGame(Game game,
                            ResultType resultType,
                            String winner) {
        activeGames.remove(game.getId());

        var response = new MessageResponse(
                "gameOver",
                new GameOver(
                        resultType.name(),
                        game.getP2()
                )
        );

        connectionService.sendMessage(
                game.getP1(),
                response
        );

        connectionService.sendMessage(
                game.getP2(),
                response
        );

        gameResultRepository.save(
                new GameResult(
                        game.getId(),
                        game.getP1(),
                        game.getP2(),
                        resultType,
                        winner
                )
        );

        connectionService.closeSession(game.getP1());
        connectionService.closeSession(game.getP2());
    }

    public void initialJoin(String player) {
        for (var game: activeGames.values()) {
            if (player.equals(game.getP1()) || player.equals(game.getP2())) {
                log.debug("{}", game.getId());
                connectionService.sendMessage(player,
                        new MessageResponse(
                                "initialJoin",
                                new InitialJoin(
                                        game.getId(),
                                        player,
                                        player.equals(game.getP1()) ? game.getP2() : game.getP1(),
                                        player.equals(game.getP1()) ? 0 : 1,
                                        game.getMoves().stream().map(Object::toString).collect(Collectors.joining(",")),
                                        new TimeControl(
                                                game.getPlayer1Time(),
                                                game.getPlayer2Time()
                                        )
                                )
                        ));
            } else {
                connectionService.sendMessage(player,
                        new MessageResponse(
                                "redirect",
                                "home"
                        ));
            }
        }
    }

    public void makeMove(int gameId, String player, byte n) {
        var game = activeGames.get(gameId);

        // Invalid game ID
        if (game == null) {
            log.debug("Invalid game ID {}", gameId);
            return;
        }

        // Player is not the current player
        if (!player.equals(game.getCurrentTurn())) {
            log.debug("Player attempting to make move out of turn. Player: {}, GameID: {}", player, game.getId());
            return;
        }

        var otherPlayer = player.equals(game.getP1()) ? game.getP2() : game.getP1();

        try {
            var timeout = checkTimeoutAndUpdate(game);

            if (timeout) {
                cleanupGame(game, ResultType.TIMEOUT, otherPlayer);
                return;
            }

            game.makeMove(player, n);

            var moveResponse = new MessageResponse(
                    "move",
                    new Move(
                    player,
                    n,
                    new TimeControl(
                            game.getPlayer1Time(),
                            game.getPlayer2Time()
                    )
            ));

            connectionService.sendMessage(
                    otherPlayer,
                    moveResponse
            );

            connectionService.sendMessage(
                    player,
                    moveResponse
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            log.debug("Invalid move in game {}, index {}", game.getId(), n);
        }

        checkGameOver(game);
    }

    public void handleFlag(int gameId, String player) {
        var game = activeGames.get(gameId);
        if (game == null) {
            return;
        }

        var timeout = checkTimeoutAndUpdate(game);

        if (timeout) {
            cleanupGame(game, ResultType.TIMEOUT, player);
        } else {
            connectionService.sendMessage(
                    player,
                    new MessageResponse(
                            "timeUpdate",
                            new TimeControl(
                                    game.getPlayer1Time(),
                                    game.getPlayer2Time()
                            )
                    )
            );
        }
    }

    private void checkGameOver(Game game) {
        game.checkFinar();
        if (game.isFinar()) {
            cleanupGame(game, ResultType.FINAR, game.getWinner());
        } else if (game.getMoves().size() == 100) {
            cleanupGame(game, ResultType.DRAW, "");
        }
    }

    public boolean checkTimeoutAndUpdate(Game game) {
        var currentTime = System.currentTimeMillis();
        var timeDiff = currentTime - game.getLastTimeUpdate();
        var currentPlayer = game.getCurrentTurn();

        if (currentPlayer.equals(game.getP1()) && !game.getMoves().isEmpty()) {
            var newTime = Math.max(game.getPlayer1Time() - timeDiff, 0);

            game.setPlayer1Time(newTime);

            if (newTime == 0) {
                return true;
            }
        } else if (game.getP2().equals(game.getCurrentTurn()) && game.getMoves().size() > 1) {
            var newTime = Math.max(game.getPlayer2Time() - timeDiff, 0);
            game.setPlayer2Time(newTime);
            game.setLastTimeUpdate(currentTime);

            if (newTime == 0) {
                return true;
            }
        }

        game.setLastTimeUpdate(currentTime);
        return false;
    }

    @Scheduled(fixedRate = 20 * 1000)
    public void timeCheck() {
        for (var game : activeGames.values()) {
            var timeout = checkTimeoutAndUpdate(game);

            if (timeout) {
                cleanupGame(game, ResultType.TIMEOUT, game.getCurrentTurn());
            }
        }
    }

    public void quitPlayer(int gameId, String player) {
        var game = activeGames.get(gameId);

        if (game == null) {
            return;
        }

        if (!player.equals(game.getP1()) && !player.equals(game.getP2())) {
            return;
        }

        if (player.equals(game.getP1()) && game.getCurrentMove() == 1
                || player.equals(game.getP2()) && game.getCurrentMove() <= 2) {
            cleanupGame(game, ResultType.ABORT, "");
        } else {
            var otherPlayer = player.equals(game.getP1()) ? game.getP2() : game.getP1();
            cleanupGame(game, ResultType.ABANDON, otherPlayer);
        }
    }
}
