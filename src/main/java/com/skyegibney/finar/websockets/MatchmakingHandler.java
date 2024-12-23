package com.skyegibney.finar.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.matchmaking.MatchmakingService;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class MatchmakingHandler extends FinarSocketHandler {
    private final MatchmakingService matchmakingService;
    private final ConnectionService connectionService;
    private final GameService gameService;

    public MatchmakingHandler(MatchmakingService matchmakingService, ConnectionService connectionService, GameService gameService) {
        super(connectionService);
        this.matchmakingService = matchmakingService;
        this.connectionService = connectionService;
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        var username = session.getPrincipal().getName();

        if (gameService.isPlayerInGame(username)) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "redirect",
                            "play"
                    )
            );
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClientMessage clientMessage = mapper.readValue(message.getPayload(), ClientMessage.class);

            switch (clientMessage.type()) {
                case "join":
                    matchmakingService.queuePlayer(session.getPrincipal().getName());
                    connectionService.sendMessage(session.getPrincipal().getName(),
                            new MessageResponse("ack", null));
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            log.info("Error processing JSON in text message: {}", e.getMessage());
        }
    }
}
