package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AuthResponse;
import com.deniz.bloomlishbackend.dto.LoginRequest;
import com.deniz.bloomlishbackend.dto.RegisterRequest;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Builder
@Transactional
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService  jwtService;

    public AuthResponse register(RegisterRequest request) {

        String role = request.getRole() == null ? "" : request.getRole().toUpperCase();
        if (!"STUDENT".equals(role) && !"TEACHER".equals(role)) {
            throw new IllegalArgumentException("Seçeneklerden birini seçiniz.");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(role)
                .build();
        User savedUser = userRepository.save(user);
        System.out.println("Saved user ID: " + savedUser.getUserID());
       String token =jwtService.generateToken(user);
        return new AuthResponse(token);

    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email bulunamadı"));

        String token= jwtService.generateToken(loginUser);
        return new AuthResponse(token);
    }

}
