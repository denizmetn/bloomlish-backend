package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.QuizResultsDto;
import com.deniz.bloomlishbackend.dto.QuizSubmitRequest;
import com.deniz.bloomlishbackend.service.ResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/results")
@RequiredArgsConstructor
public class ResultsController {
    private final ResultsService resultsService;

    @PostMapping("/submit/{quizId}")
    public ResponseEntity<QuizResultsDto> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmitRequest submitRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(resultsService.submitQuiz(quizId, submitRequest, username));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<QuizResultsDto>> getMyResults(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(resultsService.getResultsByUser(username));
    }

}
