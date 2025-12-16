package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.DailyWordAnswerRequest;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.service.DailyWordGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/daily-word")
@RequiredArgsConstructor
public class DailyWordGameController {
    private final DailyWordGameService dailyWordGameService;
    private final UserRepository userRepository;

    //oyunu başlatma ve cevaplama
    @GetMapping("/start")
    public ResponseEntity<?> start(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean newRound
    ) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(
                dailyWordGameService.start(user.getUserID(), newRound)
        );
    }



    @PostMapping("/answer")
    public ResponseEntity<?> answer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DailyWordAnswerRequest request
    ) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElseThrow();

        var response = dailyWordGameService.answer(
                user.getUserID(),
                request
        );

        return ResponseEntity.ok(response);
    }

}
