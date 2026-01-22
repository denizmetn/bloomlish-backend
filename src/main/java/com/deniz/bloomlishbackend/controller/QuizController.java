package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.*;
import com.deniz.bloomlishbackend.entity.Quiz;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.QuizRepository;
import com.deniz.bloomlishbackend.service.QuizService;
import com.deniz.bloomlishbackend.service.ResultsService;
import com.deniz.bloomlishbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final UserService userService;
    private final ResultsService resultsService;
    private final QuizRepository  quizRepository;

    @GetMapping("/start")
    public ResponseEntity<QuizStartResponse> startQuiz(
            @RequestParam String testType,
            @RequestParam String difficulty,
            @RequestParam Integer limit,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("Bu işlemi yapmak için giriş yapmalısınız!");
        }
        String username = userDetails.getUsername();
        System.out.println("Quiz başlatan kullanıcı: " + username);
        User user = userService.findByEmail(username);
        Quiz quiz = quizRepository.save(
                Quiz.builder()
                        .quizType(testType)
                        .difficulty(difficulty)
                        .duration(limit)
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build()
        );
        List<QuestionDto> questions = quizService.startQuiz(testType, difficulty, limit);
        questions.forEach(q -> q.setQuizId(quiz.getId()));
        QuizStartResponse response = QuizStartResponse.builder()
                .quizId(quiz.getId())
                .testType(testType)
                .difficulty(difficulty)
                .limit(limit)
                .questions(questions)
                .build();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/start/listening")
    public ResponseEntity<ListeningQuizResponse> startListeningQuiz(
            @RequestParam String difficulty,
            @RequestParam Integer limit,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("Bu işlemi yapmak için giriş yapmalısınız!");
        }
        String username = userDetails.getUsername();
        System.out.println("Listening quiz başlatan kullanıcı: " + username);
        User user = userService.findByEmail(username);

        // 1) Quiz kaydı
        Quiz quiz = Quiz.builder()
                .quizType("dinleme")
                .difficulty(difficulty)
                .duration(limit)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        quiz = quizRepository.save(quiz);

        ListeningQuizResponse response = quizService.startListeningQuiz(difficulty, limit);

        response.setQuizId(quiz.getId());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/submit")
    public ResponseEntity<QuizResultsDto> submitQuiz(
            @RequestBody QuizSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Bu işlemi yapmak için giriş yapmalısınız!");
        }
        String username = userDetails.getUsername();
        System.out.println("Quiz çözen kullanıcı: " + username);
        System.out.println(" /submit request.quizId = " + request.getQuizId());

        QuizResultsDto result = quizService.evaluateQuiz(username, request.getAnswers());

        result.setQuizId(request.getQuizId());
        User user = userService.findByEmail(username);

        resultsService.saveResult(
                user,
                result.getQuizId(),
                result.getScore(),
                result.getCorrectCount(),
                result.getWrongCount(),
                result.getLevel()

        );

        return ResponseEntity.ok(result);
    }
}



