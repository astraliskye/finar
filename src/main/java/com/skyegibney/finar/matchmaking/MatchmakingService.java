package com.skyegibney.finar.matchmaking;

import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.websockets.ConnectionService;
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
        if (gameService.isPlayerInGame(playerName)) {
            return;
        }

        if (isPlayerInQueue(playerName)) {
            return;
        }

        matchmakingQueue.add(playerName);
    }

    public boolean isPlayerInQueue(String playerName) {
        return matchmakingQueue.contains(playerName);
    }

    public boolean removePlayerFromQueue(String playerName) {
        return matchmakingQueue.remove(playerName);
    }

    // Returns a list of two players or a list of zero players
    public List<String> matchPlayers() {
        List<String> result = new ArrayList<>();

        while (!matchmakingQueue.isEmpty() && result.size() < 2) {
            var player = matchmakingQueue.poll();
            if (connectionService.hasActiveConnection(player)) {
                result.add(player);

            }
        }

        if (result.size() == 1) {
            matchmakingQueue.add(result.getFirst());
            result.clear();
        }

        return result;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 8000)
    private void matchPlayerJob() {
        while (matchmakingQueue.size() >= 2) {
            List<String> players = matchPlayers();

            if (players.size() == 2) {
                long gameId = gameService.createGame(players.get(0), players.get(1));

                players.forEach(p ->
                        connectionService.sendMessage(
                                p,
                                new MessageResponse(
                                        "join",
                                        gameId
                                )
                        ));
            }
        }
    }
}
