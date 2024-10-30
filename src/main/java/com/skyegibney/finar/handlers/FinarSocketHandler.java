package com.skyegibney.finar.handlers;

import com.skyegibney.finar.dtos.messages.client.ClientMessage;
import com.skyegibney.finar.services.ConnectionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.EOFException;
import java.time.Instant;

@Component
public class FinarSocketHandler extends TextWebSocketHandler {
    private final ConnectionService connectionService;

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

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
        super.handleMessage(session, message);
    }
}
