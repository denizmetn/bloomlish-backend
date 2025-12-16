package com.deniz.bloomlishbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class DailyWordQuestionDto {
    private Long sessionId;
    private Long wordId;
    private int order;
    private String sentence;
    private List<String> options;
}
