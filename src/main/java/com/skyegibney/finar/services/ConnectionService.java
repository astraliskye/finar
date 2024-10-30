package com.skyegibney.finar.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.dtos.messages.client.ClientMessage;
import com.skyegibney.finar.dtos.messages.server.MessageResponse;
import com.skyegibney.finar.errors.DuplicateConnectionError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableScheduling
public class ConnectionService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) throws DuplicateConnectionError {
        var principal = session.getPrincipal();

        if (principal == null) {
            log.debug("Error attempting to register session with null principle");
            return;
        }

        sessions.put(principal.getName(), session);
    }

    public boolean hasActiveConnection(String username) {
        return sessions.containsKey(username);
    }

    public void closeSession(String username) {
        try {
            sessions.get(username).close();
        } catch (Exception e) {
            log.debug("Exception while closing session for user '{}': {}", username, e.getMessage());
        }

        sessions.remove(username);
    }

    public void sendMessage(String username, MessageResponse message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(message);
            sessions.get(username).sendMessage(new TextMessage(payload));
            log.debug("Sending message to user {}: {}", username, payload);
        } catch (IOException e) {
            log.error("Error while sending message for user '{}': {}", username, e.getMessage());
        }
    }

    // Prune connections every 5 seconds
    @Scheduled(initialDelay = 5000, fixedRate = 5000)
    private void pruneInactiveSessions() {
        for (var entry : sessions.entrySet()) {
            Object lastMessageAt = entry.getValue().getAttributes().get("lastMessageAt");
            Object createdAt = entry.getValue().getAttributes().get("createdAt");

            if (createdAt instanceof Instant
                    && lastMessageAt instanceof Instant
                    && (Duration.between((Instant)createdAt, Instant.now()).compareTo(Duration.ofSeconds(5)) < 0
                    || Duration.between((Instant)lastMessageAt, Instant.now()).compareTo(Duration.ofSeconds(5)) < 0)) {
                continue;
            }

            try {
                log.debug("CreatedAt: {}", createdAt);
                log.debug("LastMessageAt: {}", lastMessageAt);
                log.debug("Instant.now(): {}", Instant.now());
                entry.getValue().close();
            } catch (IOException e) {
                log.debug("Exception while closing session for user '{}'", entry.getKey());
            }

            sessions.remove(entry.getKey());
        }
    }
}
