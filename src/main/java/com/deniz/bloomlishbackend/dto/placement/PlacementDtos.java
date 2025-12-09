package com.deniz.bloomlishbackend.dto.placement;

import com.deniz.bloomlishbackend.entity.Level;
import lombok.*;

import java.util.List;
import java.util.Map;


public class PlacementDtos {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlacementQuestionDto {
        private Long id;
        private String question;       // soru metni
        private List<String> options;  // A, B, C, D
        private Level level;           // sorunun seviyesi (A1, A2, B1...)
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlacementStartDto {

        // Kullanıcının geçmişine göre tahmini başlangıç seviyesi (ör: A2, B1)
        private Level estimatedLevel;

        // Frontend'e gösterilecek sorular
        private List<PlacementQuestionDto> questions;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlacementResultDto {
        private String finalLevel; // Örn: "B1"

        // örn: "A2" -> 3 doğru, "B1" -> 5 doğru
        private Map<String, Integer> correctPerLevel;

        // örn: "A2" -> 5 soru, "B1" -> 5 soru
        private Map<String, Integer> totalPerLevel;

        private int totalCorrect;
        private int totalWrong;
        private int score; // % olarak: (totalCorrect / totalQuestions) * 100
        private Integer totalQuestions;
        private Double overallCorrectRate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PlacementSubmitDto {
        // questionId -> selectedAnswer
        private Map<Long, String> answers;
    }
    @Data
    public static class PlacementQuestionJson {
        private String questionText;
        private List<String> options;
        private String correctOption; // "A", "B", "C", "D"
        private String cefrLevel;     // "A1", "A2", "B1", ...
        private String skill;         // şimdilik kullanmıyoruz ama JSON’da dursa da olur
    }
}