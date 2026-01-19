package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.*;
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
        String normalizedDifficulty = mapDifficulty(difficulty);
        // Eğer karışık test istendiyse özel metoda yönlendir
        if ("karisik".equalsIgnoreCase(type)) {
            return startMixedQuiz(normalizedDifficulty, count);
        }
        // 1) Filtrele
        List<QuestionDto> filtered = datasets.getOrDefault(type, List.of()).stream()
                .filter(q -> q.getDifficulty() != null &&
                        q.getDifficulty().equalsIgnoreCase(normalizedDifficulty))
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
    public ListeningQuizResponse startListeningQuiz(String difficulty, int totalLimit) {
        String normalizedDifficulty = mapDifficulty(difficulty);
        // 1) Sadece dinleme sorularını al ve zorluk filtresi uygula
        List<QuestionDto> listeningQs = datasets
                .getOrDefault("dinleme", List.of()).stream()
                .filter(q -> q.getDifficulty() != null &&
                        q.getDifficulty().equalsIgnoreCase(normalizedDifficulty))
                .toList();

        // 2) Aynı audioUrl'e sahip soruları grupla
        Map<String, List<QuestionDto>> groupedByAudio =
                listeningQs.stream()
                        .filter(q -> q.getAudioUrl() != null)
                        .collect(java.util.stream.Collectors.groupingBy(QuestionDto::getAudioUrl));

        List<ListeningAudioGroupDto> audioGroups = new ArrayList<>();
        long audioIdCounter = 1L;

        // 3) toplam soru limitini kaba şekilde uygula (istersen ilerde ince ayar yaparız)
        int remaining = totalLimit > 0 ? totalLimit : Integer.MAX_VALUE;
        List<Map.Entry<String, List<QuestionDto>>> groupEntries =
                new ArrayList<>(groupedByAudio.entrySet());
        Collections.shuffle(groupEntries);

        for (Map.Entry<String, List<QuestionDto>> entry : groupedByAudio.entrySet()) {
            if (remaining <= 0) break;

            String audioUrl = entry.getKey();
            List<QuestionDto> qs = new ArrayList<>(entry.getValue());
            String topic = qs.stream()
                    .map(QuestionDto::getTopic)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            Collections.shuffle(qs);
            List<QuestionDto> chosen;
            if (remaining < qs.size()) {
                chosen = qs.subList(0, remaining);
            } else {
                chosen = qs;
            }
            remaining -= chosen.size();

            ListeningAudioGroupDto group = new ListeningAudioGroupDto();
            group.setAudioId(audioIdCounter++);
            group.setAudioUrl(audioUrl);
            group.setTopic(topic);
            group.setQuestions(chosen);

            audioGroups.add(group);
        }

        ListeningQuizResponse response = new ListeningQuizResponse();
        response.setDifficulty(difficulty);
        response.setAudioGroups(audioGroups);
        return response;
    }
    public List<QuestionDto> startMixedQuiz(String difficulty, int limit) {
        String normalizedDifficulty = mapDifficulty(difficulty);
        // Hangi tipler karışıkta olsun?
        List<String> types = List.of("kelime", "dilbilgisi", "okuma", "yazim", "dinleme");

        List<QuestionDto> pool = new ArrayList<>();

        for (String type : types) {
            List<QuestionDto> list = datasets.getOrDefault(type, List.of()).stream()
                    .filter(q -> q.getDifficulty() != null &&
                            q.getDifficulty().equalsIgnoreCase(normalizedDifficulty))
                    .toList();
            pool.addAll(list);
        }

        // Karıştır ve ilk limit kadarını dön
        Collections.shuffle(pool);

        return pool.stream()
                .limit(limit)
                .toList();
    }
    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<QuestionDto> kelime = readJson("data/words.json", mapper);
        datasets.put("kelime", kelime);

         List<QuestionDto> dil = readJson("data/grammar.json", mapper);
         datasets.put("dilbilgisi", dil);
        List<QuestionDto> okuma = readJson("data/reading.json", mapper);
        datasets.put("okuma", okuma);
        List<QuestionDto> yazim = readJson("data/spelling.json", mapper);
        datasets.put("yazim", yazim);
       List<QuestionDto> listening = readJson("data/listening.json", mapper);
        datasets.put("dinleme", listening);


        kelime.forEach(q -> questionIndex.put(q.getId(), q));
         dil.forEach(q -> questionIndex.put(q.getId(), q));
         okuma.forEach(q -> questionIndex.put(q.getId(), q));
         yazim.forEach(q -> questionIndex.put(q.getId(), q));
       listening.forEach(q -> questionIndex.put(q.getId(), q));
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
                .id(null).userId(null).quizId(null).score(score).correctCount(correct).wrongCount(wrong).level(level)
                .takenAt(LocalDateTime.now())
                .build();
        return dto;
    }
    private String mapDifficulty(String cefr) {
        if (cefr == null) return "medium"; // default

        return switch (cefr) {
            case "A1-A2" -> "easy";
            case "B1-B2" -> "medium";
            case "C1-C2" -> "hard";
            default -> cefr.toLowerCase();
        };
    }

}

