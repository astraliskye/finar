package com.skyegibney.finar.matchmaking;

import com.skyegibney.finar.game.GameService;
import com.skyegibney.finar.matchmaking.events.LobbyDisbandedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerKickedEvent;
import com.skyegibney.finar.matchmaking.events.PlayerLeftEvent;
import com.skyegibney.finar.notifications.messages.ChatMessage;
import com.skyegibney.finar.notifications.messages.MessageResponse;
import com.skyegibney.finar.notifications.messages.PlayerReadyStatus;
import com.skyegibney.finar.websockets.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher publisher;

  private final Map<Integer, Lobby> lobbies = new HashMap<>();

  public void queuePlayer(String username) {
    // Redirect player to game if they are currently in a game
    var gameId = gameService.getGameIdByPlayer(username);
    if (gameId.isPresent()) {
      connectionService.sendMessage(username, new MessageResponse("matchFound", Long.toString(gameId.get())));
      return;
    }

    removePlayerFromLobbies(username);

    if (!isPlayerInQueue(username)) {
      matchmakingQueue.add(username);
    }

    connectionService.sendMessage(username, new MessageResponse("ack", "joinQueue"));
  }

  public boolean isPlayerInQueue(String playerName) {
    return matchmakingQueue.contains(playerName);
  }

  public void removePlayerFromQueue(String playerName) {
    matchmakingQueue.remove(playerName);
    connectionService.sendMessage(playerName, new MessageResponse("ack", "cancelQueue"));
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

  public List<String> getPlayersByLobbyId(int lobbyId) {
    var lobby = lobbies.get(lobbyId);

    if (lobby == null) {
      return new ArrayList<>();
    }

    return lobby.players().stream().map(Player::getUsername).toList();
  }

  public int createLobby(String username) {
    var gameId = gameService.getGameIdByPlayer(username);
    if (gameId.isPresent()) {
      connectionService.sendMessage(username, new MessageResponse("matchFound", Long.toString(gameId.get())));
      return -1;
    }

    var players = new ArrayList<>(List.of(new Player(username, false)));
    var lobbyId = random.nextInt(Integer.MAX_VALUE);
    var lobby = new Lobby(lobbyId, username, players);

    lobbies.put(lobbyId, lobby);

    return lobby.id();
  }

  public void playerJoinLobby(String username, int lobbyId) {
    var lobby = lobbies.get(lobbyId);

    if (lobby == null) {
      connectionService.sendMessage(username, new MessageResponse("lobbyNotFound"));
      return;
    }

    if (lobby.players().stream().anyMatch(p -> p.username.equals(username))) {
      connectionService.sendMessage(username, new MessageResponse("lobbyInfo", lobby));
    } else if (lobby.players().size() >= 2) {
      connectionService.sendMessage(username, new MessageResponse("lobbyFull"));
    } else {
      removePlayerFromQueue(username);
      removePlayerFromLobbies(username);
      lobby.players().add(new Player(username, false));

      connectionService.sendMessage(username, new MessageResponse("lobbyInfo", lobby));

      lobby
          .players()
          .forEach(
              p -> {
                if (!p.username.equals(username)) {
                  connectionService.sendMessage(
                      p.username, new MessageResponse("playerJoinedLobby", username));
                }
              });
    }
  }

  private void removePlayerFromLobbies(String username) {
    var playerLobbies =
        lobbies.values().stream()
            .filter(
                lobby ->
                    lobby.players().stream().anyMatch(player -> player.username.equals(username)))
            .toList();

    playerLobbies.forEach(
        lobby -> {
          lobby.players().removeIf(p -> p.getUsername().equals(username));

          // Notify other players
          if (lobby.owner().equals(username)) {
            publisher.publishEvent(new LobbyDisbandedEvent(lobby.id()));
            lobbies.remove(lobby.id());
          } else {
            publisher.publishEvent(new PlayerLeftEvent(lobby.id(), username));
          }
        });
  }

  public void chatMessage(String username, int lobbyId, String content) {
    var lobby = lobbies.get(lobbyId);
    if (lobby == null) {
      return;
    }

    if (lobby.players().stream().noneMatch(player -> player.username.equals(username))) {
      return;
    }

    lobby
        .players()
        .forEach(
            player ->
                connectionService.sendMessage(
                    player.username,
                    new MessageResponse("lobbyChat", new ChatMessage(username, content))));
  }

  public void kickPlayer(String principalUsername, int lobbyId, String playerToKick) {
    var lobby = lobbies.get(lobbyId);
    if (lobby == null) {
      return;
    }

    if (!lobby.owner().equals(principalUsername)) {
      return;
    }

    if (lobby.owner().equals(playerToKick)) {
      return;
    }

    if (lobby.players().removeIf(player -> player.username.equals(playerToKick))) {
      publisher.publishEvent(new PlayerKickedEvent(lobbyId, playerToKick));
    }
  }

  public void togglePlayerReady(String username, int lobbyId) {
    var lobby = lobbies.get(lobbyId);
    if (lobby == null) {
      return;
    }

    lobby
        .players()
        .forEach(
            player -> {
              if (player.username.equals(username)) {
                player.ready = !player.ready;

                lobby
                    .players()
                    .forEach(
                        recipient ->
                            connectionService.sendMessage(
                                recipient.username,
                                new MessageResponse(
                                    "playerReadyStatus",
                                    new PlayerReadyStatus(username, player.ready))));
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

    var gameId =
        gameService.createGame(lobby.players().get(0).username, lobby.players().get(1).username);

    lobby
        .players()
        .forEach(
            p ->
                connectionService.sendMessage(
                    p.username, new MessageResponse("matchFound", Long.toString(gameId))));

    lobbies.remove(lobbyId);
  }

  @Scheduled(initialDelay = 1000, fixedDelay = 8000)
  private void matchPlayerJob() {
    while (matchmakingQueue.size() >= 2) {
      List<String> players = matchPlayers();

      if (players.size() == 2) {
        var gameId = gameService.createGame(players.get(0), players.get(1));

        players.forEach(
            p -> connectionService.sendMessage(p, new MessageResponse("matchFound", Long.toString(gameId))));
      }
    }
  }
}
