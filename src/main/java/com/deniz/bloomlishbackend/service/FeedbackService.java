package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.FeedbackCreateRequest;
import com.deniz.bloomlishbackend.dto.FeedbackResponse;
import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.Lesson;
import com.deniz.bloomlishbackend.entity.LessonFeedback;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.LessonFeedbackRepository;
import com.deniz.bloomlishbackend.repository.LessonRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackResponse create(String studentEmail, FeedbackCreateRequest req) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User yok"));

        Lesson lesson = lessonRepository.findById(req.lessonId())
                .orElseThrow(() -> new RuntimeException("Lesson yok"));

        // 1) öğrenci bu derse kayıtlı mı + paid mi?
        Enrollment enrollment = enrollmentRepository.findByLessonAndStudent(lesson, student)
                .orElseThrow(() -> new RuntimeException("Bu derse kayıtlı değilsiniz"));

        if (!enrollment.isPaid()) {
            throw new RuntimeException("Ödeme yapılmamış");
        }

        // 2) ders bitmiş mi? (en kolayı: bitiş anı < now)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lessonEnd = lesson.getDate().atTime(lesson.getEndTime()); // date=LocalDate, endTime=LocalTime varsayımı

        if (now.isBefore(lessonEnd)) {
            throw new RuntimeException("Ders bitmeden geri bildirim veremezsiniz");
        }

        // 3) aynı derse 1 kez
        if (feedbackRepository.existsByLessonAndStudent(lesson, student)) {
            throw new RuntimeException("Bu ders için zaten geri bildirim verdiniz");
        }

        // 4) validation
        int rating = req.rating() == null ? 5 : req.rating();
        if (rating < 1 || rating > 5) throw new RuntimeException("Puan 1-5 arasında olmalı");
        if (req.comment() == null || req.comment().trim().isEmpty()) throw new RuntimeException("Yorum boş olamaz");

        LessonFeedback saved = feedbackRepository.save(
                LessonFeedback.builder()
                        .lesson(lesson)
                        .student(student)
                        .instructor(lesson.getInstructor())
                        .rating(rating)
                        .comment(req.comment().trim())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return toResponse(saved);
    }

    public List<FeedbackResponse> getForInstructor(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("User yok"));

        return feedbackRepository.findByInstructorOrderByCreatedAtDesc(instructor)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean isSubmitted(String studentEmail, Long lessonId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User yok"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson yok"));

        return feedbackRepository.existsByLessonAndStudent(lesson, student);
    }

    private FeedbackResponse toResponse(LessonFeedback f) {
        return new FeedbackResponse(
                f.getId(),
                f.getLesson().getId(),
                f.getLesson().getName(),
                // displayName varsa onu gösterelim, yoksa username
                (f.getStudent().getDisplayName() != null ? f.getStudent().getDisplayName() : f.getStudent().getUsername()),
                f.getCreatedAt(),
                f.getRating(),
                f.getComment()
        );
    }
}
