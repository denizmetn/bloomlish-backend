package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizDto {
    private Long id;
    private String quizType;
    private String difficulty;
    private Integer duration;
    private LocalDateTime createdAt;

    private List<QuestionDto> questions;
    private List<QuizResultsDto> results;
}
