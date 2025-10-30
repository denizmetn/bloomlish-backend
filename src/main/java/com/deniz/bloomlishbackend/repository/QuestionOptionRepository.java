package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption,Long> {
}
