package com.deniz.bloomlishbackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentHistoryItemDto {
    private LocalDate date;
    private String plan;
    private int amount;
    private String currency;
    private String status;
}

