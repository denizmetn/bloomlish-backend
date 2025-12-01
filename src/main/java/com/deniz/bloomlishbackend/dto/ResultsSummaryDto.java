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
public class ResultsSummaryDto {
    private Integer averageScore;
    private String averageLevel;
    private LastResultDto lastResult;
    private List<DailyStatsDto> dailyStats;
}
