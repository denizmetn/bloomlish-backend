package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByLessonAndStudent(Lesson lesson, User student);

    List<Enrollment> findByStudent(User student);
}
