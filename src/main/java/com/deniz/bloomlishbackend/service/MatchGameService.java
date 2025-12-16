package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.MatchWordDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchGameService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getRound(String level, int count) {
        List<MatchWordDto> all = loadAll();

        List<MatchWordDto> filtered = all.stream()
                .filter(x -> level == null || level.isBlank() || x.getLevel().equalsIgnoreCase(level))
                .collect(Collectors.toList());

        Collections.shuffle(filtered);

        List<MatchWordDto> picked = filtered.stream().limit(count).toList();

        // kelimeler ve anlamlar ayrı ayrı karışık gitsin
        List<String> words = picked.stream().map(MatchWordDto::getWord).collect(Collectors.toList());
        List<String> meanings = picked.stream().map(MatchWordDto::getMeaning).collect(Collectors.toList());
        Collections.shuffle(words);
        Collections.shuffle(meanings);

        // doğruları kontrol edebilmek için map dönüyoruz (frontend eşleşmeyi kontrol edecek)
        Map<String, String> correctPairs = picked.stream()
                .collect(Collectors.toMap(MatchWordDto::getWord, MatchWordDto::getMeaning));

        Map<String, Object> res = new HashMap<>();
        res.put("words", words);
        res.put("meanings", meanings);
        res.put("correctPairs", correctPairs);
        return res;
    }

    private List<MatchWordDto> loadAll() {
        try {
            ClassPathResource resource = new ClassPathResource("data/match-words.json");
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<List<MatchWordDto>>() {});
            }
        } catch (Exception e) {
            throw new RuntimeException("match-words.json okunamadı: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> completeRound(String level, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int xp = switch (level.toUpperCase()) {
            case "A1", "A2", "B1", "B2", "C1" -> 5;
            default -> 5;
        };

        user.setTotalXp(user.getTotalXp() + xp);
        user.setWeeklyXp(user.getWeeklyXp() + xp);
        userRepository.save(user);

        return Map.of(
                "xpGained", xp,
                "totalXp", user.getTotalXp(),
                "weeklyXp", user.getWeeklyXp()
        );
    }
}