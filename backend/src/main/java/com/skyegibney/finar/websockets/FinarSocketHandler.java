package com.skyegibney.finar.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.matchmaking.MatchmakingService;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.websockets.events.GameChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinarSocketHandler extends TextWebSocketHandler {
  private final ConnectionService connectionService;
  private final MatchmakingService matchmakingService;
  private final GameService gameService;
  private final ApplicationEventPublisher publisher;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    session.getAttributes().put("createdAt", Instant.now());
    session.getAttributes().put("lastMessageAt", Instant.now());
    connectionService.registerSession(session);
  }

  @Override
  public void handleMessage(WebSocketSession session, @NonNull WebSocketMessage<?> message)
      throws Exception {
    if (session.getPrincipal() == null) {
      return;
    }
    session.getAttributes().put("lastMessageAt", Instant.now());
    log.debug(
        "Received message from {}: {}", session.getPrincipal().getName(), message.getPayload());
    super.handleMessage(session, message);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
    super.handleTextMessage(session, message);

    try {
      ObjectMapper mapper = new ObjectMapper();
      ClientMessage clientMessage = mapper.readValue(message.getPayload(), ClientMessage.class);

      if (session.getPrincipal() == null) {
        return;
      }
      var username = session.getPrincipal().getName();

      switch (clientMessage.type()) {
        case "joinQueue":
          matchmakingService.queuePlayer(session.getPrincipal().getName());
          break;
        case "cancelQueue":
          matchmakingService.removePlayerFromQueue(session.getPrincipal().getName());
        case "joinLobby":
          int joinLobbyId = clientMessage.lobbyId();
          matchmakingService.playerJoinLobby(username, joinLobbyId);
          break;
        case "readyPlayer":
          int readyPlayerLobbyId = clientMessage.lobbyId();
          matchmakingService.togglePlayerReady(username, readyPlayerLobbyId);
          break;
        case "kickPlayer":
          int kickPlayerLobbyId = clientMessage.lobbyId();
          matchmakingService.kickPlayer(username, kickPlayerLobbyId, (String)clientMessage.data());
          break;
        case "lobbyChat":
          var chatLobbyId = clientMessage.lobbyId();
          var lobbyChatContent = (String) clientMessage.data();
          matchmakingService.chatMessage(username, chatLobbyId, lobbyChatContent);
          break;
        case "gameChat":
          var chatGameId = clientMessage.gameId();
          var gameChatContent = (String) clientMessage.data();
          publisher.publishEvent(new GameChatEvent(
                  Long.parseLong(chatGameId),
                  username,
                  gameChatContent
          ));
          break;
        case "startGame":
          var startGameLobbyId = clientMessage.lobbyId();
          matchmakingService.startGame(username, startGameLobbyId);
        case "joinGame":
          if (!gameService.rejoinPlayer(username)) {
            connectionService.sendMessage(username, new MessageResponse("matchNotFound"));
          }
          break;
        case "quit":
          gameService.quitPlayer(Long.parseLong(clientMessage.gameId()), session.getPrincipal().getName());
          break;
        case "move":
          byte move = ((Integer) clientMessage.data()).byteValue();

          gameService.makeMove(Long.parseLong(clientMessage.gameId()), username, move);
          break;
        case "timeFlag":
          gameService.handleFlag(Long.parseLong(clientMessage.gameId()), session.getPrincipal().getName());
        default:
          break;
      }
    } catch (JsonProcessingException e) {
      log.info("Error processing JSON in text message: {}", e.getMessage());
    }
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
    super.handleTransportError(session, exception);
    session.close(CloseStatus.SERVER_ERROR);
  }
}
