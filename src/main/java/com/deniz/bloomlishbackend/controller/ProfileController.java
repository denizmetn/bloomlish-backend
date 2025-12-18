package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.*;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.BadgeService;
import com.deniz.bloomlishbackend.service.ProfileService;
import com.deniz.bloomlishbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final BadgeService badgeService;
    private  final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<UserProfileDto> getMyProfileDto(
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
    public ProfileMeResponse uploadAvatar(
            @AuthenticationPrincipal UserDetails ud,
            @RequestPart("file") MultipartFile file
    ) {
        profileService.uploadAvatar(ud.getUsername(), file); // sadece upload işlemi

        User user = userService.findByEmail(ud.getUsername());
        List<BadgeDto> badges = badgeService.getBadgesForUser(user.getUserID());
        String aiTip = profileService.buildAiTip(user);

        return new ProfileMeResponse(
                user.getUserID(),
                user.getDisplayName(),
                user.getEmail(),
                user.getCurrentLevel(),
                user.getProfileImageUrl(),
                badges,
                aiTip
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
        String currentEmail = auth.getName();
        return profileService.updateMe(currentEmail, req);
    }

    @GetMapping("/me")
    public ProfileMeResponse me(@AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        List<BadgeDto> badges = badgeService.getBadgesForUser(user.getUserID());
        String aiTip = profileService.buildAiTip(user);

        return new ProfileMeResponse(
                user.getUserID(),
                user.getDisplayName(),
                user.getEmail(),
                user.getCurrentLevel(),
                user.getProfileImageUrl(),
                badges,
                aiTip
        );
    }

}
