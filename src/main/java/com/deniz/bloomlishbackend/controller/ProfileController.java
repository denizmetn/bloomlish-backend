package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.UpdateProfileRequest;
import com.deniz.bloomlishbackend.dto.UpdateProfileResponse;
import com.deniz.bloomlishbackend.dto.UserProfileDto;
import com.deniz.bloomlishbackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/get")
    public ResponseEntity<UserProfileDto> me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(
                profileService.getMyProfile(email)
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
                profileService.uploadAvatar(ud.getUsername(), file)
        );
    }

    @DeleteMapping("/me/avatar/delete")
    public ResponseEntity<Void> deleteAvatar(
            @AuthenticationPrincipal UserDetails ud
    ) {
        profileService.removeAvatar(ud.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public UpdateProfileResponse updateMe(@Valid @RequestBody UpdateProfileRequest req,
                                          Authentication auth) {
        String currentEmail = auth.getName(); // token subject (email)
        return profileService.updateMe(currentEmail, req);
    }

    @GetMapping("/me")
    public UpdateProfileResponse me(Authentication auth) {
        return profileService.me(auth.getName());
    }
}
