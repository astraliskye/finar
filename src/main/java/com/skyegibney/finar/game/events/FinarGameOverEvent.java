package com.skyegibney.finar.game.events;

public record FinarGameOverEvent(
        int gameId,
        String player1,
        String player2,
        String result,
        String winner,
        int[] winningMoves
) {
}
