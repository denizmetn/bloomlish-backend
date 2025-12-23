package com.deniz.bloomlishbackend.service;

import com.deniz.bloomlishbackend.dto.AdminEnrollmentRowDto;
import com.deniz.bloomlishbackend.dto.AdminUserRowDto;
import com.deniz.bloomlishbackend.entity.AccountStatus;
import com.deniz.bloomlishbackend.entity.Enrollment;
import com.deniz.bloomlishbackend.entity.User;
import com.deniz.bloomlishbackend.repository.EnrollmentRepository;
import com.deniz.bloomlishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

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
        Page<Enrollment> result;

        // paid + q birlikte
        if (paid != null && q != null && !q.isBlank()) {
            result = enrollmentRepository.searchByPaidWithStudentAndLesson(paid, q, PageRequest.of(page, size));
        }
        // sadece paid
        else if (paid != null) {
            result = enrollmentRepository.findByPaidWithStudentAndLesson(paid, PageRequest.of(page, size));
        }
        // sadece q
        else if (q != null && !q.isBlank()) {
            result = enrollmentRepository.searchWithStudentAndLesson(q, PageRequest.of(page, size));
        }
        // hepsi
        else {
            result = enrollmentRepository.findAllWithStudentAndLesson(PageRequest.of(page, size));
        }

        return result.map(e -> new AdminEnrollmentRowDto(
                e.getId(),
                e.getStudent().getEmail(),
                e.getStudent().getDisplayName(),
                e.getLesson().getName(),
                e.getLesson().getPrice(),
                e.isPaid(),
                e.getEnrolledAt()
        ));
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
}