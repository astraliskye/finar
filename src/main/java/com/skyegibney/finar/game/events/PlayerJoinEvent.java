package com.skyegibney.finar.game.events;

import com.skyegibney.finar.notifications.messages.TimeControl;

public record PlayerJoinEvent(
        int gameId,
        String player,
        String opponent,
        int turn,
        String moves,
        TimeControl timeControl,
        int wins,
        int losses
) {
}
