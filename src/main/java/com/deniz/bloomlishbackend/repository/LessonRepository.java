package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByInstructor(User instructor);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lesson l where l.id = :id")
    Optional<Lesson> findByIdForUpdate(@Param("id") Long id);
    @Query("""
        select l from Lesson l
        where not exists (
            select 1 from Enrollment e where e.lesson = l
        )
    """)
    List<Lesson> findAllAvailable();
    List<Lesson> findByInstructorOrderByCreatedAtDesc(User instructor);
}
