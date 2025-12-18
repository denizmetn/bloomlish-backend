package com.deniz.bloomlishbackend.dto;

public record UserStats(
        int vocabCorrect,
        int grammarCorrect,
        int listeningCorrect,
        long totalQuizzes,
        int totalScore
) {}