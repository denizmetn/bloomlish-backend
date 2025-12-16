package com.deniz.bloomlishbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomWordService {

    private final RestTemplate restTemplate;
    private final Random random = new Random();

    private static final String WORDS_URL =
            "https://raw.githubusercontent.com/Maximax67/Words-CEFR-Dataset/main/csv/words.csv";

    private List<String> cachedWords;

    private void loadWordsIfNeeded() {
        if (cachedWords != null) return;

        cachedWords = new ArrayList<>();
        String csv = restTemplate.getForObject(WORDS_URL, String.class);
        if (csv == null) return;

        String[] lines = csv.split("\n");

        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(",");
            if (parts.length < 2) continue;

            String word = parts[1].replace("\"", "").trim();
            if (!word.isBlank()) {
                cachedWords.add(word.toLowerCase());
            }
        }
    }

    public String getRandomWord() {
        loadWordsIfNeeded();
        if (cachedWords.isEmpty()) return null;
        return cachedWords.get(random.nextInt(cachedWords.size()));
    }
}
