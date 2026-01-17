package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public void enrollOneSeat(Long lessonId, User student) {

        Lesson lesson = lessonRepository.findByIdForUpdate(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND"));

        // ✅ ders dolu mu? (tek kişilik)
        if (enrollmentRepository.existsByLesson(lesson)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LESSON_FULL");
        }

        // (opsiyonel ama iyi)
        if (enrollmentRepository.existsByLessonAndStudent(lesson, student)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ALREADY_ENROLLED");
        }

        Enrollment e = Enrollment.builder()
                .lesson(lesson)
                .student(student)
                .paid(true) // sende ödeme akışına göre ayarla
                .enrolledAt(LocalDateTime.now())
                .build();

        try {
            enrollmentRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LESSON_FULL");
        }
    }
}

