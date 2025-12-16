package com.deniz.bloomlishbackend.dto;

import java.util.List;

public class RoundSummaryDto {
    private int roundNumber;
    private List<WordSummaryDto> words;

    public RoundSummaryDto(int roundNumber, List<WordSummaryDto> words) {
        this.roundNumber = roundNumber;
        this.words = words;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<WordSummaryDto> getWords() {
        return words;
    }
}
