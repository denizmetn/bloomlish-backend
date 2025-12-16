package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordSummaryDto {
    private String word;
    private String meaning;
}