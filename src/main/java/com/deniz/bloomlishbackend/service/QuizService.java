package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AnswerDto;
import com.deniz.bloomlishbackend.dto.QuestionDto;
import com.deniz.bloomlishbackend.dto.QuizResultsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private Map<String, List<QuestionDto>> datasets = new HashMap<>();
    private Map<Long, QuestionDto> questionIndex = new HashMap<>();


    private List<QuestionDto> readJson(String path, ObjectMapper mapper) throws IOException {
        InputStream is = new ClassPathResource(path).getInputStream();
        return Arrays.asList(mapper.readValue(is, QuestionDto[].class));
    }

    public List<QuestionDto> startQuiz(String type, String difficulty, int count) {
        // 1) Filtrele
        List<QuestionDto> filtered = datasets.getOrDefault(type, List.of()).stream()
                .filter(q -> q.getDifficulty() != null &&
                        q.getDifficulty().equalsIgnoreCase(difficulty))
                .toList(); // immutable olabilir

        // 2) Mutable listeye kopyala
        List<QuestionDto> all = new ArrayList<>(filtered);

        // 3) Shuffle artık güvenli
        Collections.shuffle(all);

        // 4) İlk 'count' kadarını dön
        return all.stream()
                .limit(count)
                .toList();
    }

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<QuestionDto> kelime = readJson("data/words.json", mapper);
        datasets.put("kelime", kelime);

        // İleride diğer tipler:
        // List<QuestionDto> dil = readJson("data/grammar.json", mapper);
        // datasets.put("dilbilgisi", dil);

        // Tüm soruları id → QuestionDto map'ine koy
        kelime.forEach(q -> questionIndex.put(q.getId(), q));
        // dil.forEach(q -> questionIndex.put(q.getId(), q)); // vs...
    }

    public QuizResultsDto evaluateQuiz(String username, List<AnswerDto> answers) {
        int total = answers.size();
        int correct = 0;
        int wrong = 0;
        for (AnswerDto answer : answers) {
            QuestionDto question = questionIndex.get(answer.getQuestionId());
            if (question == null) {
                wrong++;
                continue;
            }
            String correctAnswer = question.getAnswer();
            if (correctAnswer != null &&
                    correctAnswer.equalsIgnoreCase(answer.getSelectedOption())) {
                correct++;
            } else {
                wrong++;
            }
        }
        int score = total == 0 ? 0 : (int) Math.round((correct * 100.0) / total);
        String level;
        if (score >= 80) level = "İleri";
        else if (score >= 50) level = "Orta";
        else level = "Başlangıç";

        QuizResultsDto dto = QuizResultsDto.builder()
                .id(null)
                .userId(null)
                .quizId(null)
                .score(score)
                .correctCount(correct)
                .wrongCount(wrong)
                .level(level)
                .takenAt(LocalDateTime.now())
                .build();
        return dto;
    }
}

