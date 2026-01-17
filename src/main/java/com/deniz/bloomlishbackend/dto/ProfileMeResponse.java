package com.deniz.bloomlishbackend.dto;

import com.deniz.bloomlishbackend.entity.Level;

import java.util.List;

public record ProfileMeResponse(
        Long userId,
        String displayName,
        String email,
        Level currentLevel,
        String profileImageUrl,
        List<BadgeDto> badges,
        String aiTip,
        String role
) {}

