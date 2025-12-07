package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Results;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultsRepository extends JpaRepository<Results, Long> {
    List<Results> findByUserUserIDOrderByTakenAtAsc(Long userId);
    List<Results> findByUserEmail(String email);
}
