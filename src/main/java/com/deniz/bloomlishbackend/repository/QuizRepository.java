package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz,Long> {
    Optional<Quiz> findFirstByQuizTypeAndDifficultyAndDuration(
            String quizType, String difficulty, Integer duration
    );
}
