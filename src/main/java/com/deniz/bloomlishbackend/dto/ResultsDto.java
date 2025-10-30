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
public class ResultsDto {
    private Long id;
    private Long userId;
    private Long quizId;

    private Integer score;
    private Integer correct;
    private Integer wrong;
    private String level;
    private LocalDateTime takenAt;
}
