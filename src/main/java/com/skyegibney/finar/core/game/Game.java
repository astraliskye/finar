package com.skyegibney.finar.core.game;

import com.skyegibney.finar.errors.InvalidMoveException;
import com.skyegibney.finar.errors.OutOfTurnException;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class Game {
    private long id;

    private String p1;
    private String p2;

    private byte[] board = new byte[100];
    private byte currentMove = 1;
    private List<Byte> moves = new ArrayList<>();

    private String winner;
    private boolean finar = false;
    private byte[] winningMoves = new byte[5];

    private Instant player1LastMove;
    private Instant player2LastMove;

    private long lastTimeUpdate;
    private AtomicLong player1Time;
    private AtomicLong player2Time;

    private SecureRandom random = new SecureRandom();

    public Game(String p1, String p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = random.nextLong();
        lastTimeUpdate = System.currentTimeMillis();
        player1Time = new AtomicLong(10 * 60 * 1000);
        player2Time = new AtomicLong(10 * 60 * 1000);
    }

    public void makeMove(String player, byte n) throws InvalidMoveException, OutOfTurnException {
        if (n < 0 || n >= 100) {
            throw new InvalidMoveException();
        }

        if (!getCurrentTurn().equals(player)) {
            throw new OutOfTurnException();
        }

        try {
            board[n] = currentMove;
            moves.add(n);
            currentMove++;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidMoveException();
        }
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
                    winner = p1;
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                    && b % 2 == 0 && b != 0
                    && c % 2 == 0 && c != 0
                    && d % 2 == 0 && d != 0
                    && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = p2;
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
                    winner = p1;
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = p2;
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }

            if (i + 44 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i + 4, 10)) {
                byte a = board[i];
                byte b = board[i + 11];
                byte c = board[i + 22];
                byte d = board[i + 33];
                byte e = board[i + 44];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = p1;
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = p2;
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }

            if (i + 40 < 100 && Math.floorDiv(i, 10) == Math.floorDiv(i - 4, 10)) {
                byte a = board[i];
                byte b = board[i + 9];
                byte c = board[i + 18];
                byte d = board[i + 27];
                byte e = board[i + 36];

                if ((a % 2 + b % 2 + c % 2 + d % 2 + e % 2) == 5) {
                    finar = true;
                    winner = p1;
                    winningMoves = new byte[]{a, b, c, d, e};
                }

                if (a % 2 == 0 && a != 0
                        && b % 2 == 0 && b != 0
                        && c % 2 == 0 && c != 0
                        && d % 2 == 0 && d != 0
                        && e % 2 == 0 && e != 0) {
                    finar = true;
                    winner = p2;
                    winningMoves = new byte[]{a, b, c, d, e};
                }
            }
        }
    }

    public String getCurrentTurn() {
        return currentMove % 2 == 1 ? p1 : p2;
    }

    public void shufflePlayers() {
        if (p2 != null) {
            if (random.nextFloat() < 0.5) {
                String temp = p1;
                p1 = p2;
                p2 = temp;
            }
        }
    }

    public void swapPlayers() {
        if (p2 != null) {
            String temp = p1;
            p1 = p2;
            p2 = temp;
        }
    }
}
