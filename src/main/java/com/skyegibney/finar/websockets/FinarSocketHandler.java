package com.skyegibney.finar.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.matchmaking.MatchmakingService;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

@Slf4j
@Component
public class FinarSocketHandler extends TextWebSocketHandler {
    private final ConnectionService connectionService;
    private final MatchmakingService matchmakingService;
    private final GameService gameService;

    public FinarSocketHandler(ConnectionService connectionService,
                              MatchmakingService matchmakingService,
                              GameService gameService) {
        this.connectionService = connectionService;
        this.matchmakingService = matchmakingService;
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.getAttributes().put("createdAt", Instant.now());
        session.getAttributes().put("lastMessageAt", Instant.now());
        connectionService.registerSession(session);

        // TODO: logic to rejoin active lobby or game
        // TODO: make it so that a player can't join a lobby if they're in another lobby or have an active game
        // TODO: make a way for players to leave active games (client side)
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        session.getAttributes().put("lastMessageAt", Instant.now());
        log.debug("Received message from {}: {}", session.getPrincipal().getName(), message.getPayload());
        super.handleMessage(session, message);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        try {
            ObjectMapper mapper = new ObjectMapper();
            ClientMessage clientMessage = mapper.readValue(message.getPayload(), ClientMessage.class);
            var username = session.getPrincipal().getName();

            switch (clientMessage.type()) {
                case "joinQueue":
                    matchmakingService.queuePlayer(session.getPrincipal().getName());
                    connectionService.sendMessage(session.getPrincipal().getName(),
                            new MessageResponse("ack", "joinQueue"));
                    break;
                case "cancelQueue":
                    if (matchmakingService.removePlayerFromQueue(session.getPrincipal().getName())) {
                        connectionService.sendMessage(
                                session.getPrincipal().getName(),
                                new MessageResponse("ack", "cancelQueue")
                        );
                    }
                case "joinLobby":
                    int joinLobbyId = clientMessage.lobbyId();
                    matchmakingService.playerJoinLobby(username, joinLobbyId);
                    break;
                case "readyPlayer":
                    int readyPlayerLobbyId = clientMessage.lobbyId();
                    matchmakingService.togglePlayerReady(username, readyPlayerLobbyId);
                    break;
                case "lobbyChat":
                    var chatLobbyId = clientMessage.lobbyId();
                    var content = (String)clientMessage.data();
                    matchmakingService.chatMessage(username, chatLobbyId, content);
                    break;
                case "startGame":
                    var startGameLobbyId = clientMessage.lobbyId();
                    matchmakingService.startGame(username, startGameLobbyId);
                case "joinGame":
                    if (!gameService.rejoinPlayer(username)) {
                        connectionService.sendMessage(
                                username,
                                new MessageResponse(
                                        "matchNotFound"
                                )
                        );
                    }
                    break;
                case "quit":
                    gameService.quitPlayer(
                            clientMessage.gameId(),
                            session.getPrincipal().getName());
                    break;
                case "move":
                    byte move = ((Integer) clientMessage.data()).byteValue();

                    gameService.makeMove(
                            clientMessage.gameId(),
                            username,
                            move
                    );
                    break;
                case "timeFlag":
                    gameService.handleFlag(
                            clientMessage.gameId(),
                            session.getPrincipal().getName());
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            log.info("Error processing JSON in text message: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        session.close(CloseStatus.SERVER_ERROR);
    }
}
