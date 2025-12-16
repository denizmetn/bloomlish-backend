package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class DailyWordRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DailyWordSession session;

    private int roundNumber;

    @Column(nullable = false)
    private boolean completed = false;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    private List<DailyWordItem> items;

    private LocalDateTime createdAt = LocalDateTime.now();
}
