package com.skyegibney.finar.game;

import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
class Game {
  private long id;

  private List<String> players = new ArrayList<>();

  private int[] board = new int[100];
  private int currentMove = 0;
  private List<Integer> moves = new ArrayList<>();

  private String winner;
  private boolean finar = false;
  private int[] winningMoves = new int[5];

  private Instant player1LastMove;
  private Instant player2LastMove;

  private long lastTimeUpdate;
  private long player1Time;
  private long player2Time;

  private SecureRandom random = new SecureRandom();

  public Game(String p1, String p2) {
    players.add(p1);
    players.add(p2);
    id = random.nextLong();
    lastTimeUpdate = System.currentTimeMillis();
    player1Time = 2 * 60 * 1000;
    player2Time = 2 * 60 * 1000;
  }

  // currentMove can either be 0 or 1
  // board cells store 1 or 2
  void makeMove(int n) throws ArrayIndexOutOfBoundsException {
    board[n] = currentMove + 1;
    moves.add(n);
    currentMove = (currentMove + 1) % 2;
  }

  public void checkFinar() {
    for (int i = 0; i < 100; i++) {
      // Horizontal win
      if (Math.floorDiv(i, 10) == Math.floorDiv(i + 4, 10)) {
        int a = board[i];
        int b = board[i + 1];
        int c = board[i + 2];
        int d = board[i + 3];
        int e = board[i + 4];

        if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
          finar = true;
          winner = players.getFirst();
          winningMoves = new int[] {i, i + 1, i + 2, i + 3, i + 4};
        }

        if (a % 2 == 0
            && a != 0
            && b % 2 == 0
            && b != 0
            && c % 2 == 0
            && c != 0
            && d % 2 == 0
            && d != 0
            && e % 2 == 0
            && e != 0) {
          finar = true;
          winner = players.get(1);
          winningMoves = new int[] {i, i + 1, i + 2, i + 3, i + 4};
        }
      }

      // Check vertical win
      if (i + 40 < 100) {
        int a = board[i];
        int b = board[i + 10];
        int c = board[i + 20];
        int d = board[i + 30];
        int e = board[i + 40];

        if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
          finar = true;
          winner = players.getFirst();
          winningMoves = new int[] {i, i + 10, i + 20, i + 30, i + 40};
        }

        if (a % 2 == 0
            && a != 0
            && b % 2 == 0
            && b != 0
            && c % 2 == 0
            && c != 0
            && d % 2 == 0
            && d != 0
            && e % 2 == 0
            && e != 0) {
          finar = true;
          winner = players.get(1);
          winningMoves = new int[] {i, i + 10, i + 20, i + 30, i + 40};
        }
      }

      // Check down-right diagonal win
      if (i + 44 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i + 4, 10)) {
        int a = board[i];
        int b = board[i + 11];
        int c = board[i + 22];
        int d = board[i + 33];
        int e = board[i + 44];

        if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
          finar = true;
          winner = players.getFirst();
          winningMoves = new int[] {i, i + 11, i + 22, i + 33, i + 44};
        }

        if (a % 2 == 0
            && a != 0
            && b % 2 == 0
            && b != 0
            && c % 2 == 0
            && c != 0
            && d % 2 == 0
            && d != 0
            && e % 2 == 0
            && e != 0) {
          finar = true;
          winner = players.get(1);
          winningMoves = new int[] {a, b, c, d, e};
          winningMoves = new int[] {i, i + 11, i + 22, i + 33, i + 44};
        }
      }

      // Check down-left diagonal win
      if (i + 40 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i - 4, 10)) {
        int a = board[i];
        int b = board[i + 9];
        int c = board[i + 18];
        int d = board[i + 27];
        int e = board[i + 36];

        if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
          finar = true;
          winner = players.getFirst();
          winningMoves = new int[] {i, i + 9, i + 18, i + 27, i + 36};
        }

        if (a % 2 == 0
            && a != 0
            && b % 2 == 0
            && b != 0
            && c % 2 == 0
            && c != 0
            && d % 2 == 0
            && d != 0
            && e % 2 == 0
            && e != 0) {
          finar = true;
          winner = players.get(1);
          winningMoves = new int[] {i, i + 9, i + 18, i + 27, i + 36};
        }
      }
    }
  }

  public String getCurrentTurn() {
    return players.get(currentMove);
  }

  public String getPlayerOne() {
    return players.getFirst();
  }

  public String getPlayerTwo() {
    return players.get(1);
  }

  public void shufflePlayers() {
    Collections.shuffle(players);
  }
}
