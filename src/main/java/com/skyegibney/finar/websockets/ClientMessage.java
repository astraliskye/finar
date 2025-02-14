package com.skyegibney.finar.websockets;

import java.util.UUID;

public record ClientMessage(long gameId, int lobbyId, String type, Object data) {}
