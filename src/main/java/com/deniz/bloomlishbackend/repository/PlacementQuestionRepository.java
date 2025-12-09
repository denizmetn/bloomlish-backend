package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Level;
import com.deniz.bloomlishbackend.entity.PlacementQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementQuestionRepository extends JpaRepository<PlacementQuestion, Long> {
    List<PlacementQuestion> findByLevel(Level level);
}
