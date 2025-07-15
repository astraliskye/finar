package com.skyegibney.finar.notifications;

import com.skyegibney.finar.game.events.*;
import com.skyegibney.finar.matchmaking.MatchmakingService;
import com.skyegibney.finar.matchmaking.events.LobbyDisbandedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerKickedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerLeftEvent;
import com.skyegibney.finar.notifications.messages.*;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.websockets.ConnectionService;
import com.skyegibney.finar.websockets.events.GameChatEvent;
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
  private final MatchmakingService matchmakingService;

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
                Long.toString(event.gameId()),
                event.player(),
                event.opponent(),
                event.turn(),
                event.moves(),
                event.timeControl(),
                event.wins(),
                event.draws(),
                event.losses())));
  }

  @EventListener
  void on(PlayerKickedEvent event) {
    matchmakingService
        .getPlayersByLobbyId(event.lobbyId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "playerKicked",
                        new PlayerKickedMessage(Long.toString(event.lobbyId()), event.player()))));

    connectionService.sendMessage(
        event.player(),
        new MessageResponse(
            "playerKicked",
            new PlayerKickedMessage(Long.toString(event.lobbyId()), event.player())));
  }

  @EventListener
  void on(PlayerLeftEvent event) {
    matchmakingService
        .getPlayersByLobbyId(event.lobbyId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "playerLeft",
                        new PlayerLeftMessage(Long.toString(event.lobbyId()), event.player()))));
  }

  @EventListener
  void on(LobbyDisbandedEvent event) {
    matchmakingService
        .getPlayersByLobbyId(event.lobbyId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "lobbyDisbanded",
                        new LobbyDisbandedMessage(Long.toString(event.lobbyId())))));
  }

  @EventListener
  void on(GameChatEvent event) {
    gameService
        .getPlayersByGameId(event.gameId())
        .forEach(
            player ->
                connectionService.sendMessage(
                    player,
                    new MessageResponse(
                        "gameChat", new ChatMessage(event.username(), event.message()))));
  }
}
