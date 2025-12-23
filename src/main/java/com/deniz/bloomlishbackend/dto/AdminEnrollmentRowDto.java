package com.deniz.bloomlishbackend.dto;

import java.time.LocalDateTime;

public record AdminEnrollmentRowDto(
        Long id,
        String studentEmail,
        String studentName,
        String lessonName,
        double price,
        boolean paid,
        LocalDateTime enrolledAt
) {}
