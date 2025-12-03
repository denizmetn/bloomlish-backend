package com.deniz.bloomlishbackend.repository;


import com.deniz.bloomlishbackend.entity.Subscription;

import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
   Optional<Subscription> findByUserAndActiveTrue(User user);
    List<Subscription> findByUser(User user);
}
