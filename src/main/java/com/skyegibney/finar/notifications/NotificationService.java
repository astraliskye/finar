package com.skyegibney.finar.notifications;

import com.skyegibney.finar.game.events.*;
import com.skyegibney.finar.notifications.messages.InitialJoin;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.notifications.messages.TimeControl;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.websockets.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
  private final ConnectionService connectionService;
  private final GameService gameService;

  @EventListener
  void on(MoveMadeEvent event) {
    gameService
        .getPlayersByGameId(event.gameId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "move",
                        new Move(
                            event.player(),
                            event.n(),
                            new TimeControl(event.player1Time(), event.player2Time())))));
  }

  @EventListener
  void on(GameOverEvent event) {
    gameService
        .getPlayersByGameId(event.gameId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse("gameOver", new GameOver(event.result(), event.winner()))));
  }

  @EventListener
  void on(FinarGameOverEvent event) {
    gameService
        .getPlayersByGameId(event.gameId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "finarGameOver",
                        new FinarGameOver(
                            event.result(),
                            event.winner(),
                            Arrays.stream(event.winningMoves())
                                .boxed()
                                .map(Object::toString)
                                .collect(Collectors.joining(","))))));
  }

  @EventListener
  void on(TimeUpdateEvent event) {
    gameService
        .getPlayersByGameId(event.gameId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "timeUpdate", new TimeControl(event.player1Time(), event.player2Time()))));
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
                event.draws(),
                event.losses())));
  }
}
