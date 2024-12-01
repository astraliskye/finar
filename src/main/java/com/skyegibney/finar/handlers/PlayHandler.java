package com.skyegibney.finar.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.dtos.messages.client.ClientMessage;
import com.skyegibney.finar.dtos.messages.server.InitialJoin;
import com.skyegibney.finar.dtos.messages.server.MessageResponse;
import com.skyegibney.finar.dtos.messages.server.TimeControl;
import com.skyegibney.finar.services.ConnectionService;
import com.skyegibney.finar.services.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.stream.Collectors;

@Slf4j
@Component
public class PlayHandler extends FinarSocketHandler {
    private final GameService gameService;
    private final ConnectionService connectionService;

    public PlayHandler(GameService gameService, ConnectionService connectionService) {
        super(connectionService);
        this.gameService = gameService;
        this.connectionService = connectionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        var username = session.getPrincipal().getName();
        gameService.initialJoin(username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClientMessage clientMessage = mapper.readValue(message.getPayload(), ClientMessage.class);

            switch (clientMessage.type()) {
                case "quit":
                    gameService.quitPlayer(
                            clientMessage.gameId(),
                            session.getPrincipal().getName());
                    break;
                case "move":
                    byte move = ((Integer)clientMessage.data()).byteValue();

                    log.debug("==================================================");
                    log.debug("Game ID: {}", clientMessage.gameId());

                    gameService.makeMove(
                            clientMessage.gameId(),
                            session.getPrincipal().getName(),
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
}
