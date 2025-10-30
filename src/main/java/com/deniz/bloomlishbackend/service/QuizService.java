package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.QuizDto;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.mapper.QuestionMapper;
import com.deniz.bloomlishbackend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizService {

    public final QuizRepository quizRepository;
    private final QuestionMapper questionMapper;


    public QuizDto create(QuizDto quizDto) {
        Quiz quiz = Quiz.builder()
                .quizType(quizDto.getQuizType())
                .difficulty(quizDto.getDifficulty())
                .duration(quizDto.getDuration())
                .build();
        Quiz saved = quizRepository.save(quiz);
        return questionMapper.toDtoQuiz(saved);

    }

    public QuizDto startQuiz(String quizType, String difficulty, Integer duration) {
        Quiz quiz = quizRepository.findFirstByQuizTypeAndDifficultyAndDuration(quizType, difficulty, duration)
                .orElseThrow(() -> new RuntimeException("Uygun quiz bulunamadı."));
        return questionMapper.toDtoQuiz(quiz);
    }
}
