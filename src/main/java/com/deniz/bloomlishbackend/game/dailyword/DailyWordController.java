package com.deniz.bloomlishbackend.game.dailyword;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game/daily-word")
public class DailyWordController {
    private final DailyWordService dailyWordService;

    @GetMapping
    public DailyWord getDailyWord() {
        return dailyWordService.getRandomDailyWord();
    }
}
