package com.deniz.bloomlishbackend.config;

import com.deniz.bloomlishbackend.dto.placement.PlacementDtos;
import com.deniz.bloomlishbackend.entity.Level;
import com.deniz.bloomlishbackend.entity.PlacementQuestion;
import com.deniz.bloomlishbackend.repository.PlacementQuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlacementQuestionLoader implements CommandLineRunner {
    private final PlacementQuestionRepository placementQuestionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (placementQuestionRepository.count() > 0) {
            return;
        }
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/placement-question.json");
        if (inputStream == null) {
            throw new IllegalStateException(
                    "data/placement-question.json classpath'te bulunamadı! " +
                            "Dosyanın yolunu ve adını kontrol et."
            );
        }
        List<PlacementDtos.PlacementQuestionJson> dtoList =
                Arrays.asList(objectMapper.readValue(inputStream, PlacementDtos.PlacementQuestionJson[].class));
        List<PlacementQuestion> entities = dtoList.stream().map(dto -> {
            PlacementQuestion q = new PlacementQuestion();
            q.setQuestion(dto.getQuestionText());
            q.setOptions(dto.getOptions());
            q.setCorrectAnswer(dto.getCorrectOption());

            // JSON'da "a2", "B1" gibi gelse bile büyük harfe çevirip enum'a çeviriyoruz
            String levelStr = dto.getCefrLevel();
            Level level = Level.valueOf(levelStr.toUpperCase()); // A1, A2, B1, B2, C1, C2
            q.setLevel(level);

            return q;
        }).toList();

        placementQuestionRepository.saveAll(entities);
        System.out.println("Placement soruları yüklendi: " + entities.size());
    }
}