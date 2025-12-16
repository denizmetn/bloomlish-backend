package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByWordIgnoreCase(String word);

    boolean existsByWordIgnoreCase(String word);

    @Query(value = "SELECT * FROM uygulama.words ORDER BY random() LIMIT 1", nativeQuery = true)
    Word getRandomWord();
}
