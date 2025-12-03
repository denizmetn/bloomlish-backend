package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.InstructorStatsDto;
import com.deniz.bloomlishbackend.dto.LessonDto;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.service.LessonPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class LessonPaymentController {

    private final LessonPaymentService lessonPaymentService;

    @PostMapping("/lesson/{lessonId}")
    public ResponseEntity<Map<String, String>> startLessonPayment(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User student
    ) {
        String paymentUrl = lessonPaymentService.startLessonPayment(lessonId, student);

        if (paymentUrl == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Ödeme linki üretilemedi"));
        }

        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @PostMapping("/lesson-callback")
    public ResponseEntity<String> lessonCallback(@RequestParam String token) {
        lessonPaymentService.handleLessonPaymentCallback(token);

        String html = """
        <html>
            <head>
                <meta http-equiv="refresh" content="0; URL='http://localhost:5173/payment-success'" />
            </head>
            <body>
                <p>Ödeme başarılı, yönlendiriliyorsunuz...</p>
            </body>
        </html>
    """;

        return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
    }

    @GetMapping("/my-lessons")
    public ResponseEntity<List<LessonDto>> myLessons(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(lessonPaymentService.getMyLessons(student));
    }

    
}
