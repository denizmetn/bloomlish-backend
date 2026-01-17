package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.FeedbackCreateRequest;
import com.deniz.bloomlishbackend.dto.FeedbackResponse;
import com.deniz.bloomlishbackend.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public FeedbackResponse create(Authentication auth, @RequestBody FeedbackCreateRequest req) {
        String email = auth.getName();
        return feedbackService.create(email, req);
    }

    @GetMapping("/instructor")
    public List<FeedbackResponse> instructorFeedbacks(Authentication auth) {
        String email = auth.getName();
        return feedbackService.getForInstructor(email);
    }

    @GetMapping("/status/{lessonId}")
    public Map<String, Boolean> status(Authentication auth, @PathVariable Long lessonId) {
        String email = auth.getName();
        return Map.of("submitted", feedbackService.isSubmitted(email, lessonId));
    }
}
