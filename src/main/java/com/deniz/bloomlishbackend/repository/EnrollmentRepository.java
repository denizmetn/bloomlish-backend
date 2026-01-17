package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByLessonAndStudent(Lesson lesson, User student);
    Optional<Enrollment> findByLessonAndStudent(Lesson lesson, User student);

    boolean existsByLesson(Lesson lesson);

    List<Enrollment> findByStudent(User student);
    List<Enrollment> findByLesson(Lesson lesson);
}
