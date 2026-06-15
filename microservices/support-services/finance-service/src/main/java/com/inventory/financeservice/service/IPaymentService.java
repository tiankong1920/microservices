package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.PaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IPaymentService {

    List<PaymentDTO> getAllPayments();

    PaymentDTO getPaymentById(Long id);

    PaymentDTO getPaymentByPaymentNumber(String paymentNumber);

    List<PaymentDTO> getPaymentsBySupplierId(Long supplierId);

    List<PaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<PaymentDTO> getPaymentsByPaymentMethod(String paymentMethod);

    List<PaymentDTO> getPaymentsByPaymentStatus(String paymentStatus);

    PaymentDTO createPayment(PaymentDTO paymentDTO);

    PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO);

    void deletePayment(Long id);

    PaymentDTO updatePaymentStatus(Long id, String paymentStatus);

    BigDecimal getTotalPaymentBySupplierId(Long supplierId);
}
