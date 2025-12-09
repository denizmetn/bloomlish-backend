package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizStartResponse {
    private Long quizId;
    private String testType;
    private String difficulty;
    private Integer limit;
    private List<QuestionDto> questions;
}