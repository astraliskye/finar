package com.skyegibney.finar.notifications;

import com.skyegibney.finar.notifications.messages.InitialJoin;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.notifications.messages.TimeControl;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.game.events.GameOverEvent;
import com.skyegibney.finar.game.events.MoveMadeEvent;
import com.skyegibney.finar.game.events.PlayerJoinEvent;
import com.skyegibney.finar.game.events.TimeUpdateEvent;
import com.skyegibney.finar.websockets.ConnectionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final ConnectionService connectionService;
    private final GameService gameService;

    public NotificationService(ConnectionService connectionService, GameService gameService) {
        this.connectionService = connectionService;
        this.gameService = gameService;
    }

    @EventListener
    void on(MoveMadeEvent event) {
        gameService.getPlayersByGameId(event.gameId()).forEach(player -> {
                    connectionService.sendMessage(
                            player,
                            new MessageResponse(
                                    "move",
                                    new Move(
                                            event.player(),
                                            event.n(),
                                            new TimeControl(
                                                    event.player1Time(),
                                                    event.player2Time()
                                            )
                                    )
                            ));
                }
        );
    }

    @EventListener
    void on(GameOverEvent event) {
        gameService.getPlayersByGameId(event.gameId()).forEach(player -> {
            connectionService.sendMessage(
                    player,
                    new MessageResponse(
                            "gameOver",
                            new GameOver(
                                    event.result(),
                                    event.winner()
                            )
                    ));
        });
    }

    @EventListener
    void on(TimeUpdateEvent event) {
        gameService.getPlayersByGameId(event.gameId()).forEach(player -> {
            connectionService.sendMessage(
                    player,
                    new MessageResponse(
                            "timeUpdate",
                            new TimeControl(
                                    event.player1Time(),
                                    event.player2Time()
                            )
                    )
            );
        });
    }

    @EventListener
    void on(PlayerJoinEvent event) {
        connectionService.sendMessage(
                event.player(),
                new MessageResponse(
                        "initialJoin",
                        new InitialJoin(
                                event.gameId(),
                                event.player(),
                                event.opponent(),
                                event.turn(),
                                event.moves(),
                                event.timeControl(),
                                event.wins(),
                                event.losses()
                        )
                )
        );
    }
}
