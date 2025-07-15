package com.skyegibney.finar.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

interface PlayerRecord {
  Integer getWins();

  Integer getDraws();

  Integer getLosses();
}

@Repository
interface GameResultRepository extends JpaRepository<GameResult, Long> {
  @Query(
      value =
          "select coalesce(sum(case when winner = ?1 then 1 else 0 end), 0) wins, "
              + " coalesce(sum(case when winner = ?2 then 1 else 0 end), 0) losses,"
              + " coalesce(sum(case when result = 'DRAW' then 1 else 0 end), 0) draws "
              + " from game_results "
              + " where (player1 = ?1 or player1 = ?2) "
              + " and (player2 = ?1 or player2 = ?2) ")
  PlayerRecord getUserRecordVsOpponent(String player, String opponent);
}
