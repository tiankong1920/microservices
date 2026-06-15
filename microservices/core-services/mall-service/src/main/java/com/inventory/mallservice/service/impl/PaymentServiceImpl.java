package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.Payment;
import com.inventory.mallservice.exception.PaymentException;
import com.inventory.mallservice.repository.IPaymentRepository;
import com.inventory.mallservice.service.IPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 支付服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment createPayment(final Long orderId, final BigDecimal amount, final String paymentMethod) {
        log.info("Creating payment for order {} with amount {}", orderId, amount);
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("INIT");
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(final Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Payment", id));
    }

    @Override
    public Payment getPaymentByPaymentNo(final String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Payment", "paymentNo", paymentNo));
    }

    @Override
    public Payment getPaymentByOrderId(final Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Payment", "orderId", orderId));
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(final Long id, final String status, final String transactionId) {
        log.info("Updating payment {} status to {}", id, status);
        Payment payment = getPaymentById(id);
        payment.setPaymentStatus(status);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processPaymentSuccess(final String paymentNo, final String transactionId) {
        log.info("Processing payment success for paymentNo: {}", paymentNo);
        Payment payment = getPaymentByPaymentNo(paymentNo);
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionId(transactionId);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processPaymentFailed(final String paymentNo, final String reason) {
        log.info("Processing payment failed for paymentNo: {}, reason: {}", paymentNo, reason);
        Payment payment = getPaymentByPaymentNo(paymentNo);
        payment.setPaymentStatus("FAILED");
        return paymentRepository.save(payment);
    }

    @Override
    public Page<Payment> getPayments(final Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    @Override
    public List<Payment> getPaymentsByStatus(final String status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    @Override
    @Transactional
    public Payment refundPayment(final Long paymentId) {
        log.info("Processing refund for payment {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        if (!"SUCCESS".equals(payment.getPaymentStatus())) {
            throw PaymentException.onlySuccessfulCanBeRefunded();
        }
        payment.setPaymentStatus("REFUNDING");
        return paymentRepository.save(payment);
    }
}
