package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.DailyWordSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyWordSessionRepository extends JpaRepository <DailyWordSession, Long> {
    Optional<DailyWordSession> findByUserIdAndDate(Long userId, LocalDate date);
}
