package com.skyegibney.finar.websockets;

public record ClientMessage(
        int gameId,
        int lobbyId,
        String type,
        Object data
) {
}
