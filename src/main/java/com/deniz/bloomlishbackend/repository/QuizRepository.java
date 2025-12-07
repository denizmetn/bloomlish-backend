package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
