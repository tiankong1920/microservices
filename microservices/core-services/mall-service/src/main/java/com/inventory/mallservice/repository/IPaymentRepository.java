package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNo(String paymentNo);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findByOrderIdIn(List<Long> orderIds);
}
