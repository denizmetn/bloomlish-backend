package com.deniz.bloomlishbackend.dto;

import lombok.Data;

@Data
public class DailyWordResultDto {
    private boolean correct;
    private boolean completed;
    private String word;
    private String meaning;

    private int gainedXp;   // ⭐ EKLE
    private int totalXp;
}
