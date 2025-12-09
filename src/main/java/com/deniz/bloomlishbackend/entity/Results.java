package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Results {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private Integer score;

    private Integer correct;

    private Integer wrong;

    private String level;
    private Level currentLevel;

    @Column(name = "taken_at")
    private LocalDateTime takenAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.takenAt == null) {
            this.takenAt = LocalDateTime.now();
        }
    }
}
