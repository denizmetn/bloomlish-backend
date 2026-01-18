package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto;
import com.deniz.bloomlishbackend.dto.AdminPaymentRowDto;
import com.deniz.bloomlishbackend.dto.AdminUserRowDto;
import com.deniz.bloomlishbackend.entity.*;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.PaymentRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import com.deniz.bloomlishbackend.repository.spec.PaymentSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;


    public Page<AdminUserRowDto> listUsers(String q, int page, int size) {
        return userRepository.search(q, PageRequest.of(page, size))
                .map(u -> new AdminUserRowDto(
                        u.getUserID(),
                        u.getDisplayName(),
                        u.getEmail(),
                        normalizeRole(u.getRole()),
                        u.getCurrentLevel(),
                        u.getTotalXp(),
                        u.getWeeklyXp(),
                        u.getAccountStatus(),
                        u.isPremium(),
                        u.getCreatedAt()
                ));
    }

    public void changeUserRole(Long userId, String role) {
        String normalized = normalizeRole(role);
        if (!normalized.equals("ROLE_ADMIN")
                && !normalized.equals("ROLE_INSTRUCTOR")
                && !normalized.equals("ROLE_STUDENT")) {
            throw new IllegalArgumentException("Geçersiz rol");
        }

        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(normalized);
        userRepository.save(user);
    }

    private String normalizeRole(String role) {
        if (role == null) return "ROLE_STUDENT";
        return role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    }
    public Page<AdminEnrollmentRowDto> listEnrollments(Boolean paid, String q, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "enrolledAt")
        );

        // paid + q birlikte
        if (paid != null && q != null && !q.isBlank()) {
            return enrollmentRepository.searchAdminRowsByPaid(paid, q, pageable);
        }
        // sadece paid
        else if (paid != null) {
            return enrollmentRepository.findAdminRowsByPaid(paid, pageable);
        }
        // sadece q
        else if (q != null && !q.isBlank()) {
            return enrollmentRepository.searchAdminRows(q, pageable);
        }
        // hepsi
        else {
            return enrollmentRepository.findAdminRows(pageable);
        }
    }

    public void changeUserStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAccountStatus(status);
        userRepository.save(user);
    }

    public void setUserPremium(Long userId, boolean premium) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPremium(premium);
        userRepository.save(user);
    }

    public Page<AdminPaymentRowDto> listPayments(PaymentStatus status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paidAt"));

        Specification<Payment> spec =
                PaymentSpecifications.hasStatus(status)
                        .and(PaymentSpecifications.queryLikeEmailOrPlan(q));


        return paymentRepository.findAll(spec, pageable)
                .map(p -> AdminPaymentRowDto.builder()
                        .id(p.getId())
                        .user(p.getUser().getEmail())
                        .plan(p.getPlanType().name())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .status(p.getStatus().name())
                        .createdAt(p.getPaidAt())
                        .build());
    }


}