package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaymentRowDto {
    private Long id;
    private String user;      // email
    private String plan;      // planType
    private int amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt; // paidAt
}
