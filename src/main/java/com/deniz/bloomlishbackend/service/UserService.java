package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.*;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService  jwtService;

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
        String token =jwtService.generateToken(user);
        return  AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserID())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();

    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authenticate=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        User loginUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email bulunamadı"));

        String token= jwtService.generateToken(loginUser);
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


}
