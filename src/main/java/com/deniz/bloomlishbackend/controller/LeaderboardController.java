package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.entity.User; // senin User class'ın paketi
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final UserRepository userRepository;

    @GetMapping("/weekly")
    public List<User> weekly() {
        return userRepository.findTop10ByOrderByWeeklyXpDesc();
    }
}
