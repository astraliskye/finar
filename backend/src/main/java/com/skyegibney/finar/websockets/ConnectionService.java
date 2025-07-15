package com.skyegibney.finar.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class ConnectionService {
  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  public void registerSession(WebSocketSession session) {
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

  public void sendMessage(String username, MessageResponse message) {
    var session = sessions.get(username);
    if (session == null) {
      return;
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      String payload = mapper.writeValueAsString(message);
      session.sendMessage(new TextMessage(payload));
      log.debug("Sending message to user {}: {}", username, payload);
    } catch (IOException e) {
      log.error("Error while sending message for user '{}': {}", username, e.getMessage());
    }
  }

  // Prune connections every 5 seconds
  @Scheduled(initialDelay = 5000, fixedRate = 5000)
  @Async
  public void pruneInactiveSessions() {
    for (var entry : sessions.entrySet()) {
      Object lastMessageAt = entry.getValue().getAttributes().get("lastMessageAt");
      Object createdAt = entry.getValue().getAttributes().get("createdAt");

      if (createdAt instanceof Instant
          && lastMessageAt instanceof Instant
          && (Duration.between((Instant) createdAt, Instant.now()).compareTo(Duration.ofSeconds(5))
                  < 0
              || Duration.between((Instant) lastMessageAt, Instant.now())
                      .compareTo(Duration.ofSeconds(5))
                  < 0)) {
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
