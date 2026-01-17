package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/{lessonId}")
    public ResponseEntity<?> enroll(@PathVariable Long lessonId,
                                    @AuthenticationPrincipal User student) {
        enrollmentService.enrollOneSeat(lessonId, student);
        return ResponseEntity.ok().build();
    }
}
