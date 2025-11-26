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
public class CheckoutResponse {
    private Long subscriptionId;

    private String planType;   // "MONTHLY" / "YEARLY"
    private int amount;        // 200 / 2000
    private String currency;   // "TRY"

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String status;     // "SUCCESS"
    private String message;
}
