package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.ListeningQuizResponse;
import com.deniz.bloomlishbackend.dto.QuestionDto;
import com.deniz.bloomlishbackend.dto.QuizResultsDto;
import com.deniz.bloomlishbackend.dto.QuizSubmitRequest;
import com.deniz.bloomlishbackend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/start")
    public ResponseEntity<List<QuestionDto>> startQuiz(
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
        return ResponseEntity.ok(quizService.startQuiz(testType, difficulty, limit));
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

        ListeningQuizResponse response = quizService.startListeningQuiz(difficulty, limit);
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

        QuizResultsDto result = quizService.evaluateQuiz(username, request.getAnswers());
        return ResponseEntity.ok(result);
    }
}



