package com.skyegibney.finar.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

record Lobby(int id, String owner, List<Player> players) {}

@AllArgsConstructor
@Getter
@Setter
class Player {
  String username;
  boolean ready;
}
