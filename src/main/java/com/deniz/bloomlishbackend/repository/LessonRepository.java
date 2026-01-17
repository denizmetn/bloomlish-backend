package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByInstructor(User instructor);
    List<Lesson> findByInstructorOrderByCreatedAtDesc(User instructor);

}
