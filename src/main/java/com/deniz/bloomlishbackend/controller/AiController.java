package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.AiSuggestionResponseDto;
import com.deniz.bloomlishbackend.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final SuggestionService suggestionService;

    @GetMapping("/suggestion/me")
    public ResponseEntity<AiSuggestionResponseDto> suggestionMe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(suggestionService.suggestForMe(userDetails.getUsername()));
    }
}
