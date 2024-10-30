package com.skyegibney.finar.services;

import com.skyegibney.finar.core.game.ResultType;
import com.skyegibney.finar.dtos.messages.server.GameOver;
import com.skyegibney.finar.dtos.messages.server.MessageResponse;
import com.skyegibney.finar.dtos.messages.server.Move;
import com.skyegibney.finar.dtos.messages.server.TimeControl;
import com.skyegibney.finar.errors.InvalidMoveException;
import com.skyegibney.finar.errors.OutOfTurnException;
import com.skyegibney.finar.core.game.Game;
import com.skyegibney.finar.models.GameResult;
import com.skyegibney.finar.repositories.GameResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GameService {
    private final GameResultRepository gameResultRepository;
    private final ConnectionService connectionService;
    Map<String, Game> activeGames = new HashMap<>();

    public GameService(GameResultRepository gameResultRepository, ConnectionService connectionService) {
        this.gameResultRepository = gameResultRepository;
        this.connectionService = connectionService;
    }

    public Game getGameByPlayer(String player) {
        return activeGames.get(player);
    }

    public long createGame(String player1, String player2) {
        var game = new Game(player1, player2);

        activeGames.put(player1, game);
        activeGames.put(player2, game);

        game.shufflePlayers();

        return game.getId();
    }

    public void makeMove(String player, byte n) {
        var game = getGameByPlayer(player);
        var otherPlayer = player.equals(game.getP1()) ? game.getP2() : game.getP1();

        if (checkTimeout(game)) {
            // checkTimeout will clean up the game
            return;
        }

        try {
            game.makeMove(player, n);

            var moveResponse = new Move(
                    player,
                    n,
                    new TimeControl(
                            game.getPlayer1Time().longValue(),
                            game.getPlayer2Time().longValue()
                    )
            );

            connectionService.sendMessage(
                    otherPlayer,
                    new MessageResponse(
                            "move",
                            moveResponse
                    )
            );

            connectionService.sendMessage(
                    player,
                    new MessageResponse(
                            "move",
                            moveResponse
                    )
            );
        } catch (InvalidMoveException e) {
            log.debug("Invalid move");
        } catch (OutOfTurnException e) {
            log.debug("Player {} out of turn. Currently {}'s turn", player, game.getCurrentTurn());
        }

        checkGameOver(game);
    }

    private void checkGameOver(Game game) {
        if (game.isFinar()) {
            activeGames.remove(game.getP1());
            activeGames.remove(game.getP2());


            var response = new MessageResponse(
                    "gameOver",
                    new GameOver(
                            "finar",
                            game.getWinner()
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
                            ResultType.FINAR,
                            game.getWinner()
                    )
            );

            connectionService.closeSession(game.getP1());
            connectionService.closeSession(game.getP2());
        } else if (game.getMoves().size() == 100) {
            activeGames.remove(game.getP1());
            activeGames.remove(game.getP2());

            var response = new MessageResponse(
                    "gameOver",
                    new GameOver(
                            "draw",
                            ""
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
                            ResultType.DRAW,
                            ""
                    )
            );

            connectionService.closeSession(game.getP1());
            connectionService.closeSession(game.getP2());
        }
    }

    private boolean checkTimeout(Game game) {
        var currentTime = System.currentTimeMillis();
        var timeDiff = currentTime - game.getLastTimeUpdate();
        var currentPlayer = game.getCurrentTurn();

        if (currentPlayer.equals(game.getP1()) && !game.getMoves().isEmpty()) {
            var newTime = game.getPlayer1Time().updateAndGet(time -> time - timeDiff);
            game.setLastTimeUpdate(currentTime);

            if (newTime < 0) {
                activeGames.remove(game.getP1());
                activeGames.remove(game.getP2());

                var response = new MessageResponse(
                        "gameOver",
                        new GameOver(
                                "timeout",
                                game.getP2()
                        )
                );

                connectionService.sendMessage(
                        game.getP2(),
                        response
                );

                connectionService.sendMessage(
                        currentPlayer,
                        response
                );

                gameResultRepository.save(
                        new GameResult(
                                game.getId(),
                                game.getP1(),
                                game.getP2(),
                                ResultType.EXPIRE,
                                game.getP2()
                        )
                );

                connectionService.closeSession(currentPlayer);
                connectionService.closeSession(game.getP2());
                return true;
            }
        } else if (game.getP2().equals(game.getCurrentTurn()) && game.getMoves().size() > 1) {
            var newTime = game.getPlayer2Time().updateAndGet(time -> time - timeDiff);
            game.setLastTimeUpdate(currentTime);

            if (newTime < 0) {
                activeGames.remove(game.getP1());
                activeGames.remove(game.getP2());

                var response = new MessageResponse(
                        "gameOver",
                        new GameOver(
                                "timeout",
                                game.getP1()
                        )
                );

                connectionService.sendMessage(
                        currentPlayer,
                        response
                );

                connectionService.sendMessage(
                        game.getP1(),
                        response
                );

                gameResultRepository.save(
                        new GameResult(
                                game.getId(),
                                game.getP1(),
                                game.getP2(),
                                ResultType.EXPIRE,
                                game.getP1()
                        )
                );

                connectionService.closeSession(currentPlayer);
                connectionService.closeSession(game.getP1());
                return true;
            }
        }

        return false;
    }

    @Scheduled(fixedRate = 1000)
    public void timeCheck() {
        var seenGames = new HashSet<Long>();

        for (var game : activeGames.values()) {
            if (seenGames.contains(game.getId())) {
                continue;
            } else {
                seenGames.add(game.getId());
            }

            checkTimeout(game);
        }
    }

    public void quitPlayer(String player) {
        var game = activeGames.get(player);

        if (game == null) {
            return;
        }

        activeGames.remove(game.getP1());
        activeGames.remove(game.getP2());

        if (player.equals(game.getP1()) && game.getCurrentMove() == 1
                || player.equals(game.getP2()) && game.getCurrentMove() <= 2) {
            // Abort game if player hasn't played a move yet
            var otherPlayer = player.equals(game.getP1()) ? game.getP2() : game.getP1();
            var response = new MessageResponse(
                    "gameOver",
                    new GameOver(
                            "abort",
                            ""
                    )
            );

            connectionService.sendMessage(
                    otherPlayer,
                    response
            );

            connectionService.sendMessage(
                    player,
                    response
            );

            connectionService.closeSession(player);
            connectionService.closeSession(otherPlayer);
        } else {
            // Abandon game (counts for loss)
            var otherPlayer = player.equals(game.getP1()) ? game.getP2() : game.getP1();
            var response = new MessageResponse(
                    "gameOver",
                    new GameOver(
                            "abandon",
                            otherPlayer
                    )
            );

            connectionService.sendMessage(
                    otherPlayer,
                    response
            );

            connectionService.sendMessage(
                    player,
                    response
            );

            gameResultRepository.save(
                    new GameResult(
                            game.getId(),
                            game.getP1(),
                            game.getP2(),
                            ResultType.ABANDON,
                            otherPlayer
                    )
            );

            connectionService.closeSession(player);
            connectionService.closeSession(otherPlayer);
        }
    }
}
