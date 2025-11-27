package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.CheckoutRequest;
import com.deniz.bloomlishbackend.dto.CheckoutResponse;
import com.deniz.bloomlishbackend.dto.PaymentHistoryItemDto;
import com.deniz.bloomlishbackend.dto.SubscriptionDto;

import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.entity.Payment;
import com.deniz.bloomlishbackend.entity.Subscription;
import com.deniz.bloomlishbackend.entity.PlanType;
import com.deniz.bloomlishbackend.entity.PaymentStatus;

import com.deniz.bloomlishbackend.repository.PaymentRepository;
import com.deniz.bloomlishbackend.repository.SubscriptionRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public CheckoutResponse checkout(CheckoutRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        PlanType planType = PlanType.valueOf(request.getPlanType().toUpperCase());
        int amount = (planType == PlanType.MONTHLY) ? 200 : 2000;

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = (planType == PlanType.MONTHLY) ? start.plusMonths(1) : start.plusYears(1);

        Payment payment = Payment.builder() // Ödeme kaydı oluştur
                .user(user)
                .planType(planType)
                .amount(amount)
                .currency("TRY")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        subscriptionRepository.findByUserAndActiveTrue(user).ifPresent(oldSub -> {
            oldSub.setActive(false); //ESKİ ABONELİĞİ PASİF YAP
            subscriptionRepository.save(oldSub);
        });

        Subscription subscription = Subscription.builder() //Yeni abonelik kaydı oluştur
                .user(user)
                .planType(planType)
                .startDate(start)
                .endDate(end)
                .active(true)
                .build();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return CheckoutResponse.builder()
                .subscriptionId(savedSubscription.getId())
                .planType(planType.name())
                .currency("TRY")
                .amount(amount)
                .startDate(start)
                .endDate(end)
                .status("SUCCESS")
                .message("Ödeme başarılı! Aboneliğin aktifleştirildi.")
                .build();
    }

    public SubscriptionDto getCurrentSubscription(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Kullanıcı bulunamadı"));
        return subscriptionRepository.findByUserAndActiveTrue(user)
                .map(sub -> SubscriptionDto.builder()
                        .hasActiveSubscription(true)
                        .planType(sub.getPlanType().name())
                        .expiresAt(sub.getEndDate().toLocalDate())
                        .features(getFeaturesForPlan(sub.getPlanType()))
                        .build())
                .orElse(
                        SubscriptionDto.builder()
                                .hasActiveSubscription(false)
                                .features(List.of())
                                .build()
                );
    }

    private List<String> getFeaturesForPlan(PlanType planType) {
        if (planType == PlanType.MONTHLY) {
            return List.of(
                    "Günlük 40 Mesaj AI Chat",
                    "Kelime Ezber Modülü",
                    "Günlük İngilizce Pratik Görevleri",
                    "Reklamsız Kullanım"
            );
        }

        return List.of(
                "Günlük 40 Mesaj AI Chat",
                "Kelime Ezber Modülü",
                "Günlük İngilizce Pratik Görevleri",
                "Reklamsız Kullanım",
                "Yıllık Özel İstatistikler",
                "Premium rozet"
        );
    }
    public List<PaymentHistoryItemDto> getPaymentHistory(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return paymentRepository.findByUserOrderByPaidAtDesc(user)
                .stream()
                .map(p -> PaymentHistoryItemDto.builder()
                        .date(p.getPaidAt().toLocalDate())
                        .plan(p.getPlanType().name())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .status(p.getStatus().name())
                        .build()
                )
                .toList();
    }

}
