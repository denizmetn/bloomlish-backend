package com.deniz.bloomlishbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class DailyWordGameResponse {

    private String type;

    // type = QUESTION
    private DailyWordQuestionDto question;

    // type = RESULT
    private DailyWordResultDto result;

    // type = SUMMARY
    private List<WordSummaryDto> summary;

    private List<RoundSummaryDto> rounds;

    private Integer gainedXp;
    private Integer totalXp;
}
