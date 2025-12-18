package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.InstructorStatsDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.InstructorEarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructor/earnings")
@RequiredArgsConstructor
public class InstructorEarningController {

    private final InstructorEarningService earningService;

    @GetMapping("/summary")
    public InstructorStatsDto summary(@AuthenticationPrincipal User instructor) {
        return earningService.getStats(instructor);
    }

    @GetMapping("/monthly")
    public Map<String, Object> monthly(@AuthenticationPrincipal User instructor) {
        return Map.of("data", earningService.getMonthly(instructor));
    }

    @GetMapping("/table")
    public Map<String, Object> table(@AuthenticationPrincipal User instructor) {
        return Map.of("data", earningService.getTable(instructor));
    }
}
