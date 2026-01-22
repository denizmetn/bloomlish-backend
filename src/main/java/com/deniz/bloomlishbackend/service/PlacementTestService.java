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

    public PlacementDtos.PlacementStartDto startPlacementTest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));

        // Eğer kullanıcı daha önce placement ile seviyesini aldıysa onu kullan
        Level estimatedLevel = user.getCurrentLevel();

        // Eğer currentLevel yoksa geçmiş quiz sonuçlarından tahmin et
        if (estimatedLevel == null) {
            List<Results> userResults = resultsRepository.findByUserEmail(email);
            estimatedLevel = estimateLevelFromHistory(userResults);
        }

        // 3) Bu seviyeye göre soruları seç
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

        Map<Long, PlacementQuestion> questionMap = placementQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(PlacementQuestion::getId, q -> q));

        int totalCorrect = 0;
        int totalQuestions = request.getAnswers().size();

        // Level bazlı istatistik tut
        Map<Level, Integer> levelTotalMap = new EnumMap<>(Level.class);
        Map<Level, Integer> levelCorrectMap = new EnumMap<>(Level.class);
        List<PlacementDtos.PlacementQuestionResultDto> questionResults = new ArrayList<>();
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long qId = entry.getKey();
            String selected = entry.getValue();

            PlacementQuestion q = questionMap.get(qId);
            if (q == null) continue;
            Level level = q.getLevel();
            levelTotalMap.put(level, levelTotalMap.getOrDefault(level, 0) + 1);

            String correctLetter = q.getCorrectAnswer();
            String correctText = correctLetter;

            List<String> options = q.getOptions();
            if (correctLetter != null && correctLetter.matches("[ABCD]") && options != null) {
                int idx = correctLetter.charAt(0) - 'A';
                if (idx >= 0 && idx < options.size()) {
                    correctText = options.get(idx);
                }
            }

            boolean isCorrect = Objects.equals(correctText, selected);

            questionResults.add(
                    PlacementDtos.PlacementQuestionResultDto.builder()
                            .questionId(qId)
                            .question(q.getQuestion())
                            .selectedAnswer(selected)
                            .correctAnswer(correctText)
                            .correct(isCorrect)
                            .level(level.name())
                            .build()
            );

            if (isCorrect) {
                totalCorrect++;
                levelCorrectMap.put(level, levelCorrectMap.getOrDefault(level, 0) + 1);
            }
        }
        int totalWrong = totalQuestions - totalCorrect;
        int score = totalQuestions == 0 ? 0 : (int) Math.round((totalCorrect * 100.0) / totalQuestions);

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

        Level finalLevelEnum =
                calculateFinalLevel(user.getCurrentLevel(), score / 100.0);

        String finalLevel = finalLevelEnum.name();


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
                .questionResults(questionResults)
                .build();
    }


    private Level estimateLevelFromHistory(List<Results> results) {
        if (results == null || results.isEmpty()) {
            return Level.A2;
        }

        Map<Level, List<Integer>> scoreByLevel = new EnumMap<>(Level.class);

        for (Results r : results) {
            Level level = r.getCurrentLevel();

            if (level == null) {
                String str = r.getLevel();
                if (str == null) {
                    continue;
                }
                try {
                    level = Level.valueOf(str.toUpperCase());
                } catch (IllegalArgumentException e) {

                    continue;
                }
            }

            Integer score = r.getScore();
            if (score == null) {
                continue;
            }

            scoreByLevel
                    .computeIfAbsent(level, l -> new ArrayList<>())
                    .add(score);
        }

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

        if (bestAverage >= 80) {
            return promoteLevel(bestLevel);
        }

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

    private List<PlacementQuestion> selectQuestionsForLevel(Level level, int count) {
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

    private Level calculateFinalLevel(Level lastFinalLevel,
                                      double overallRate) {

        final double UST_SINIR = 0.75;
        final double ALT_SINIR = 0.45;

        if (lastFinalLevel == null) {
            lastFinalLevel = Level.A2;
        }

        if (overallRate >= UST_SINIR) {
            return promoteLevel(lastFinalLevel);
        }

        if (overallRate < ALT_SINIR) {
            return demoteLevel(lastFinalLevel);
        }

        return lastFinalLevel;
    }


}
