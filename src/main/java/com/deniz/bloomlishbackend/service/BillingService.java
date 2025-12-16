package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.CheckoutRequest;
import com.deniz.bloomlishbackend.dto.PaymentHistoryItemDto;
import com.deniz.bloomlishbackend.dto.SubscriptionDto;
import com.deniz.bloomlishbackend.entity.*;

import com.deniz.bloomlishbackend.repository.PaymentRepository;
import com.deniz.bloomlishbackend.repository.SubscriptionRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Locale;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        iyzReq.setConversationId("SUB-" + user.getUserID());
        iyzReq.setPrice(BigDecimal.valueOf(amount));
        iyzReq.setPaidPrice(BigDecimal.valueOf(amount));
        iyzReq.setCurrency("TRY");



        iyzReq.setCallbackUrl("http://localhost:8080/api/billing/checkout-callback?userId=" + user.getUserID());


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


        Address addr = new Address();
        addr.setContactName(user.getUsername());
        addr.setCity("İstanbul");
        addr.setCountry("Türkiye");
        addr.setAddress("Test adres");
        iyzReq.setBillingAddress(addr);
        iyzReq.setShippingAddress(addr);


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


        System.out.println("Iyzi status      = " + result.getStatus());
        System.out.println("Iyzi payStatus   = " + result.getPaymentStatus());
        System.out.println("Iyzi convId      = " + result.getConversationId());
        System.out.println("Iyzi token       = " + result.getToken());
        System.out.println("Callback userId  = " + userId);

        if (!"success".equalsIgnoreCase(result.getPaymentStatus())) {
            System.out.println("Ödeme başarısız → " + result.getErrorMessage());
            return;
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User bulunamadı: " + userId));

        expireExpiredSubscriptions(user);

        int amount = result.getPaidPrice().intValue();
        PlanType planType = (amount == 200) ? PlanType.MONTHLY : PlanType.YEARLY;

        // Eski aktif aboneliklerin hepsini pasif yap
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndActiveTrue(user);
        for (Subscription old : activeSubs) old.setActive(false);
        if (!activeSubs.isEmpty()) subscriptionRepository.saveAll(activeSubs);


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

        //  Burada artık bizim entity Payment kullanılıyor
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

        expireExpiredSubscriptions(user);

        return subscriptionRepository
                .findFirstByUserAndActiveTrueOrderByEndDateDesc(user)
                .map(sub -> SubscriptionDto.builder()
                        .hasActiveSubscription(true)
                        .planType(sub.getPlanType().name())
                        .expiresAt(sub.getEndDate()) // şimdilik böyle
                        .features(getFeatures(sub.getPlanType()))
                        .build())
                .orElse(SubscriptionDto.builder()
                        .hasActiveSubscription(false)
                        .features(List.of())
                        .build());
    }

    private List<String> getFeatures(PlanType planType) {
        if (planType == PlanType.MONTHLY) {
            return List.of("Günlük 40 Mesaj", "Kelime Ezber", "Görevler");
        }
        if (planType == PlanType.TRIAL) {
            return List.of("Günlük 40 Mesaj", "Kelime Ezber", "Görevler",
                    "Reklamsız Kullanım", "Yıllık Özel İstatistikler", "Premium Rozet");
        }
        return List.of("Günlük 40 Mesaj", "Kelime Ezber", "Görevler",
                "Reklamsız Kullanım", "Yıllık Özel İstatistikler", "Premium Rozet");
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

    // -------------------------------------------------------
    // 5) CURRENT USER YARDIMCI METODU
    // -------------------------------------------------------
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + email));
    }

    // -------------------------------------------------------
    // 6) 3 GÜNLÜK TRIAL BAŞLATMA
    // -------------------------------------------------------
    public void startTrialForCurrentUser() {
        User user = getCurrentUser();

        expireExpiredSubscriptions(user);

        boolean hasUsedTrial = subscriptionRepository.existsByUserAndPlanType(user, PlanType.TRIAL);
        if (hasUsedTrial) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trial zaten kullanıldı");
        }

        boolean hasActiveSubscription =
                subscriptionRepository.findFirstByUserAndActiveTrueOrderByEndDateDesc(user).isPresent();

        if (hasActiveSubscription) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aktif bir aboneliğin varken ücretsiz deneme kullanamazsın"
            );
        }

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(3);

        Subscription trial = Subscription.builder()
                .user(user)
                .planType(PlanType.TRIAL)
                .startDate(start)
                .endDate(end)
                .active(true)
                .build();

        subscriptionRepository.save(trial);
    }
    private void expireExpiredSubscriptions(User user) {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expiredActives =
                subscriptionRepository.findByUserAndActiveTrueAndEndDateBefore(user, now);

        if (!expiredActives.isEmpty()) {
            for (Subscription s : expiredActives) {
                s.setActive(false);
            }
            subscriptionRepository.saveAll(expiredActives);
        }
    }

}