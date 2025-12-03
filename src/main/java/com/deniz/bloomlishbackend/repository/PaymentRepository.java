package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Payment;

import com.deniz.bloomlishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository <Payment, Long> {
    List<Payment> findByUserOrderByPaidAtDesc(User user);
}
