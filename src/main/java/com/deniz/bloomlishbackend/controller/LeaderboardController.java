package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.LeaderboardDto;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final UserRepository userRepository;

    @GetMapping("/weekly")
    public List<LeaderboardDto> weekly() {
        return userRepository.findTop10ByOrderByWeeklyXpDesc()
                .stream()
                .map(u -> LeaderboardDto.builder()
                        .userID(u.getUserID())
                        .displayName(
                                u.getDisplayName() != null
                                        ? u.getDisplayName()
                                        : u.getUsername()
                        )
                        .weeklyXp(u.getWeeklyXp()) // ✅ int zaten 0 veya değer
                        .build())
                .toList();
    }

}
