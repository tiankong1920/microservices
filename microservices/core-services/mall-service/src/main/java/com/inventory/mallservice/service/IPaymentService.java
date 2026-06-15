package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.Payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IPaymentService {

    Payment createPayment(Long orderId, BigDecimal amount, String paymentMethod);

    Payment getPaymentById(Long id);

    Payment getPaymentByPaymentNo(String paymentNo);

    Payment getPaymentByOrderId(Long orderId);

    Payment updatePaymentStatus(Long id, String status, String transactionId);

    Payment processPaymentSuccess(String paymentNo, String transactionId);

    Payment processPaymentFailed(String paymentNo, String reason);

    Page<Payment> getPayments(Pageable pageable);

    List<Payment> getPaymentsByStatus(String status);

    Payment refundPayment(Long paymentId);
}
