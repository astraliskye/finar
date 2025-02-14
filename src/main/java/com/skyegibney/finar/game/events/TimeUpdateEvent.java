package com.skyegibney.finar.game.events;

import java.util.UUID;

public record TimeUpdateEvent(long gameId, long player1Time, long player2Time) {}
