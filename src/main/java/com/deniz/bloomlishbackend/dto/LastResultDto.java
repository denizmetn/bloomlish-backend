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
public class LastResultDto {
    private Integer score;
    private String level;
    private Integer correct;
    private Integer wrong;
    private LocalDateTime takenAt;
}
