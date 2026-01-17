package com.deniz.bloomlishbackend.controller;

import com.deniz.bloomlishbackend.dto.AdminPaymentRowDto;
import com.deniz.bloomlishbackend.entity.AccountStatus;
import com.deniz.bloomlishbackend.entity.PaymentStatus;
import com.deniz.bloomlishbackend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminService;

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestParam(required = false) String q,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(q, page, size));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long userId,
                                        @RequestBody ChangeRoleRequest req) {
        adminService.changeUserRole(userId, req.role());
        return ResponseEntity.ok().build();
    }

    public record ChangeRoleRequest(String role) {}

    // -------- ENROLLMENTS (Purchases/Payments) --------
    @GetMapping("/enrollments")
    public ResponseEntity<?> listEnrollments(@RequestParam(required = false) Boolean paid,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listEnrollments(paid, q, page, size));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long userId,
                                          @RequestBody ChangeStatusRequest req) {
        adminService.changeUserStatus(userId, req.status());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{userId}/premium")
    public ResponseEntity<?> setPremium(@PathVariable Long userId,
                                        @RequestBody SetPremiumRequest req) {
        adminService.setUserPremium(userId, req.premium());
        return ResponseEntity.ok().build();
    }

    public record ChangeStatusRequest(AccountStatus status) {}
    public record SetPremiumRequest(boolean premium) {}

    @GetMapping("/payments")
    public Page<AdminPaymentRowDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String query
    ) {
        return adminService.listPayments(status, query, page, size);

    }

}

