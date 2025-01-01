package com.skyegibney.finar.game;

import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
class Game {
    private int id;

    private List<String> players = new ArrayList<>();

    private byte[] board = new byte[100];
    private int currentMove = 0;
    private List<Byte> moves = new ArrayList<>();

    private String winner;
    private boolean finar = false;
    private byte[] winningMoves = new byte[5];

    private Instant player1LastMove;
    private Instant player2LastMove;

    private long lastTimeUpdate;
    private long player1Time;
    private long player2Time;

    private SecureRandom random = new SecureRandom();

    public Game(String p1, String p2) {
        players.add(p1);
        players.add(p2);
        id = random.nextInt(Integer.MAX_VALUE);
        lastTimeUpdate = System.currentTimeMillis();
        player1Time = 2 * 60 * 1000;
        player2Time = 2 * 60 * 1000;
    }

    // currentMove can either be 0 or 1
    // board cells store 1 or 2
    void makeMove(byte n) throws ArrayIndexOutOfBoundsException {
        board[n] = (byte)(currentMove + 1);
        moves.add(n);
        currentMove = (currentMove + 1) % 2;
    }

    public void checkFinar() {
        for (int i = 0; i < 100; i++) {
            // Horizontal win
            if (Math.floorDiv(i, 10) == Math.floorDiv(i + 4, 10)) {
                byte a = board[i];
                byte b = board[i + 1];
                byte c = board[i + 2];
                byte d = board[i + 3];
                byte e = board[i + 4];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = players.get(0);
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                    && b % 2 == 0 && b != 0
                    && c % 2 == 0 && c != 0
                    && d % 2 == 0 && d != 0
                    && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = players.get(1);
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }

            // Check vertical win
            if (i + 40 < 100) {
                byte a = board[i];
                byte b = board[i + 10];
                byte c = board[i + 20];
                byte d = board[i + 30];
                byte e = board[i + 40];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = players.get(0);
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = players.get(1);
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }

            // Check down-right diagonal win
            if (i + 44 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i + 4, 10)) {
                byte a = board[i];
                byte b = board[i + 11];
                byte c = board[i + 22];
                byte d = board[i + 33];
                byte e = board[i + 44];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = players.get(0);
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = players.get(1);
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }

            // Check down-left diagonal win
            if (i + 40 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i - 4, 10)) {
                byte a = board[i];
                byte b = board[i + 9];
                byte c = board[i + 18];
                byte d = board[i + 27];
                byte e = board[i + 36];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = players.get(0);
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = players.get(1);
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }
        }
    }

    public String getCurrentTurn() {
        return players.get(currentMove);
    }

    public String getPlayerOne() {
        return players.get(0);
    }

    public String getPlayerTwo() {
        return players.get(1);
    }

    public void shufflePlayers() {
        Collections.shuffle(players);
    }
}
