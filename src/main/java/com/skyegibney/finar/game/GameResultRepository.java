package com.skyegibney.finar.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/* record PlayerWins(
        String username,
        int wins
) {

} */

interface PlayerWins {
    String getUsername();
    Integer getWins();
}

@Repository
interface GameResultRepository extends JpaRepository<GameResult, Long> {
    @Query(
            value = "select winner as username, count(*) as wins "
                    + " from game_results "
                    + " where (player1 = ?1 or player1 = ?2) " +
                    " and (player2 = ?1 or player2 = ?2) "
                    + " group by winner"
    )
    List<PlayerWins> getUserVsUser(String player, String otherPlayer);
}
