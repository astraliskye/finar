package com.skyegibney.finar.matchmaking;

import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.notifications.messages.ChatMessage;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.notifications.messages.PlayerReadyStatus;
import com.skyegibney.finar.websockets.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchmakingService {
    private final Queue<String> matchmakingQueue = new LinkedList<>();
    private final Random random;
    private final GameService gameService;
    private final ConnectionService connectionService;
    private final Map<Integer, Lobby> lobbies = new HashMap<>();

    int getLobbyIdByUsername(String username) {
        for (Lobby lobby : lobbies.values()) {
            if (lobby.players().stream().anyMatch(p -> p.getUsername().equals(username))) {
                return lobby.id();
            }
        }

        return -1;
    }

    public void queuePlayer(String username) {
        var gameId = gameService.getGameIdByPlayer(username);
        if (gameId != -1) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "gameInProgress",
                            gameId
                    )
            );
            return;
        }

        var lobbyId = getLobbyIdByUsername(username);
        if (lobbyId != -1) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "lobbyInProgress",
                            gameId
                    )
            );
            return;
        }

        if (!isPlayerInQueue(username)) {
            matchmakingQueue.add(username);
        }

        connectionService.sendMessage(
                username,
                new MessageResponse(
                        "ack",
                        "joinQueue"
                )
        );
    }

    public boolean isPlayerInQueue(String playerName) {
        return matchmakingQueue.contains(playerName);
    }

    public void removePlayerFromQueue(String playerName) {
        matchmakingQueue.remove(playerName);

        connectionService.sendMessage(
                playerName,
                new MessageResponse(
                        "ack",
                        "cancelQueue"
                )
        );
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

    public int createLobby(String username) {
        var players = new ArrayList<>(List.of(new Player(username, false)));
        var lobbyId = random.nextInt(Integer.MAX_VALUE);

        var lobby = new Lobby(
                lobbyId,
                username,
                players
        );

        lobbies.put(lobbyId, lobby);

        return lobby.id();
    }

    public void playerJoinLobby(String username, int lobbyId) {
        var lobby = lobbies.get(lobbyId);

        if (lobby == null) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "lobbyNotFound"
                    )
            );
            return;
        }

        if (lobby.players().stream().anyMatch(p -> p.username.equals(username))) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "lobbyInfo",
                            lobby

                    )
            );
        } else if (lobby.players().size() >= 2) {
            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "lobbyFull"
                    )
            );
        } else {
            lobby.players().add(new Player(username, false));

            connectionService.sendMessage(
                    username,
                    new MessageResponse(
                            "lobbyInfo",
                            lobby

                    )
            );

            lobby.players().forEach(p -> {
                if (!p.username.equals(username)) {
                    connectionService.sendMessage(
                            p.username,
                            new MessageResponse(
                                    "playerJoinedLobby",
                                    username
                            )
                    );
                }
            });
        }
    }

    public void chatMessage(String username, int lobbyId, String content) {
        var lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return;
        }

        if (lobby.players().stream().noneMatch(player -> player.username.equals(username))) {
            return;
        }

        lobby.players().forEach(player ->
            connectionService.sendMessage(
                    player.username,
                    new MessageResponse(
                            "lobbyChat",
                            new ChatMessage(
                                    username,
                                    content
                            )
                    )
            )
        );
    }

    public void togglePlayerReady(String username, int lobbyId) {
        var lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return;
        }

        lobby.players().forEach(player -> {
            if (player.username.equals(username)) {
                player.ready = !player.ready;

                lobby.players().forEach(recipient ->
                    connectionService.sendMessage(
                            recipient.username,
                            new MessageResponse(
                                    "playerReadyStatus",
                                    new PlayerReadyStatus(
                                            username,
                                            player.ready
                                    )
                            )
                    )
                );
            }
        });
    }

    public void startGame(String username, int lobbyId) {
        var lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return;
        }

        if (!lobby.owner().equals(username)) {
            return;
        }

        if (lobby.players().stream().mapToInt(player -> player.ready ? 1 : 0).sum() != 2) {
            return;
        }

        long gameId = gameService.createGame(
                lobby.players().get(0).username,

                lobby.players().get(1).username);

        lobby.players().forEach(p ->
                connectionService.sendMessage(
                        p.username,
                        new MessageResponse(
                                "matchFound",
                                gameId
                        )
                ));

        lobbies.remove(lobbyId);
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
                                        "matchFound",
                                        gameId
                                )
                        ));
            }
        }
    }
}
