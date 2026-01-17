package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.LessonFeedback;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonFeedbackRepository extends JpaRepository<LessonFeedback, Long> {

    Optional<LessonFeedback> findByLessonAndStudent(Lesson lesson, User student);

    boolean existsByLessonAndStudent(Lesson lesson, User student);

    List<LessonFeedback> findByInstructorOrderByCreatedAtDesc(User instructor);
}
