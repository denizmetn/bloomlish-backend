package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.QuizDto;
import com.deniz.bloomlishbackend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/create")
    public ResponseEntity<QuizDto> createQuiz(
            @RequestBody QuizDto quizDto) {
        return ResponseEntity.ok(quizService.create(quizDto));
    }

    @GetMapping("/start")
    public ResponseEntity<QuizDto> startQuiz(
            @RequestParam String quizType,
            @RequestParam String difficulty,
            @RequestParam Integer duration,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        if (userDetails == null) {
            throw new RuntimeException("Bu işlemi yapmak için giriş yapmalısınız!");
        }
        String username=userDetails.getUsername();
        System.out.println("Quiz başlatan kullanıcı: " + username);
        return ResponseEntity.ok(quizService.startQuiz(quizType,difficulty,duration));
    }
}
