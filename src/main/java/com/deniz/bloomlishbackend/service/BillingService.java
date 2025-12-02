package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.CheckoutRequest;
import com.deniz.bloomlishbackend.dto.PaymentHistoryItemDto;
import com.deniz.bloomlishbackend.dto.SubscriptionDto;
import com.deniz.bloomlishbackend.entity.*;
import com.deniz.bloomlishbackend.entity.Payment;
import com.deniz.bloomlishbackend.repository.PaymentRepository;
import com.deniz.bloomlishbackend.repository.SubscriptionRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final Options iyzicoOptions;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    // -------------------------------------------------------
    // 1) CHECKOUT FORM OLUŞTURMA → ÖDEME SAYFASI LİNKİ DÖNER
    // -------------------------------------------------------
    public String createCheckoutFormToken(CheckoutRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        PlanType planType = PlanType.valueOf(request.getPlanType().toUpperCase());
        int amount = (planType == PlanType.MONTHLY) ? 200 : 2000;

        CreateCheckoutFormInitializeRequest iyzReq = new CreateCheckoutFormInitializeRequest();
        iyzReq.setLocale(Locale.TR.getValue());
        // İstersek loglamak için yine set edebiliriz ama artık buna güvenmiyoruz.
        iyzReq.setConversationId("SUB-" + user.getUserID());
        iyzReq.setPrice(BigDecimal.valueOf(amount));
        iyzReq.setPaidPrice(BigDecimal.valueOf(amount));
        iyzReq.setCurrency("TRY");

        // Iyzi ödeme bittikten sonra backend'e dönecek URL
        // userId'yi query param olarak ekliyoruz
        iyzReq.setCallbackUrl("http://localhost:8080/api/billing/checkout-callback?userId=" + user.getUserID());

        // Buyer
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(user.getUserID()));
        buyer.setName(user.getUsername());
        buyer.setSurname("User");
        buyer.setEmail(user.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("İstanbul");
        buyer.setCity("İstanbul");
        buyer.setCountry("Türkiye");
        iyzReq.setBuyer(buyer);

        // Adres
        Address addr = new Address();
        addr.setContactName(user.getUsername());
        addr.setCity("İstanbul");
        addr.setCountry("Türkiye");
        addr.setAddress("Test adres");
        iyzReq.setBillingAddress(addr);
        iyzReq.setShippingAddress(addr);

        // Basket item
        BasketItem item = new BasketItem();
        item.setId("SUB-" + planType);
        item.setName(planType.name() + " abonelik");
        item.setCategory1("Abonelik");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(BigDecimal.valueOf(amount));
        iyzReq.setBasketItems(List.of(item));

        CheckoutFormInitialize form =
                CheckoutFormInitialize.create(iyzReq, iyzicoOptions);

        if (!"success".equalsIgnoreCase(form.getStatus())) {
            throw new RuntimeException("Iyzico Hata → " + form.getErrorMessage());
        }

        // ÖDEME SAYFASI LİNKİ
        return form.getPaymentPageUrl();
    }

    // -------------------------------------------------------
    // 2) CHECKOUT CALLBACK – Iyzi ödeme sonucunu geri yollar
    // -------------------------------------------------------
    public void handleCheckoutResult(String token, Long userId) {

        RetrieveCheckoutFormRequest req = new RetrieveCheckoutFormRequest();
        req.setLocale(Locale.TR.getValue());
        req.setToken(token);

        CheckoutForm result = CheckoutForm.retrieve(req, iyzicoOptions);

        if (result == null) {
            throw new RuntimeException("CheckoutForm null döndü!");
        }

        // Debug log
        System.out.println("Iyzi status      = " + result.getStatus());
        System.out.println("Iyzi payStatus   = " + result.getPaymentStatus());
        System.out.println("Iyzi convId      = " + result.getConversationId());
        System.out.println("Iyzi token       = " + result.getToken());
        System.out.println("Callback userId  = " + userId);

        if (!"success".equalsIgnoreCase(result.getPaymentStatus())) {
            System.out.println("Ödeme başarısız → " + result.getErrorMessage());
            return;
        }

        // 🔴 userId artık callback URL'den geliyor
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User bulunamadı: " + userId));

        int amount = result.getPaidPrice().intValue();
        PlanType planType = (amount == 200) ? PlanType.MONTHLY : PlanType.YEARLY;

        // Eski aboneliği pasif yap
        subscriptionRepository.findByUserAndActiveTrue(user).ifPresent(old -> {
            old.setActive(false);
            subscriptionRepository.save(old);
        });

        // Yeni abonelik aç
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = (planType == PlanType.MONTHLY)
                ? start.plusMonths(1)
                : start.plusYears(1);

        Subscription sub = Subscription.builder()
                .user(user)
                .planType(planType)
                .startDate(start)
                .endDate(end)
                .active(true)
                .build();

        subscriptionRepository.save(sub);

        // Ödeme kaydı ekle
        Payment payment = Payment.builder()
                .user(user)
                .planType(planType)
                .amount(amount)
                .currency("TRY")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        System.out.println("Ödeme + abonelik başarılı → " + user.getEmail());
    }

    // -------------------------------------------------------
    // 3) ABONELİK BİLGİSİ
    // -------------------------------------------------------
    public SubscriptionDto getCurrentSubscription(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User bulunamadı"));

        return subscriptionRepository.findByUserAndActiveTrue(user)
                .map(sub -> SubscriptionDto.builder()
                        .hasActiveSubscription(true)
                        .planType(sub.getPlanType().name())
                        .expiresAt(sub.getEndDate().toLocalDate())
                        .features(getFeatures(sub.getPlanType()))
                        .build())
                .orElse(SubscriptionDto.builder()
                        .hasActiveSubscription(false)
                        .features(List.of())
                        .build());
    }

    private List<String> getFeatures(PlanType planType) {
        if (planType == PlanType.MONTHLY) {
            return List.of("Günlük 40 Mesaj", "Kelime Ezber", "Görevler", "Reklamsız Kullanım");
        }
        return List.of("Günlük 40 Mesaj", "Kelime Ezber", "Görevler", "Reklamsız Kullanım",
                "Yıllık Özel İstatistikler", "Premium Rozet");
    }

    // -------------------------------------------------------
    // 4) ÖDEME GEÇMİŞİ
    // -------------------------------------------------------
    public List<PaymentHistoryItemDto> getPaymentHistory(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User bulunamadı"));

        return paymentRepository.findByUserOrderByPaidAtDesc(user)
                .stream()
                .map(p -> PaymentHistoryItemDto.builder()
                        .date(p.getPaidAt().toLocalDate())
                        .plan(p.getPlanType().name())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .status(p.getStatus().name())
                        .build())
                .toList();
    }
}
