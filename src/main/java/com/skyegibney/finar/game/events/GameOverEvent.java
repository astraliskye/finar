package com.skyegibney.finar.game.events;

public record GameOverEvent(
    long gameId, String player1, String player2, String result, String winner) {}
