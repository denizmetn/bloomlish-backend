package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.entity.User; // senin User class'ın hangi paketteyse onu yaz
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class GameProfileController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public User me(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }
}

