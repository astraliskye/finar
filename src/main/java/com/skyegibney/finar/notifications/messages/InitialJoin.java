package com.skyegibney.finar.notifications.messages;

public record InitialJoin(
        int gameId,
        String player,
        String opponent,
        int turn,
        String moves,
        TimeControl timeControl
) {
}
