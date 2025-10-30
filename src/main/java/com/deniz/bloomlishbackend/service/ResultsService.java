package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AnswerDto;
import com.deniz.bloomlishbackend.dto.QuizSubmitRequest;
import com.deniz.bloomlishbackend.dto.ResultsDto;
import com.deniz.bloomlishbackend.entity.Question;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.Results;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.mapper.QuestionMapper;
import com.deniz.bloomlishbackend.repository.QuizRepository;
import com.deniz.bloomlishbackend.repository.ResultsRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResultsService {
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final ResultsRepository resultsRepository;
    private final QuestionMapper questionMapper;

    public ResultsDto submitQuiz(Long quizId, QuizSubmitRequest quizSubmitRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz bulunamadı."));
        int correct = 0;
        int wrong = 0;

        for (AnswerDto answerDto : quizSubmitRequest.getAnswers()) {
            Question question = quiz.getQuestions().stream()
                    .filter(q -> q.getId().equals(answerDto.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Soru bulunamadı."));
            boolean isCorrect = question.getOptions().stream()
                    .anyMatch(opt -> Objects.equals(opt.getId(), answerDto.getSelectedOptionId()) && opt.isCorrect());

            if (isCorrect) correct++;
            else wrong++;
        }

        int score = (int) (((double) correct / (correct + wrong)) * 100);

        Results result = Results.builder()
                .user(user)
                .quiz(quiz)
                .score(score)
                .correct(correct)
                .wrong(wrong)
                .level(quiz.getDifficulty())
                .build();

        resultsRepository.save(result);

        return questionMapper.toDtoResults(result);
    }

    public List<ResultsDto> getResultsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        List<Results> results = resultsRepository.findByUser(user);
        return questionMapper.toDtoListResults(results);
    }

}
