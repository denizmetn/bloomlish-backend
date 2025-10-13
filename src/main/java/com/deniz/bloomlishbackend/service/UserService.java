package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AuthResponse;
import com.deniz.bloomlishbackend.dto.LoginRequest;
import com.deniz.bloomlishbackend.dto.RegisterRequest;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Builder

public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthResponse register(RegisterRequest request) {

        String role = request.getRole() == null ? "" : request.getRole().toUpperCase();
        if(!"STUDENT".equals(role) && !"TEACHER".equals(role)){
            throw new IllegalArgumentException("Seçeneklerden birini seçiniz.");
        }
        User user= User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(role)
                .build();
        userRepository.save(user);
        return new AuthResponse(user);

    }

    public AuthResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("Email bulunamadı"));

        String requestedRole = loginRequest.getRole() == null ? "" : loginRequest.getRole();
        if (loginUser.getRole().equalsIgnoreCase(requestedRole)){
            throw new RuntimeException("Bu kullanıcı bu giriş tipine yetkili değil.");
        }


        return new AuthResponse(loginUser);
    }

}
