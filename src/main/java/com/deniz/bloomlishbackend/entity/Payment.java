package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "payments")
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "amount", nullable = false)
    private int amount;

    // Para birimi (şimdilik sabit TRY)
    @Column(name = "currency", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;


    @PrePersist
    public void prePersist() {
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
        if (currency == null) {
            currency = "TRY"; // Varsayılan para birimi
        }
        if (status == null) {
            status = PaymentStatus.PENDING; // Varsayılan ödeme durumu
        }
    }
}
