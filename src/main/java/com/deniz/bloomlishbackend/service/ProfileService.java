package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.UpdateProfileRequest;
import com.deniz.bloomlishbackend.dto.UpdateProfileResponse;
import com.deniz.bloomlishbackend.dto.UserProfileDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserProfileDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User yok"));

        return UserProfileDto.builder()
                .id(user.getUserID())
                .name(user.getDisplayName())
                .email(user.getEmail())
                .level(user.getCurrentLevel())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
    public UserProfileDto uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User yok"));

        try {
            Path dir = Paths.get("uploads/avatars");
            Files.createDirectories(dir);

            String filename = "user_" + user.getUserID() + ".png";
            Path target = dir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String url = "http://localhost:8080/uploads/avatars/" + filename;
            user.setProfileImageUrl(url);
            userRepository.save(user);

            return getMyProfile(email);

        } catch (Exception e) {
            throw new RuntimeException("Avatar yüklenemedi");
        }
    }
    public void removeAvatar(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User yok"));

        // diskten sil (opsiyonel ama doğru)
        if (user.getProfileImageUrl() != null) {
            try {
                Path path = Paths.get("uploads/avatars/user_" + user.getUserID() + ".png");
                Files.deleteIfExists(path);
            } catch (Exception e) {
                // logla ama kullanıcıyı bozma
            }
        }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }

    public UpdateProfileResponse updateMe(String currentEmail, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // email değişiyorsa unique kontrol
        String newEmail = req.email().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Bu email zaten kullanılıyor");
        }

        user.setEmail(newEmail);
        user.setUsername(req.username());

        // şifre boşsa değiştirme
        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 6) {
                throw new RuntimeException("Şifre en az 6 karakter olmalı");
            }
            user.setPassword(passwordEncoder.encode(req.password()));
        }

        userRepository.save(user);

        // subject=email olduğu için yeni token şart
        String newToken = jwtService.generateToken(user);

        return new UpdateProfileResponse(
                user.getUserID(),
                user.getEmail(),
                user.getDisplayName(), // veya user.getUsernameField() gibi
                newToken
        );
    }

    public UpdateProfileResponse me(String currentEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user); // istersen burada token dönme, optional
        return new UpdateProfileResponse(user.getUserID(), user.getEmail(), user.getDisplayName(), token);
    }
}