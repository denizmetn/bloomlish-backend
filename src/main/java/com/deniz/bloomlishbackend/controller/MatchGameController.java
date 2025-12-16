package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.service.MatchGameService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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


    @PostMapping("/complete")
    public Map<String, Object> complete(
            @RequestParam String level,
            Principal principal
    ) {
        return matchGameService.completeRound(level, principal.getName());
    }

}
