package com.skyegibney.finar.notifications.messages;

import java.util.UUID;

public record InitialJoin(
    String gameId,
    String player,
    String opponent,
    int turn,
    String moves,
    TimeControl timeControl,
    int wins,
    int draws,
    int losses) {}
