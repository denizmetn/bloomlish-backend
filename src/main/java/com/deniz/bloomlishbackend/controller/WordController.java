package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.service.WordSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordSeedService wordSeedService;

    @PostMapping("/seed")
    public ResponseEntity<String> seed() {
        int count = wordSeedService.seed();
        return ResponseEntity.ok("Seeded words: " + count);
    }
}
