package com.deniz.bloomlishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "questions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Question {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "correct_answer", nullable = false)
    private String correct_answer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
