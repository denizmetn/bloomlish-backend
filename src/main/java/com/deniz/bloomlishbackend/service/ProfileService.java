package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.UpdateProfileRequest;
import com.deniz.bloomlishbackend.dto.UpdateProfileResponse;
import com.deniz.bloomlishbackend.dto.UserProfileDto;
import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ResultsRepository resultsRepository;

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

        if (user.getProfileImageUrl() != null) {
            try {
                Path path = Paths.get("uploads/avatars/user_" + user.getUserID() + ".png");
                Files.deleteIfExists(path);
            } catch (Exception e) {
            }
        }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }

    public UpdateProfileResponse updateMe(String currentEmail, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newEmail = req.email().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Bu email zaten kullanılıyor");
        }

        user.setEmail(newEmail);
        user.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 6) {
                throw new RuntimeException("Şifre en az 6 karakter olmalı");
            }
            user.setPassword(passwordEncoder.encode(req.password()));
        }

        userRepository.save(user);
        String newToken = jwtService.generateToken(user);

        return new UpdateProfileResponse(
                user.getUserID(),
                user.getEmail(),
                user.getDisplayName(),
                newToken
        );
    }

    public UpdateProfileResponse me(String currentEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);
        return new UpdateProfileResponse(user.getUserID(), user.getEmail(), user.getDisplayName(), token);
    }
    public String buildAiTip(User user) {
        // Son 7 gün
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Results> recent = resultsRepository.findRecentResultsWithQuiz(user.getUserID(), from);

        if (recent.isEmpty()) {
            return "Henüz yeni başladın ✨ Bugün 1 kelime + 1 okuma testiyle seri başlatabilirsin.";
        }

        // Quiz type bazlı doğruluk: correct / (correct+wrong)
        Map<String, int[]> agg = new HashMap<>(); // type -> [correctSum, totalSum]
        int totalCorrect = 0, totalWrong = 0;

        for (Results r : recent) {
            String type = (r.getQuiz() != null && r.getQuiz().getQuizType() != null)
                    ? r.getQuiz().getQuizType()
                    : "genel";

            int c = r.getCorrect() != null ? r.getCorrect() : 0;
            int w = r.getWrong() != null ? r.getWrong() : 0;

            totalCorrect += c;
            totalWrong += w;

            agg.putIfAbsent(type, new int[]{0, 0});
            agg.get(type)[0] += c;
            agg.get(type)[1] += (c + w);
        }

        // En güçlü ve en zayıf tipi bul
        String weakestType = null;
        double weakestAcc = 2.0;

        String strongestType = null;
        double strongestAcc = -1.0;

        for (var e : agg.entrySet()) {
            int correctSum = e.getValue()[0];
            int totalSum = e.getValue()[1];
            if (totalSum == 0) continue;

            double acc = (double) correctSum / totalSum;

            if (acc < weakestAcc) {
                weakestAcc = acc;
                weakestType = e.getKey();
            }
            if (acc > strongestAcc) {
                strongestAcc = acc;
                strongestType = e.getKey();
            }
        }

        double overallAcc = (totalCorrect + totalWrong) == 0
                ? 0
                : (double) totalCorrect / (totalCorrect + totalWrong);

        // Basit ama “AI gibi” kural seti
        if (overallAcc < 0.45) {
            return "Bu hafta biraz zorlanmışsın. Zorluk seviyesini 1 kademe düşürüp kısa testlerle tekrar güçlenelim 💪";
        }

        if (weakestType != null && weakestAcc < 0.55) {
            return String.format(
                    "%s tarafın çok iyi gidiyor! Bu hafta %s pratiğine günde 10 dk ekleyerek daha hızlı ilerlersin ✨",
                    prettifyType(strongestType),
                    prettifyType(weakestType)
            );
        }

        return String.format(
                "Harika gidiyorsun! Bu hafta seviyeni korumak için %s + %s karışık 2 kısa test öneriyorum ✅",
                prettifyType(strongestType),
                (weakestType != null ? prettifyType(weakestType) : "kelime")
        );
    }

    private String prettifyType(String t) {
        if (t == null) return "Genel";
        return switch (t.toLowerCase()) {
            case "dinleme" -> "Dinleme";
            case "okuma" -> "Okuma";
            case "kelime" -> "Kelime";
            case "dilbilgisi" -> "Dilbilgisi";
            default -> t.substring(0, 1).toUpperCase() + t.substring(1);
        };
    }
}