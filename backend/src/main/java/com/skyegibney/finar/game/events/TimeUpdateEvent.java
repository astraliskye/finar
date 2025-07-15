package com.skyegibney.finar.game.events;

public record TimeUpdateEvent(long gameId, long player1Time, long player2Time) {}
