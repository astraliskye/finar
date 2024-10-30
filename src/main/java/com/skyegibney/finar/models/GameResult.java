package com.skyegibney.finar.models;

import com.skyegibney.finar.core.game.ResultType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "game_results")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameResult {
    @Id
    private long id;

    @Column(nullable = false)
    private String player1;

    @Column(nullable = false)
    private String player2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultType result;

    @Column
    private String winner;
}
