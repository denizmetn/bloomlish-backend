package com.deniz.bloomlishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListeningQuizResponse {
    private Long quizId;
    private String difficulty;
    private List<ListeningAudioGroupDto> audioGroups;
}
