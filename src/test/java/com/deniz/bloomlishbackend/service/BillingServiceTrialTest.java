package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.entity.PlanType;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.PaymentRepository;
import com.deniz.bloomlishbackend.repository.SubscriptionRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.iyzipay.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class BillingServiceTrialTest {

    @Mock Options iyzicoOptions; // ctor istiyor
    @Mock UserRepository userRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks BillingService billingService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthEmail(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, "pw", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void startTrial_trialAlreadyUsed_shouldThrow409() {
        String email = "a@b.com";
        setAuthEmail(email);
        User user = User.builder().userID(1L).email(email).role("STUDENT").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserAndActiveTrueAndEndDateBefore(eq(user), any()))
                .thenReturn(java.util.List.of());
        when(subscriptionRepository.existsByUserAndPlanType(user, PlanType.TRIAL))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billingService.startTrialForCurrentUser());

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().toLowerCase().contains("trial"));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void startTrial_hasActiveSubscription_shouldThrow409() {
        // given
        String email = "a@b.com";
        setAuthEmail(email);

        User user = User.builder().userID(1L).email(email).role("STUDENT").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserAndActiveTrueAndEndDateBefore(eq(user), any()))
                .thenReturn(java.util.List.of()); // expireExpiredSubscriptions içi
        when(subscriptionRepository.existsByUserAndPlanType(user, PlanType.TRIAL))
                .thenReturn(false);

        when(subscriptionRepository.findFirstByUserAndActiveTrueOrderByEndDateDesc(user))
                .thenReturn(Optional.of(mock(com.deniz.bloomlishbackend.entity.Subscription.class)));

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billingService.startTrialForCurrentUser());

        // then
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertNotNull(ex.getReason());
        verify(subscriptionRepository, never()).save(any());
    }
}
