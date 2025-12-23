package com.deniz.bloomlishbackend.dto;

import com.deniz.bloomlishbackend.entity.AccountStatus;
import com.deniz.bloomlishbackend.entity.Level;

import java.time.LocalDateTime;

public record AdminUserRowDto(
        Long userId,
        String username,
        String email,
        String role,
        Level currentLevel,
        int totalXp,
        int weeklyXp,
        AccountStatus accountStatus,
        boolean premium,
        LocalDateTime createdAt
) {}