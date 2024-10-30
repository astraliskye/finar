package com.skyegibney.finar.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.dtos.messages.client.ClientMessage;
import com.skyegibney.finar.services.ConnectionService;
import com.skyegibney.finar.services.MatchmakingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class MatchmakingHandler extends FinarSocketHandler {
    private final MatchmakingService matchmakingService;

    public MatchmakingHandler(MatchmakingService matchmakingService, ConnectionService connectionService) {
        super(connectionService);
        this.matchmakingService = matchmakingService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClientMessage clientMessage = mapper.readValue(message.getPayload(), ClientMessage.class);

            switch (clientMessage.type()) {
                case "join":
                    matchmakingService.queuePlayer(session.getPrincipal().getName());
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            log.info("Error processing JSON in text message: {}", e.getMessage());
        }
    }
}
