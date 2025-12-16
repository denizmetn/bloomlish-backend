package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.DailyWordRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyWordRoundRepository extends JpaRepository<DailyWordRound, Long> {
    Optional<DailyWordRound> findTopBySessionIdOrderByRoundNumberDesc(Long sessionId);

    List<DailyWordRound> findBySessionIdOrderByRoundNumberAsc(Long sessionId);
}

