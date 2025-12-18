package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.BadgeDto;
import com.deniz.bloomlishbackend.dto.UserStats;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final ResultsRepository resultsRepository;

    public List<BadgeDto> getBadgesForUser(Long userId) {

        int kelimeCorrect     = resultsRepository.sumCorrectByUserAndQuizType(userId, "kelime");
        int dilbilgisiCorrect = resultsRepository.sumCorrectByUserAndQuizType(userId, "dilbilgisi");
        int okumaCorrect      = resultsRepository.sumCorrectByUserAndQuizType(userId, "okuma");
        int dinlemeCorrect    = resultsRepository.sumCorrectByUserAndQuizType(userId, "dinleme");
        int yazimCorrect      = resultsRepository.sumCorrectByUserAndQuizType(userId, "yazim");
        int karisikCorrect    = resultsRepository.sumCorrectByUserAndQuizType(userId, "karisik");

        return List.of(
                badge("VOCAB_30", "Kelime Ustası",
                        "Kelime Bilgisi quizlerinde toplam 30 doğru yap.",
                        kelimeCorrect, 30),

                badge("GRAMMAR_30", "Dilbilgisi Ustası",
                        "Dilbilgisi quizlerinde toplam 30 doğru yap.",
                        dilbilgisiCorrect, 30),

                badge("READING_30", "Okuma Kahramanı",
                        "Okuma Anlama quizlerinde toplam 30 doğru yap.",
                        okumaCorrect, 30),

                badge("LISTENING_30", "Dinleme Kahramanı",
                        "Dinleme Anlama quizlerinde toplam 30 doğru yap.",
                        dinlemeCorrect, 30),

                badge("DICTATION_30", "Dikte Ustası",
                        "Yazım/Dikte quizlerinde toplam 30 doğru yap.",
                        yazimCorrect, 30),

                badge("MIXED_50", "Karışık Canavarı",
                        "Karışık Quizlerde toplam 50 doğru yap.",
                        karisikCorrect, 50)
        );
    }



    private BadgeDto badge(String id, String title, String desc, int value, int threshold) {
        int progress = Math.min(value, threshold);
        boolean earned = value >= threshold;
        return new BadgeDto(id, title, desc, progress, threshold, earned);
    }
}
