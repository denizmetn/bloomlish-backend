package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Comment;
import com.deniz.bloomlishbackend.entity.DailyNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {
    Page<DailyNote> findByUserId(Long userId, Pageable pageable);

}
