package com.deniz.bloomlishbackend.game.dailyword;

import lombok.Data;

import java.util.List;

@Data
public class DailyWordResponse {
    private String word;
    private String level;

    private String phonetic;
    private List<String> meanings;
    private List<String> examples;

    private List<String > synonyms;
    private List<String> antonyms;

}
