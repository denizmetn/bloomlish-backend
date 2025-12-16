package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.PlanType;
import com.deniz.bloomlishbackend.entity.Subscription;
import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Tüm aktif abonelikleri liste olarak getir
    List<Subscription> findByUserAndActiveTrue(User user);

    List<Subscription> findByUser(User user);

    // trial daha önce kullanılmış mı?
    boolean existsByUserAndPlanType(User user, PlanType planType);

    // En güncel aktif aboneliği getir (abonelik bilgisi sorgusunda kullandık)
    Optional<Subscription> findFirstByUserAndActiveTrueOrderByEndDateDesc(User user);
    List<Subscription> findByUserAndActiveTrueAndEndDateBefore(User user, LocalDateTime now);
}
