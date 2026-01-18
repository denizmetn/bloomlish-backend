package com.deniz.bloomlishbackend.repository.spec;

import com.deniz.bloomlishbackend.entity.Payment;
import com.deniz.bloomlishbackend.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecifications {
    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> queryLikeEmailOrPlan(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.toLowerCase().trim() + "%";

            var userJoin = root.join("user"); // Payment.user
            return cb.or(
                    cb.like(cb.lower(userJoin.get("email")), like),
                    cb.like(cb.lower(root.get("planType").as(String.class)), like)
            );
        };
    }
}
