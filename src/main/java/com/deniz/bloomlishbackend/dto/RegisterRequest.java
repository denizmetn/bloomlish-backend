package com.deniz.bloomlishbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username alanını boş bırakmayınız.")
    private String username;
    @NotBlank(message = "Email alanını boş bırakmayınız.")
    private String email;
    @NotBlank(message = "Password alanını boş bırakmayınız.")
    @Size(min = 6 , max = 12 , message = "Password alanı en az 6 en fazla 12 karakter olabilir.")
    private String password;
    @NotBlank(message = "Role alanını boş bırakmayınız.")
    private String role;

}
