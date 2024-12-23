package com.skyegibney.finar.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

@Slf4j
@Component
public class FinarSocketHandler extends TextWebSocketHandler {
    private final ConnectionService connectionService;

    public FinarSocketHandler(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.getAttributes().put("createdAt", Instant.now());
        session.getAttributes().put("lastMessageAt", Instant.now());
        connectionService.registerSession(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        session.getAttributes().put("lastMessageAt", Instant.now());
        log.debug("Received message from {}: {}", session.getPrincipal().getName(), message.getPayload());
        super.handleMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        session.close(CloseStatus.SERVER_ERROR);
    }
}
