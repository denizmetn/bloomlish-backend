package com.deniz.bloomlishbackend.dto;
public record UpdateProfileResponse(
        Long userId,
        String email,
        String username,
        String token
) {}