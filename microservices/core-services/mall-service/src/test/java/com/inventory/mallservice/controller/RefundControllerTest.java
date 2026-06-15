package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.RefundApplication;
import com.inventory.mallservice.service.IRefundService;
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
class RefundControllerTest {

    @Mock
    private IRefundService refundService;

    @InjectMocks
    private RefundController refundController;

    @Test
    void testCreateRefund() {
        RefundApplication app = new RefundApplication();
        app.setId(1L);
        when(refundService.createRefundApplication(anyLong(), anyLong(), anyString(),
                any(BigDecimal.class), anyString())).thenReturn(app);

        Map<String, Object> req = Map.of(
                "orderId", 1L,
                "userId", 10L,
                "refundType", "FULL",
                "refundAmount", 100,
                "reason", "damaged");
        ResponseEntity<RefundApplication> response = refundController.createRefund(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testApproveRefund() {
        RefundApplication app = new RefundApplication();
        app.setId(1L);
        when(refundService.approveRefund(anyLong(), anyLong(), anyString())).thenReturn(app);

        Map<String, Object> req = Map.of("auditorId", 99L, "notes", "ok");
        ResponseEntity<RefundApplication> response = refundController.approveRefund(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRejectRefund() {
        RefundApplication app = new RefundApplication();
        app.setId(1L);
        when(refundService.rejectRefund(anyLong(), anyLong(), anyString())).thenReturn(app);

        Map<String, Object> req = Map.of("auditorId", 99L, "notes", "rejected");
        ResponseEntity<RefundApplication> response = refundController.rejectRefund(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testProcessRefund() {
        RefundApplication app = new RefundApplication();
        app.setId(1L);
        when(refundService.processRefund(anyLong())).thenReturn(app);

        ResponseEntity<RefundApplication> response = refundController.processRefund(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetRefund() {
        RefundApplication app = new RefundApplication();
        app.setId(1L);
        when(refundService.getRefundById(anyLong())).thenReturn(app);

        ResponseEntity<RefundApplication> response = refundController.getRefund(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetRefundsByStatus() {
        Page<RefundApplication> page = new PageImpl<>(List.of(new RefundApplication()));
        when(refundService.getRefundsByStatus(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<RefundApplication>> response = refundController.getRefundsByStatus("PENDING", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetUserRefunds() {
        Page<RefundApplication> page = new PageImpl<>(List.of(new RefundApplication()));
        when(refundService.getRefundsByUserId(anyLong(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<RefundApplication>> response = refundController.getUserRefunds(1L, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetOrderRefunds() {
        when(refundService.getRefundsByOrderId(anyLong())).thenReturn(List.of(new RefundApplication()));

        ResponseEntity<List<RefundApplication>> response = refundController.getOrderRefunds(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
