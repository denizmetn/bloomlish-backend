package com.deniz.bloomlishbackend.dto;

import java.time.LocalDateTime;

public record AdminEnrollmentRowDto(
        Long enrollmentId,
        Long userId,
        String userName,
        String userEmail,
        Long lessonId,
        String lessonName,
        double amount,
        String paymentStatus, // PAID / UNPAID (istersen enum yaparız)
        LocalDateTime date
) {}
