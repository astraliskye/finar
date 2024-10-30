package com.skyegibney.finar.dtos.messages.server;

public record GameOver(
        String reason,
        String winner
) {
}
