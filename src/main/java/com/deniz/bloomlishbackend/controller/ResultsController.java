package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.ResultsSummaryDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.ResultsService;
import com.deniz.bloomlishbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultsController {
    private  final ResultsService resultsService;
    private final UserService  userService;

    @GetMapping("/summary/me")
    public ResponseEntity<ResultsSummaryDto> getSummary(@AuthenticationPrincipal UserDetails userDetails){
        String username=userDetails.getUsername();
        User currentUser=userService.findByEmail(username);
        return ResponseEntity.ok(resultsService.getSummaryForUser(currentUser));

    }

}
