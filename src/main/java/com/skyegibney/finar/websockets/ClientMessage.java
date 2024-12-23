package com.skyegibney.finar.websockets;

public record ClientMessage(
        int gameId,
        String type,
        Object data
) {
}
