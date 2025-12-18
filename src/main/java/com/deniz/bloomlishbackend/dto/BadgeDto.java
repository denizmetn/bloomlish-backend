package com.deniz.bloomlishbackend.dto;

public record BadgeDto(
        String id,
        String title,
        String description,
        int progress,
        int threshold,
        boolean earned
) {}
