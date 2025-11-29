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
public class ListeningAudioGroupDto {
    private Long audioId;
    private String audioUrl;
    private String topic;
    private List<QuestionDto> questions;
}
