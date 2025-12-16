package com.deniz.bloomlishbackend.dto;

import com.deniz.bloomlishbackend.entity.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private Level level;
    private String profileImageUrl;
}
