package com.deniz.bloomlishbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 20) String username,
        String password // boş/ null gelirse değiştirme
) {}