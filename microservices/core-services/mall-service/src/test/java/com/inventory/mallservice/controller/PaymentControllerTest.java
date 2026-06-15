package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Payment;
import com.inventory.mallservice.service.IPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PaymentControllerTest {

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void testCreatePayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.createPayment(anyLong(), any(BigDecimal.class), anyString())).thenReturn(payment);

        Map<String, Object> req = Map.of("orderId", 1L, "amount", 100, "paymentMethod", "ALIPAY");
        ResponseEntity<Payment> response = paymentController.createPayment(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.getPaymentById(anyLong())).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.getPayment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPaymentByNo() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.getPaymentByPaymentNo(anyString())).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.getPaymentByNo("PAY-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetPaymentByOrderId() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.getPaymentByOrderId(anyLong())).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.getPaymentByOrderId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetPayments() {
        Page<Payment> page = new PageImpl<>(List.of(new Payment()));
        when(paymentService.getPayments(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Payment>> response = paymentController.getPayments(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetPaymentsByStatus() {
        when(paymentService.getPaymentsByStatus(anyString())).thenReturn(List.of(new Payment()));

        ResponseEntity<List<Payment>> response = paymentController.getPaymentsByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testProcessPaymentSuccess() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.processPaymentSuccess(anyString(), anyString())).thenReturn(payment);

        Map<String, String> req = Map.of("paymentNo", "PAY-001", "transactionId", "TX-001");
        ResponseEntity<Payment> response = paymentController.processPaymentSuccess(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testProcessPaymentFailed() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.processPaymentFailed(anyString(), anyString())).thenReturn(payment);

        Map<String, String> req = Map.of("paymentNo", "PAY-001", "reason", "insufficient funds");
        ResponseEntity<Payment> response = paymentController.processPaymentFailed(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRefundPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.refundPayment(anyLong())).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.refundPayment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdatePaymentStatus() {
        Payment payment = new Payment();
        payment.setId(1L);
        when(paymentService.updatePaymentStatus(anyLong(), anyString(), any())).thenReturn(payment);

        Map<String, String> req = Map.of("status", "SUCCESS", "transactionId", "TX-001");
        ResponseEntity<Payment> response = paymentController.updatePaymentStatus(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
