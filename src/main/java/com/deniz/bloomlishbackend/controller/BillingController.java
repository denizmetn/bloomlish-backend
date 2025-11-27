package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.CheckoutRequest;
import com.deniz.bloomlishbackend.dto.CheckoutResponse;
import com.deniz.bloomlishbackend.dto.PaymentHistoryItemDto;
import com.deniz.bloomlishbackend.dto.SubscriptionDto;
import com.deniz.bloomlishbackend.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // 1) ÖDEME ALMA (CHECKOUT)
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername(); // bizim User.getUsername() email döndürüyordu
        CheckoutResponse response = billingService.checkout(request, email);
        return ResponseEntity.ok(response);
    }

    // 2) AKTİF ABONELİK BİLGİSİ (PREMIUM SAYFASI ÜST KART)
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionDto> getCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        SubscriptionDto dto = billingService.getCurrentSubscription(email);
        return ResponseEntity.ok(dto);
    }

    // 3) ÖDEME GEÇMİŞİ (PREMIUM SAYFASI TABLOSU)
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentHistoryItemDto>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        List<PaymentHistoryItemDto> history = billingService.getPaymentHistory(email);
        return ResponseEntity.ok(history);
    }
}
