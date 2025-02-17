package com.skyegibney.finar.websockets;

public record ClientMessage(String gameId, int lobbyId, String type, Object data) {}
