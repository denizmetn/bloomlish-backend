package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name="start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name="end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name="active", nullable = false)
    private boolean active;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        if(createdAt == null)
        createdAt = LocalDateTime.now();
    }
    /*Bu method DB’ye ilk kez kayıt atılırken çalışır.
    Eğer createdAt elle set edilmediyse → current time atanır.
    Bu sayede hiçbir kayıt createdAt = null kalmaz. */

}
