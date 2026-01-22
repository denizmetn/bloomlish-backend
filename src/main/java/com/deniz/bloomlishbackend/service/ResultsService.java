package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.DailyStatsDto;
import com.deniz.bloomlishbackend.dto.LastResultDto;
import com.deniz.bloomlishbackend.dto.ResultsSummaryDto;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.QuizRepository;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultsService {
    private final ResultsRepository resultsRepository;
    private final QuizRepository quizRepository;

    public ResultsSummaryDto getSummaryForUser(User user){
        log.info(" getSummmaryForUser çağrıldı. userID = {}", user.getUserID());
        List<Results> results= resultsRepository.findByUserUserIDOrderByTakenAtAsc(user.getUserID());

        log.info(" DB'den gelen sonuç sayısı = {}", results.size());

        if(results.isEmpty()){
            log.info(" Kullanıcının hiç sonucu yok, boş summary dönüyoruz.");
            return ResultsSummaryDto.builder()
                    .averageScore(0)
                    .averageLevel("Bilinmiyor")
                    .lastResult(null)
                    .dailyStats(Collections.emptyList())
                    .build();
        }
        double avgScore= results.stream()
                .map(Results:: getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        int averageScore=(int) Math.round(avgScore);
        // 2) Ortalama seviye (Quiz difficulty → sayısal)
        double avgDifficulty = results.stream()
                .map(r -> r.getQuiz().getDifficulty())  // Enum veya String varsaydım
                .filter(Objects::nonNull)
                .mapToInt(this::difficultyToNumber)
                .average()
                .orElse(0.0);

        String averageLevel = difficultyAverageToLabel(avgDifficulty);

        // 3) Son sonuç
        Results last = results.get(results.size() - 1);
        log.info(" Son sonuç -> score={}, level={}, correct={}, wrong={}, takenAt={}",
                last.getScore(), last.getLevel(), last.getCorrect(), last.getWrong(), last.getTakenAt());

        LastResultDto lastResultDto = LastResultDto.builder()
                .score(last.getScore())
                .level(last.getLevel())
                .correct(last.getCorrect())
                .wrong(last.getWrong())
                .takenAt(last.getTakenAt())
                .build();

        // 4) Günlük istatistikler (date → correct / wrong toplamı)
        Map<LocalDate, DailyStatsDto> dailyMap = new TreeMap<>();

        for (Results r : results) {
            LocalDate day = r.getTakenAt().toLocalDate();

            DailyStatsDto stats = dailyMap.get(day);
            if (stats == null) {
                stats = DailyStatsDto.builder()
                        .date(day)
                        .correct(0)
                        .wrong(0)
                        .build();
            }

            stats.setCorrect(stats.getCorrect() + (r.getCorrect() != null ? r.getCorrect() : 0));
            stats.setWrong(stats.getWrong() + (r.getWrong() != null ? r.getWrong() : 0));

            dailyMap.put(day, stats);
        }

        List<DailyStatsDto> dailyStats = new ArrayList<>(dailyMap.values());

        return ResultsSummaryDto.builder()
                .averageScore(averageScore)
                .averageLevel(averageLevel)
                .lastResult(lastResultDto)
                .dailyStats(dailyStats)
                .build();
    }

    private int difficultyToNumber(Object difficulty) {
        if (difficulty == null) return 2;

        String value = difficulty.toString().toLowerCase();  // önemli!

        return switch (value) {
            case "easy", "a1-a2" -> 1;
            case "medium", "b1-b2" -> 2;
            case "hard", "c1-c2" -> 3;
            default -> 2;
        };
    }

    private String difficultyAverageToLabel(double avg) {
        if (avg == 0.0) {
            return "Bilinmiyor";
        }
        if (avg < 1.5) return "Kolay";
        if (avg < 2.5) return "Orta";
        return "Zor";
    }
    public Results saveResult(User user,
                              Long quizId,
                              Integer score,
                              Integer correct,
                              Integer wrong,
                              String level) {

        System.out.println("saveResult çağrıldı. Gelen quizId = " + quizId);

        Quiz quiz = null;

        if (quizId != null) {
            quiz = quizRepository.findById(quizId).orElse(null);
        }

        // 2) Eğer quizId null ise VEYA DB'de böyle bir quiz yoksa, yeni Quiz oluştur
        if (quiz == null) {
            System.out.println(" quizId null veya DB'de yok, yeni Quiz oluşturuluyor.");
            quiz = createQuizFromLevel(level);
        }

        Results result = Results.builder()
                .user(user)
                .quiz(quiz)
                .score(score)
                .correct(correct)
                .wrong(wrong)
                .level(level)
                .currentLevel(user.getCurrentLevel())
                .takenAt(LocalDateTime.now())
                .build();

        return resultsRepository.save(result);
    }

    private Quiz createQuizFromLevel(String level) {
        String difficulty = mapLevelToDifficulty(level);

        Quiz quiz = Quiz.builder()
                .quizType("GENERIC")
                .difficulty(difficulty)
                .duration(0)
                .createdAt(LocalDateTime.now())
                .build();

        return quizRepository.save(quiz);
    }

    private String mapLevelToDifficulty(String level) {
        if (level == null) return "medium";

        String val = level.toLowerCase();
        return switch (val) {
            case "başlangıç", "baslangic" -> "easy";
            case "orta" -> "medium";
            case "ileri" -> "hard";
            default -> "medium";
        };
    }

}



