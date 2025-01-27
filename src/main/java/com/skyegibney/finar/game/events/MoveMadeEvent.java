package com.skyegibney.finar.game.events;

public record MoveMadeEvent(int gameId, String player, int n, long player1Time, long player2Time) {}
