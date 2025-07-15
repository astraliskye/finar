package com.skyegibney.finar.notifications.messages;

public record FinarGameOver(
    String reason, String winner, byte cell1, byte cell2, byte cell3, byte cell4, byte cell5) {}
