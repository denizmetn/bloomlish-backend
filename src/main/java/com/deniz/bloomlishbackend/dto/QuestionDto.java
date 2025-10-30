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
public class QuestionDto {
    private Long id;
    private String content;
    private String correctAnswer;
    private List<QuestionOptionDto> options;
    private Long quizId;
}
