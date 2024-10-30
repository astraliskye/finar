package com.skyegibney.finar.dtos.messages.server;

public record Move(
        String player,
        int n,
        TimeControl timeControl
) {
}
