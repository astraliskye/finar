package com.skyegibney.finar.dtos.messages.client;

public record ClientMessage(
        int gameId,
        String type,
        Object data
) {
}
