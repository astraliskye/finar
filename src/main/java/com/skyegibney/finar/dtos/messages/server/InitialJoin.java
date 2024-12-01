package com.skyegibney.finar.dtos.messages.server;

public record InitialJoin(
        int gameId,
        String player,
        String opponent,
        int turn,
        String moves,
        TimeControl timeControl
) {
}
