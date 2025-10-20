package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.DailyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {
}
