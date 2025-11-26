package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResultsDto {
    private Long id;
    private Long userId;
    private Long quizId;

    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;
    private String level;
    private LocalDateTime takenAt;
}
