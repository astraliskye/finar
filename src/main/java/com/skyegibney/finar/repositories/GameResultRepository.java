package com.skyegibney.finar.repositories;

import com.skyegibney.finar.models.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
}
