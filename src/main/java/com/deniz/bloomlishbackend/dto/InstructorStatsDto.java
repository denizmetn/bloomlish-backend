package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class InstructorStatsDto {
    private int totalStudents;
    private BigDecimal totalRevenue;
}
