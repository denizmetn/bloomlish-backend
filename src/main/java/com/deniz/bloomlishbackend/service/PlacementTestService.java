package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.placement.PlacementDtos;
import com.deniz.bloomlishbackend.entity.Level;
import com.deniz.bloomlishbackend.entity.PlacementQuestion;
import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.PlacementQuestionRepository;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacementTestService {
    private final UserRepository userRepository;
    private final ResultsRepository resultsRepository;
    private final PlacementQuestionRepository placementQuestionRepository;

    // ✔ 1) Testi başlat
    public PlacementDtos.PlacementStartDto startPlacementTest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));

        // Kullanıcının eski sonuçlarına bak
        List<Results> userResults = resultsRepository.findByUserEmail(email);

        Level estimatedLevel = estimateLevelFromHistory(userResults);

        // Bu seviyeye göre soruları seç
        List<PlacementQuestion> questions = selectQuestionsForLevel(estimatedLevel, 10);

        List<PlacementDtos.PlacementQuestionDto> questionDtos = questions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PlacementDtos.PlacementStartDto.builder()
                .estimatedLevel(estimatedLevel)
                .questions(questionDtos)
                .build();
    }

    public PlacementDtos.PlacementResultDto submitPlacementTest(String email, PlacementDtos.PlacementSubmitDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new RuntimeException("Cevap listesi boş olamaz");
        }

        Map<Long, String> answers = request.getAnswers();
        List<Long> questionIds = new ArrayList<>(answers.keySet());
        // Soruları DB'den çek
        Map<Long, PlacementQuestion> questionMap = placementQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(PlacementQuestion::getId, q -> q));

        int totalCorrect = 0;
        int totalQuestions = request.getAnswers().size();

        // Level bazlı istatistik tutalım
        Map<Level, Integer> levelTotalMap = new EnumMap<>(Level.class);
        Map<Level, Integer> levelCorrectMap = new EnumMap<>(Level.class);

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long qId = entry.getKey();
            String selected = entry.getValue();

            PlacementQuestion q = questionMap.get(qId);
            if (q == null) continue;

            Level level = q.getLevel();
            levelTotalMap.put(level, levelTotalMap.getOrDefault(level, 0) + 1);

            boolean isCorrect = q.getCorrectAnswer().equals(selected);
            if (isCorrect) {
                totalCorrect++;
                levelCorrectMap.put(level, levelCorrectMap.getOrDefault(level, 0) + 1);
            }
        }

        int totalWrong = totalQuestions - totalCorrect;
        int score = totalQuestions == 0 ? 0 : (int) Math.round((totalCorrect * 100.0) / totalQuestions);

        // 🔍 Level bazlı istatistikleri String map'lere dönüştür (DTO öyle istiyor çünkü)
        Map<String, Integer> correctPerLevel = new HashMap<>();
        Map<String, Integer> totalPerLevel = new HashMap<>();

        for (Level level : Level.values()) {
            int lvlTotal = levelTotalMap.getOrDefault(level, 0);
            int lvlCorrect = levelCorrectMap.getOrDefault(level, 0);

            if (lvlTotal > 0) {
                String key = level.name(); // "A1", "A2", "B1"...
                correctPerLevel.put(key, lvlCorrect);
                totalPerLevel.put(key, lvlTotal);
            }
        }

        // ✅ Nihai seviye (basit algoritma: yüksek orana sahip en yüksek level)
        Level finalLevelEnum = calculateFinalLevel(levelCorrectMap, levelTotalMap, score / 100.0);
        String finalLevel = finalLevelEnum.name();

        // Kullanıcının güncel seviyesini update et (istersen)
        user.setCurrentLevel(finalLevelEnum);
        userRepository.save(user);

        return PlacementDtos.PlacementResultDto.builder()
                .finalLevel(finalLevel)
                .correctPerLevel(correctPerLevel)
                .totalPerLevel(totalPerLevel)
                .totalCorrect(totalCorrect)
                .totalWrong(totalWrong)
                .totalQuestions(totalQuestions)
                .overallCorrectRate(totalQuestions == 0 ? 0 : (totalCorrect * 1.0 / totalQuestions))
                .score(score)
                .build();
    }


    // ✔ Tahmini level (geçmiş sonuçlardan)
    // ✔ Tahmini level (geçmiş sonuçlardan)
    private Level estimateLevelFromHistory(List<Results> results) {
        if (results == null || results.isEmpty()) {
            // Hiç sonuç yoksa ortadan başlayalım
            return Level.A2;
        }

        Map<Level, List<Integer>> scoreByLevel = new EnumMap<>(Level.class);

        for (Results r : results) {
            // 1) Önce Enum olan currentLevel'ı dene
            Level level = r.getCurrentLevel();

            // 2) Eğer o null'sa, String level alanından dönüştürmeyi dene
            if (level == null) {
                String str = r.getLevel(); // "A1", "B2" vs eski kayıtlar
                if (str == null) {
                    continue; // hiç level bilgisi yoksa bu sonucu skip et
                }
                try {
                    level = Level.valueOf(str.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Geçersiz değer ise yine skip
                    continue;
                }
            }

            // 3) Skor null ise yine atlatalım, yoksa ortalama hesaplarken patlar
            Integer score = r.getScore();
            if (score == null) {
                continue;
            }

            scoreByLevel
                    .computeIfAbsent(level, l -> new ArrayList<>())
                    .add(score);
        }

        // Hiç düzgün veri toplanamadıysa varsayılan seviye
        if (scoreByLevel.isEmpty()) {
            return Level.A2;
        }

        Level bestLevel = Level.A1;
        double bestAverage = 0.0;

        for (Map.Entry<Level, List<Integer>> entry : scoreByLevel.entrySet()) {
            List<Integer> scores = entry.getValue();
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            if (avg > bestAverage) {
                bestAverage = avg;
                bestLevel = entry.getKey();
            }
        }

        // Örn: eğer ortalama çok yüksekse (>= 80) bir üst seviyeye zıplat
        if (bestAverage >= 80) {
            return promoteLevel(bestLevel);
        }
        // çok düşükse bir alt seviyeye indir
        if (bestAverage < 50) {
            return demoteLevel(bestLevel);
        }

        return bestLevel;
    }

    private Level promoteLevel(Level current) {
        return switch (current) {
            case A1 -> Level.A2;
            case A2 -> Level.B1;
            case B1 -> Level.B2;
            case B2 -> Level.C1;
            case C1 -> Level.C2;
            case C2 -> Level.C2;

        };
    }

    private Level demoteLevel(Level current) {
        return switch (current) {
            case A1 -> Level.A1;
            case A2 -> Level.A1;
            case B1 -> Level.A2;
            case B2 -> Level.B1;
            case C1 -> Level.B2;
            case C2 -> Level.C1;
        };
    }

    // Seçilecek sorular
    private List<PlacementQuestion> selectQuestionsForLevel(Level level, int count) {
        // Basit versiyon: sadece o level’dan çek
        List<PlacementQuestion> all = placementQuestionRepository.findByLevel(level);
        Collections.shuffle(all);
        if (all.size() <= count) return all;
        return all.subList(0, count);
    }

    private PlacementDtos.PlacementQuestionDto toDto(PlacementQuestion q) {
        return PlacementDtos.PlacementQuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .options(q.getOptions())
                .level(q.getLevel())
                .build();
    }

    private Level calculateFinalLevel(Map<Level, Integer> levelCorrectMap,
                                      Map<Level, Integer> levelTotalMap,
                                      double overallRate) {

        Level chosen = Level.A1;

        for (Level level : Level.values()) {
            int total = levelTotalMap.getOrDefault(level, 0);
            int correct = levelCorrectMap.getOrDefault(level, 0);

            if (total == 0) continue;

            double rate = (double) correct / total;

            if (rate >= 0.7) { // %70 ve üstü ise o level'ı aday seç
                chosen = level;
            }
        }

        // Genel oran çok kötüyse biraz düşür
        if (overallRate < 0.4) {
            chosen = demoteLevel(chosen);
        }

        // Çok iyiyse bir tık yükselt
        if (overallRate >= 0.85) {
            chosen = promoteLevel(chosen);
        }

        return chosen;
    }

}
