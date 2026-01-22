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

    // Sorunun tipi
    @Column(nullable = true)
    private String type;

    @Enumerated(EnumType.STRING)
    private Level level;

    @Column(nullable = true)
    private String difficulty;

    // Soruya ipucu
    @Column(nullable = true)
    private String hint;

    // Çözüm açıklaması
    @Column(nullable = true, columnDefinition = "TEXT")
    private String solutionExplanation;

    // Tahmini süre
    @Column(nullable = true)
    private Integer estimatedTimeSec;

    // Admin onayı veya dış API’den gelen sorular için default true
    @Column(nullable = false)
    private boolean validated = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    private Integer questionCount;

    // Kolayca API’den gelen soruları map etmek için yardımcı metod
    public void addOption(QuestionOption option) {
        option.setQuestion(this);
        this.options.add(option);
    }
}
