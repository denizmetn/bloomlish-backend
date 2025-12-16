package com.deniz.bloomlishbackend.dto;

import lombok.Data;

@Data
public class DailyWordAnswerRequest {
    private Long sessionId;
    private Long wordId;
    private String selected;
}
