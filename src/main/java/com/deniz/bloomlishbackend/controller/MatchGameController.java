package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.service.MatchGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/games/match")
@RequiredArgsConstructor
public class MatchGameController {

    private final MatchGameService matchGameService;

    @GetMapping("/round")
    public Map<String, Object> round(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "6") int count
    ) {
        return matchGameService.getRound(level, count);
    }
}
