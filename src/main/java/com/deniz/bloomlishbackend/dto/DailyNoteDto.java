package com.deniz.bloomlishbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyNoteDto {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
