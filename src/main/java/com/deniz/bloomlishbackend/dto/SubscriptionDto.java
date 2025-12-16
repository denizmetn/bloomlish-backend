package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionDto {
    // Kullanıcının aktif aboneliği var mı?
    private boolean hasActiveSubscription;

    // Varsa hangi plan?
    private String planType;   // "MONTHLY" / "YEARLY"

    // Ne zamana kadar geçerli?

    private LocalDateTime expiresAt;

    // Ekranda gösterilecek özellik listesi
    private List<String> features;
}
