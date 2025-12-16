package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AiSuggestionResponseDto;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SuggestionService {
    private final ResultsRepository resultsRepository;
    private final UserRepository userRepository;

    public AiSuggestionResponseDto suggestForMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Results> last = resultsRepository.findTop30ByUserUserIDOrderByTakenAtDesc(user.getUserID());

        if (last.isEmpty()) {
            return AiSuggestionResponseDto.builder()
                    .testType("kelime")
                    .difficulty("A1-A2")
                    .limit(5)
                    .reason("Henüz sonuç yok. Başlangıç için kısa ve kolay bir kelime testi önerdim.")
                    .build();
        }

        // Ortalama skor
        double avgScore = last.stream()
                .map(Results::getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        // quizType bazlı yanlış oranı: wrong/(correct+wrong)
        Map<String, int[]> agg = new HashMap<>(); // type -> [correctSum, wrongSum]
        for (Results r : last) {
            Quiz q = r.getQuiz();
            if (q == null || q.getQuizType() == null) continue;

            String type = q.getQuizType(); // <-- senin alanın
            int c = r.getCorrect() == null ? 0 : r.getCorrect();
            int w = r.getWrong() == null ? 0 : r.getWrong();

            agg.putIfAbsent(type, new int[]{0, 0});
            agg.get(type)[0] += c;
            agg.get(type)[1] += w;
        }

        String weakestType = "karisik";
        double worstWrongRate = -1;

        for (var e : agg.entrySet()) {
            int c = e.getValue()[0];
            int w = e.getValue()[1];
            int total = c + w;
            if (total == 0) continue;

            double wrongRate = (double) w / total;
            if (wrongRate > worstWrongRate) {
                worstWrongRate = wrongRate;
                weakestType = e.getKey();
            }
        }

        // Difficulty seçimi: sen frontend’de A1-A2/B1-B2/C1-C2 kullanıyorsun
        String difficulty;
        if (avgScore >= 80) difficulty = "C1-C2";
        else if (avgScore >= 55) difficulty = "B1-B2";
        else difficulty = "A1-A2";

        // Limit seçimi
        int limit;
        if (avgScore < 50) limit = 5;
        else if (avgScore < 75) limit = 10;
        else limit = 15;

        String reason = String.format(
                "Son %d test ortalaman %.0f puan. En çok zorlandığın alan: %s (yanlış oranı ~%.0f%%). Bu yüzden %s seviyesinde %d soruluk önerdim.",
                last.size(),
                avgScore,
                weakestType,
                (worstWrongRate < 0 ? 0 : worstWrongRate * 100),
                difficulty,
                limit
        );

        return AiSuggestionResponseDto.builder()
                .testType(weakestType)
                .difficulty(difficulty)
                .limit(limit)
                .reason(reason)
                .build();
    }
}
