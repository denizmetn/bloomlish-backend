package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository <Word,Long> {
    List<Word> findByLevel(String level);
}
