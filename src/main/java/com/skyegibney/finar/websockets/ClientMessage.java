package com.skyegibney.finar.websockets;

public record ClientMessage(long gameId, int lobbyId, String type, Object data) {}
