package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.DictionaryResult;
import com.deniz.bloomlishbackend.entity.Word;
import com.deniz.bloomlishbackend.repository.WordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class WordSeedService {

    private final RandomWordService randomWordService;
    private final DictionaryService dictionaryService;
    private final WordRepository wordRepository;

    private static final int TARGET_COUNT = 100; // 🔥 ŞİMDİ 5
    private static final int DELAY_MS = 700;   // 🔥 RATE LIMIT YEMEZ

    @Transactional
    public int seed() {

        int saved = 0;

        while (saved < TARGET_COUNT) {

            String word = randomWordService.getRandomWord();
            if (word == null) continue;

            if (wordRepository.existsByWordIgnoreCase(word)) continue;

            DictionaryResult dict = dictionaryService.fetch(word);
            if (dict == null) continue;

            String sentence = dict.getExample();


            if (!sentenceContainsExactWord(word, sentence)) {
                log.info("SKIPPED ❌ '{}' not in sentence → {}", word, sentence);
                continue;
            }

            Word w = new Word();
            w.setWord(word);              // artık güvenli
            w.setMeaning(dict.getMeaning());
            w.setSentence(sentence);

            wordRepository.save(w);
            saved++;

            log.info("Saved [{}/{}] → {}", saved, TARGET_COUNT, word);

            try {
                Thread.sleep(DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return saved;
    }
    private boolean sentenceContainsExactWord(String word, String sentence) {
        if (word == null || sentence == null) return false;

        String lowerSentence = sentence.toLowerCase();
        String lowerWord = word.toLowerCase();

        return Pattern.compile("\\b" + Pattern.quote(lowerWord) + "\\b")
                .matcher(lowerSentence)
                .find();
    }

}
