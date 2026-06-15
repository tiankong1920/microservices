package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.PaymentDTO;
import com.inventory.financeservice.entity.Payment;
import com.inventory.financeservice.exception.PaymentNotFoundException;
import com.inventory.financeservice.repository.IPaymentRepository;
import com.inventory.financeservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentServiceImpl implements IPaymentService {

    private static final String LOG_PAYMENT_NOT_FOUND = "Payment not found with id: {}";

    private final IPaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PaymentDTO> getAllPayments() {
        log.info("Getting all payments");
        final List<Payment> payments = paymentRepository.findAll();
        log.info("Found {} payments", payments.size());
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        log.info("Getting payment by id: {}", id);
        final Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_PAYMENT_NOT_FOUND, id);
                    return new PaymentNotFoundException(id);
                });
        return modelMapper.map(payment, PaymentDTO.class);
    }

    @Override
    public PaymentDTO getPaymentByPaymentNumber(String paymentNumber) {
        log.info("Getting payment by payment number: {}", paymentNumber);
        final Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> {
                    log.error("Payment not found with payment number: {}", paymentNumber);
                    return new PaymentNotFoundException("paymentNumber", paymentNumber);
                });
        return modelMapper.map(payment, PaymentDTO.class);
    }

    @Override
    public List<PaymentDTO> getPaymentsBySupplierId(Long supplierId) {
        log.info("Getting payments by supplier id: {}", supplierId);
        final List<Payment> payments = paymentRepository.findBySupplierId(supplierId);
        log.info("Found {} payments for supplier id: {}", payments.size(), supplierId);
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting payments by date range: {} to {}", startDate, endDate);
        final List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        log.info("Found {} payments in date range", payments.size());
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    public List<PaymentDTO> getPaymentsByPaymentMethod(String paymentMethod) {
        log.info("Getting payments by payment method: {}", paymentMethod);
        final List<Payment> payments = paymentRepository.findByPaymentMethod(paymentMethod);
        log.info("Found {} payments with payment method: {}", payments.size(), paymentMethod);
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    public List<PaymentDTO> getPaymentsByPaymentStatus(String paymentStatus) {
        log.info("Getting payments by payment status: {}", paymentStatus);
        final List<Payment> payments = paymentRepository.findByPaymentStatus(paymentStatus);
        log.info("Found {} payments with payment status: {}", payments.size(), paymentStatus);
        return payments.stream()
                .map(payment -> modelMapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        log.info("Creating payment: {}", paymentDTO.getPaymentNumber());

        final Payment payment = modelMapper.map(paymentDTO, Payment.class);
        payment.setPaymentDate(LocalDateTime.now());
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus("PAID");
        }

        final Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with id: {}", savedPayment.getId());
        return modelMapper.map(savedPayment, PaymentDTO.class);
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        log.info("Updating payment with id: {}", id);

        final Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_PAYMENT_NOT_FOUND, id);
                    return new PaymentNotFoundException(id);
                });

        modelMapper.map(paymentDTO, existingPayment);
        final Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Payment updated successfully with id: {}", updatedPayment.getId());
        return modelMapper.map(updatedPayment, PaymentDTO.class);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        log.info("Deleting payment with id: {}", id);

        if (!paymentRepository.existsById(id)) {
            log.error(LOG_PAYMENT_NOT_FOUND, id);
            throw new PaymentNotFoundException(id);
        }

        paymentRepository.deleteById(id);
        log.info("Payment deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, String paymentStatus) {
        log.info("Updating payment status to: {} for id: {}", paymentStatus, id);

        final Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_PAYMENT_NOT_FOUND, id);
                    return new PaymentNotFoundException(id);
                });

        payment.setPaymentStatus(paymentStatus);
        final Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated successfully with id: {}", updatedPayment.getId());
        return modelMapper.map(updatedPayment, PaymentDTO.class);
    }

    @Override
    public BigDecimal getTotalPaymentBySupplierId(Long supplierId) {
        log.info("Getting total payment by supplier id: {}", supplierId);
        final List<Payment> payments = paymentRepository.findBySupplierId(supplierId);
        final BigDecimal total = payments.stream()
                .map(payment -> payment.getPaymentAmount() != null ? payment.getPaymentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total payment for supplier id {} is: {}", supplierId, total);
        return total;
    }
}
