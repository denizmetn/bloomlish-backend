package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.placement.PlacementDtos;
import com.deniz.bloomlishbackend.service.PlacementTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/placement")
@RequiredArgsConstructor
public class PlacementTestController {
    private final PlacementTestService placementTestService;

    @GetMapping("/start")
    public ResponseEntity<PlacementDtos.PlacementStartDto> startPlacementTest(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        PlacementDtos.PlacementStartDto response = placementTestService.startPlacementTest(email);
        return ResponseEntity.ok(response);
    }

    // ✔ Cevapları gönder
    @PostMapping("/submit")
    public ResponseEntity<PlacementDtos.PlacementResultDto> submitPlacementTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PlacementDtos.PlacementSubmitDto request
    ) {
        String email = userDetails.getUsername();
        PlacementDtos.PlacementResultDto result = placementTestService.submitPlacementTest(email, request);
        return ResponseEntity.ok(result);
    }
}
