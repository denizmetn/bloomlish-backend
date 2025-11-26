package com.deniz.bloomlishbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDto {
    private Long id;
    @JsonProperty("question")
    private String content;
    private String type;
    private Integer limit;
    private String difficulty;
    private String hint;
    private String solutionExplanation;
    private Integer estimatedTimeSec;
    private boolean validated;
    private List<String> options;

    private Long quizId;
}
