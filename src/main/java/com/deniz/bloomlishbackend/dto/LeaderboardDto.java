package com.deniz.bloomlishbackend.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardDto {
    private Long userID;
    private String displayName;
    private Integer weeklyXp;
}
