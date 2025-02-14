package com.skyegibney.finar.game.events;

import java.util.UUID;

public record MoveMadeEvent(long gameId, String player, int n, long player1Time, long player2Time) {}
