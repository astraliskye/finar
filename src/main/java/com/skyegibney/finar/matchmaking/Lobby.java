package com.skyegibney.finar.matchmaking;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

record Lobby(
        int id,
        String owner,
        List<Player> players
) {
}

@AllArgsConstructor
@Getter
@Setter
class Player {
    String username;
    boolean ready;
}