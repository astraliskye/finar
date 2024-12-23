package com.skyegibney.finar.game.events;

public record TimeUpdateEvent(
        int gameId,
        long player1Time,
        long player2Time
) {
}
