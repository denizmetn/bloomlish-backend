package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.ResultsSummaryDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.ResultsService;
import com.deniz.bloomlishbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/results")
@RequiredArgsConstructor
public class ResultsController {
    private  final ResultsService resultsService;
    private final UserService  userService;

    @GetMapping("summary/me")
    public ResultsSummaryDto getSummary(@AuthenticationPrincipal UserDetails userDetails){
        String username=userDetails.getUsername();
        User currentUser=userService.findByEmail(username);
        return resultsService.getSummmaryForUser(currentUser);

    }
}
