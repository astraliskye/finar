package com.skyegibney.finar.services;

import com.skyegibney.finar.dtos.messages.server.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@Service
public class MatchmakingService {
    private final Queue<String> matchmakingQueue = new LinkedList<>();
    private final GameService gameService;
    private final ConnectionService connectionService;

    public MatchmakingService(GameService gameService, ConnectionService connectionService) {
        this.gameService = gameService;
        this.connectionService = connectionService;
    }

    public void queuePlayer(String playerName) {
        var game = gameService.getGameByPlayer(playerName);

        if (game != null) {
            connectionService.sendMessage(
                    playerName,
                    new MessageResponse(
                            "join",
                            game.getId()
                    )
            );
            return;
        }

        if (!isPlayerInQueue(playerName)) {
            matchmakingQueue.add(playerName);
        }

        connectionService.sendMessage(
                playerName,
                new MessageResponse(
                    "ack",
                        null
                )
        );
    }

    public boolean isPlayerInQueue(String playerName) {
        return matchmakingQueue.contains(playerName);
    }

    public void removePlayerFromQueue(String playerName) {
        matchmakingQueue.remove(playerName);
    }

    // Returns a list of two players or a list of zero players
    public List<String> matchPlayers() {
        List<String> result = new ArrayList<>();

        String player1 = null;
        while (!matchmakingQueue.isEmpty() && result.size() < 2) {
            var player = matchmakingQueue.poll();
            connectionService.hasActiveConnection(player);

            result.add(player);
        }

        if (result.size() == 1) {
            matchmakingQueue.add(result.get(0));
            result.clear();
        }

        return result;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 8000)
    private void matchPlayerJob() {
        log.debug("Matching {} players...", matchmakingQueue.size());
        while (matchmakingQueue.size() >= 2) {
            List<String> players = matchPlayers();

            if (players.size() == 2) {
                log.debug("Matching players {} and {}", players.get(0), players.get(1));
                long gameId = gameService.createGame(players.get(0), players.get(1));

                connectionService.sendMessage(
                        players.get(0),
                        new MessageResponse(
                                "join",
                                gameId
                        )
                );

                connectionService.sendMessage(
                        players.get(1),
                        new MessageResponse(
                                "join",
                                gameId
                        )
                );

                connectionService.closeSession(players.get(0));
                connectionService.closeSession(players.get(1));
            }
        }
    }
}
