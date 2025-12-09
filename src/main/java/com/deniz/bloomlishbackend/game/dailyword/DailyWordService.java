package com.deniz.bloomlishbackend.game.dailyword;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyWordService {

    private final ObjectMapper objectMapper;

    public DailyWord getRandomDailyWord() {
        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/game-words.json");

            List<DailyWord> words =
                    objectMapper.readValue(is, new TypeReference<List<DailyWord>>() {});

            if (words.isEmpty()) return null;

            Collections.shuffle(words);
            return words.get(0);

        } catch (Exception e) {
            throw new RuntimeException("Kelime listesi okunmadı!", e);
        }
    }
}
