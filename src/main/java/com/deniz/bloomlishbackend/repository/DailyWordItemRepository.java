package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.DailyWordItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyWordItemRepository extends JpaRepository<DailyWordItem, Long> {

    List<DailyWordItem> findByRoundIdOrderByOrderIndex(Long roundId);
}
