package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AuthResponse;
import com.deniz.bloomlishbackend.dto.LoginRequest;
import com.deniz.bloomlishbackend.dto.RegisterRequest;
import com.deniz.bloomlishbackend.dto.UserDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        String role = request.getRole();

        if (!role.equals("ROLE_ADMIN") &&
                !role.equals("ROLE_INSTRUCTOR") &&
                !role.equals("ROLE_STUDENT")) {

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
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserID())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();

    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(loginUser);
        return AuthResponse.builder()
                .token(token)
                .userId(loginUser.getUserID())
                .username(loginUser.getUsername())
                .email(loginUser.getEmail())
                .build();
    }

    public List<UserDto> getAllUsers() {
        List<User> userList = userRepository.findAll();

        // Şifre veya hassas alanları göndermiyoruz
        return userList.stream()
                .map(u -> new UserDto(u.getUserID(), u.getUsername(), u.getEmail()))
                .toList();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ile kullanıcı bulunamadı: " + email));
    }

    public List<UserDto> getAllStudents() {
        List<User> students = userRepository.findAll()
                .stream()
                .filter(u -> "ROLE_STUDENT".equals(u.getRole()))
                .toList();

        return students.stream()
                .map(u -> new UserDto(u.getUserID(), u.getUsername(), u.getEmail()))
                .toList();
    }


}
