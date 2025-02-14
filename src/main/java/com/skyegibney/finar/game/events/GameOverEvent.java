package com.skyegibney.finar.game.events;

import java.util.UUID;

public record GameOverEvent(
    long gameId, String player1, String player2, String result, String winner) {}
