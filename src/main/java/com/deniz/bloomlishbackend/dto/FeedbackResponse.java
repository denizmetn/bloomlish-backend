package com.deniz.bloomlishbackend.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long lessonId,
        String lessonName,
        String studentName,
        LocalDateTime createdAt,
        Integer rating,
        String comment
) {}
