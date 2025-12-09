package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.CheckoutRequest;
import com.deniz.bloomlishbackend.dto.PaymentHistoryItemDto;
import com.deniz.bloomlishbackend.dto.SubscriptionDto;
import com.deniz.bloomlishbackend.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // ⭐ 1) ÖDEME SAYFASI LİNKİ OLUŞTURMA (IYZICO CHECKOUT)
    @PostMapping("/start-checkout")
    public ResponseEntity<?> startCheckout(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        String paymentUrl = billingService.createCheckoutFormToken(request, email);

        // Frontend buradaki URL'e redirect olacak
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    // ⭐ 2) Iyzico callback → ödeme sonucu
    // Iyzico bu endpoint'e POST ile token gönderir, userId URL'den gelir
    @PostMapping("/checkout-callback")
    public ResponseEntity<?> checkoutCallback(
            @RequestParam("token") String token,
            @RequestParam("userId") Long userId
    ) {
        System.out.println(">>> IYZI CALLBACK GELDİ token=" + token + " userId=" + userId);
        billingService.handleCheckoutResult(token, userId);

        String html = """
                <html>
                  <head>
                    <meta http-equiv="refresh" content="0; URL='http://localhost:5173/payment-success'" />
                  </head>
                  <body>
                    <p>Abonelik ödemeniz başarılı, yönlendiriliyorsunuz...</p>
                  </body>
                </html>
                """;

        return ResponseEntity
                .ok()
                .header("Content-Type", "text/html")
                .body(html);
    }
    @GetMapping("/checkout-callback")
    public ResponseEntity<String> checkoutCallbackPage() {
        return ResponseEntity.ok("Ödeme sonucu işlendi. Uygulamaya geri dönebilirsiniz.");
    }


    // (İstersen bunu silebilirsin, debug için bırakılabilir)
    @PostMapping("/callback")
    public ResponseEntity<?> iyzicoCallback(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("message", "Callback alındı", "payload", payload));
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionDto> getCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(billingService.getCurrentSubscription(email));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentHistoryItemDto>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(billingService.getPaymentHistory(email));
    }

    @PostMapping("/start-trial")
    public ResponseEntity<Void> startTrial() {
        billingService.startTrialForCurrentUser();
        return ResponseEntity.ok().build();
    }
}
