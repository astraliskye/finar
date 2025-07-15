package com.skyegibney.finar.game;

import com.skyegibney.finar.game.events.*;
import com.skyegibney.finar.notifications.messages.TimeControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
  private final GameResultRepository gameResultRepository;
  private final ApplicationEventPublisher publisher;
  private final Map<Long, Game> activeGames = new HashMap<>();

  @Scheduled(fixedRate = 20 * 1000)
  public void timeCheck() {
    for (var game : activeGames.values()) {
      var timeout = checkTimeoutAndUpdate(game);

      if (timeout) {
        cleanupGame(
            game, ResultType.TIMEOUT, game.getPlayers().get((game.getCurrentMove() + 1) % 2));
      }
    }
  }

  public long createGame(String player1, String player2) {
    var game = new Game(player1, player2);
    activeGames.put(game.getId(), game);
    game.shufflePlayers();
    return game.getId();
  }

  public Optional<Long> getGameIdByPlayer(String player) {
    for (var game : activeGames.values()) {
      if (game.getPlayers().contains(player)) {
        return Optional.of(game.getId());
      }
    }

    return Optional.empty();
  }

  public List<String> getPlayersByGameId(long gameId) {
    var game = activeGames.get(gameId);

    if (game == null) {
      return new ArrayList<>();
    } else {
      return game.getPlayers();
    }
  }

  public void quitPlayer(long gameId, String player) {
    var game = activeGames.get(gameId);

    if (game == null || !game.getPlayers().contains(player)) {
      return;
    }

    if (game.getMoves().size() < 2) {
      cleanupGame(game, ResultType.ABORT, "");
    } else {
      var otherPlayer = game.getPlayers().get((game.getPlayers().indexOf(player) + 1) % 2);
      cleanupGame(game, ResultType.ABANDON, otherPlayer);
    }
  }

  public boolean rejoinPlayer(String player) {
    for (var game : activeGames.values()) {
      if (game.getPlayers().contains(player)) {
        var timeout = checkTimeoutAndUpdate(game);

        if (timeout) {
          cleanupGame(
              game,
              ResultType.TIMEOUT,
              game.getPlayers()
                  .get(
                      game.getPlayers().indexOf((game.getCurrentTurn()) + 1)
                          % game.getPlayers().size()));
          return false;
        }

        log.debug("Starting gameResultResponse query");
        PlayerRecord playerRecord =
            gameResultRepository.getUserRecordVsOpponent(
                player,
                game.getPlayerOne().equals(player) ? game.getPlayerTwo() : game.getPlayerOne());

        publisher.publishEvent(
            new PlayerJoinEvent(
                game.getId(),
                player,
                game.getPlayers().get((game.getPlayers().indexOf(player) + 1) % 2),
                game.getPlayers().indexOf(player),
                game.getMoves().stream().map(Object::toString).collect(Collectors.joining(",")),
                new TimeControl(game.getPlayer1Time(), game.getPlayer2Time()),
                playerRecord.getWins(),
                playerRecord.getDraws(),
                playerRecord.getLosses()));

        return true;
      }
    }

    return false;
  }

  public void makeMove(long gameId, String player, byte n) {
    var game = activeGames.get(gameId);

    if (game == null
        || !player.equals(game.getCurrentTurn())
        || n < 0
        || n > game.getBoard().length) {
      return;
    }

    var players = game.getPlayers();
    var otherPlayer = players.get((game.getCurrentMove() + 1) % players.size());

    var timeout = checkTimeoutAndUpdate(game);

    if (timeout) {
      cleanupGame(game, ResultType.TIMEOUT, otherPlayer);
      return;
    }

    game.makeMove(n);

    publisher.publishEvent(
        new MoveMadeEvent(gameId, player, n, game.getPlayer1Time(), game.getPlayer2Time()));

    game.checkFinar();
    if (game.isFinar()) {
      cleanupGame(game, ResultType.FINAR, game.getWinner());
    } else if (game.getMoves().size() == game.getBoard().length) {
      cleanupGame(game, ResultType.DRAW, "");
    }
  }

  public void handleFlag(long gameId, String player) {
    var game = activeGames.get(gameId);
    if (game == null) {
      return;
    }

    var timeout = checkTimeoutAndUpdate(game);

    if (timeout) {
      cleanupGame(game, ResultType.TIMEOUT, player);
    } else {
      publisher.publishEvent(
          new TimeUpdateEvent(gameId, game.getPlayer1Time(), game.getPlayer2Time()));
    }
  }

  private boolean checkTimeoutAndUpdate(Game game) {
    var currentTime = System.currentTimeMillis();
    var timeDiff = currentTime - game.getLastTimeUpdate();
    var currentPlayer = game.getCurrentTurn();

    if (currentPlayer.equals(game.getPlayerOne()) && !game.getMoves().isEmpty()) {
      var newTime = Math.max(game.getPlayer1Time() - timeDiff, 0);

      game.setPlayer1Time(newTime);

      if (newTime == 0) {
        return true;
      }
    } else if (game.getPlayerTwo().equals(game.getCurrentTurn()) && game.getMoves().size() > 1) {
      var newTime = Math.max(game.getPlayer2Time() - timeDiff, 0);
      game.setPlayer2Time(newTime);
      game.setLastTimeUpdate(currentTime);

      if (newTime == 0) {
        return true;
      }
    }

    game.setLastTimeUpdate(currentTime);
    return false;
  }

  private void cleanupGame(Game game, ResultType resultType, String winner) {
    if (resultType.equals(ResultType.FINAR)) {
      publisher.publishEvent(
          new FinarGameOverEvent(
              game.getId(),
              game.getPlayerOne(),
              game.getPlayerTwo(),
              resultType.name(),
              winner,
              game.getWinningMoves()));
    } else {
      publisher.publishEvent(
          new GameOverEvent(
              game.getId(), game.getPlayerOne(), game.getPlayerTwo(), resultType.name(), winner));
    }

    gameResultRepository.save(
        new GameResult(
            game.getId(),
            game.getPlayers().get(0),
            game.getPlayers().get(1),
            resultType,
            winner,
            game.getMoves().stream().map(Object::toString).collect(Collectors.joining(","))));

    activeGames.remove(game.getId());
  }
}
