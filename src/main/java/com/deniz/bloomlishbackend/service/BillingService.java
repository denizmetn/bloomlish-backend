package com.deniz.bloomlishbackend.service;
import java.math.BigDecimal;

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
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final Options iyzicoOptions;          // <-- config’ten geliyor
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public CheckoutResponse checkout(CheckoutRequest request, String userEmail) {

        // 1) Kullanıcıyı bul
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 2) Plan & fiyat
        PlanType planType = PlanType.valueOf(request.getPlanType().toUpperCase());
        int amount = (planType == PlanType.MONTHLY) ? 200 : 2000;

        // 3) Iyzico payment request hazırla
        CreatePaymentRequest payReq = new CreatePaymentRequest();
        payReq.setLocale(Locale.TR.getValue());
        payReq.setConversationId("SUB-" + user.getUserID() + "-" + System.currentTimeMillis());
        payReq.setPrice(BigDecimal.valueOf(amount));
        payReq.setPaidPrice(BigDecimal.valueOf(amount));
        payReq.setCurrency(Currency.TRY.name());

        // 3.1 Kart bilgileri
        PaymentCard card = new PaymentCard();
        card.setCardHolderName(request.getCardHolderName());
        card.setCardNumber(request.getCardNumber());
        card.setExpireMonth(String.valueOf(request.getExpiryMonth())); // örn: 3 → "3"
        card.setExpireYear(String.valueOf(request.getExpiryYear()));   // örn: 26 → "26"
        card.setCvc(request.getCvc());
        card.setRegisterCard(0);
        payReq.setPaymentCard(card);

        // 3.2 Buyer bilgileri
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(user.getUserID()));
        buyer.setName(user.getUsername());
        buyer.setSurname("User");
        buyer.setEmail(user.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("İstanbul");
        buyer.setIp("85.34.78.112");
        buyer.setCity("İstanbul");
        buyer.setCountry("Türkiye");
        buyer.setZipCode("34000");

        payReq.setBuyer(buyer);


        // 3.3 Fatura adresi
        Address address = new Address();
        address.setContactName(request.getCardHolderName());
        address.setCity("İstanbul");
        address.setCountry("Türkiye");
        address.setAddress(request.getBillingAddress());
        address.setZipCode("34000");

        payReq.setBillingAddress(address);


        // 3.4 Sepet
        List<BasketItem> items = new ArrayList<>();
        BasketItem item = new BasketItem();
        item.setId("SUB-" + planType);
        item.setName(planType.name() + " abonelik");
        item.setCategory1("Abonelik");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(BigDecimal.valueOf(amount));
        items.add(item);
        payReq.setBasketItems(items);

        // 4) İyzico ödeme çağrısı
        com.iyzipay.model.Payment iyzicoPayment =
                com.iyzipay.model.Payment.create(payReq, iyzicoOptions);

        // 5) Başarısızsa
        if (!"success".equalsIgnoreCase(iyzicoPayment.getStatus())) {

            Payment failedPayment = Payment.builder()
                    .user(user)
                    .planType(planType)
                    .amount(amount)
                    .currency("TRY")
                    .status(PaymentStatus.FAILED)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(failedPayment);

            throw new RuntimeException("Ödeme reddedildi: " + iyzicoPayment.getErrorMessage());
        }

        // 6) Başarılı → eski aboneliği kapat, yenisini aç
        Payment successPayment = Payment.builder()
                .user(user)
                .planType(planType)
                .amount(amount)
                .currency("TRY")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(successPayment);

        subscriptionRepository.findByUserAndActiveTrue(user).ifPresent(old -> {
            old.setActive(false);
            subscriptionRepository.save(old);
        });

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = (planType == PlanType.MONTHLY) ? start.plusMonths(1) : start.plusYears(1);

        Subscription newSub = Subscription.builder()
                .user(user)
                .planType(planType)
                .startDate(start)
                .endDate(end)
                .active(true)
                .build();

        Subscription saved = subscriptionRepository.save(newSub);

        return CheckoutResponse.builder()
                .subscriptionId(saved.getId())
                .planType(planType.name())
                .amount(amount)
                .currency("TRY")
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
