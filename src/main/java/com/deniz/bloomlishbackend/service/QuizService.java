package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.QuestionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private Map<String, List<QuestionDto>> datasets = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        datasets.put("kelime", readJson("data/words.json", mapper));
       // datasets.put("dilbilgisi", readJson("data/grammar.json", mapper));
        //datasets.put("okuma", readJson("data/reading.json", mapper));
        //datasets.put("dinleme", readJson("data/listening.json", mapper));
        //datasets.put("yazim", readJson("data/spelling.json", mapper));
    }

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
}

