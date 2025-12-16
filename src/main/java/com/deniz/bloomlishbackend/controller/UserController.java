package com.deniz.bloomlishbackend.controller;


import com.deniz.bloomlishbackend.dto.AuthResponse;
import com.deniz.bloomlishbackend.dto.LoginRequest;
import com.deniz.bloomlishbackend.dto.RegisterRequest;
import com.deniz.bloomlishbackend.dto.UserProfileDto;
import com.deniz.bloomlishbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.ok(userService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(
                userService.getMyProfile(email)
        );
    }
    @PostMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserProfileDto> uploadAvatar(
            @AuthenticationPrincipal UserDetails ud,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                userService.uploadAvatar(ud.getUsername(), file)
        );
    }

    @DeleteMapping("/me/avatar/delete")
    public ResponseEntity<Void> deleteAvatar(
            @AuthenticationPrincipal UserDetails ud
    ) {
        userService.removeAvatar(ud.getUsername());
        return ResponseEntity.noContent().build();
    }


}
