package com.skyegibney.finar.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
interface GameResultRepository extends JpaRepository<GameResult, Long> {
}
