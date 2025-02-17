package com.skyegibney.finar.websockets.events;

public record GameChatEvent(long gameId, String username, String message) {}
