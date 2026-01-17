package com.deniz.bloomlishbackend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enrollment_lesson", columnNames = {"lesson_id"})
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Hangi ders?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    //hangi öğrenci?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private boolean paid;

    private LocalDateTime enrolledAt;

}
