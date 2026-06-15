package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.SalesOrderDTO;
import com.inventory.salesservice.service.ISalesOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SalesOrderControllerTest {

    @Mock
    private ISalesOrderService salesOrderService;

    @InjectMocks
    private SalesOrderController salesOrderController;

    private SalesOrderDTO testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new SalesOrderDTO();
        testOrder.setId(1L);
        testOrder.setOrderNumber("SO-001");
        testOrder.setCustomerId(100L);
        testOrder.setStatus("PENDING");
    }

    @Test
    void testGetAllSalesOrders() {
        when(salesOrderService.getAllSalesOrders()).thenReturn(List.of(testOrder));

        ResponseEntity<List<SalesOrderDTO>> response = salesOrderController.getAllSalesOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesOrderById() {
        when(salesOrderService.getSalesOrderById(1L)).thenReturn(testOrder);

        ResponseEntity<SalesOrderDTO> response = salesOrderController.getSalesOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SO-001", response.getBody().getOrderNumber());
    }

    @Test
    void testGetSalesOrderByOrderNumber() {
        when(salesOrderService.getSalesOrderByOrderNumber("SO-001")).thenReturn(testOrder);

        ResponseEntity<SalesOrderDTO> response = salesOrderController.getSalesOrderByOrderNumber("SO-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetSalesOrdersByCustomerId() {
        when(salesOrderService.getSalesOrdersByCustomerId(100L)).thenReturn(List.of(testOrder));

        ResponseEntity<List<SalesOrderDTO>> response = salesOrderController.getSalesOrdersByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesOrdersByStatus() {
        when(salesOrderService.getSalesOrdersByStatus("PENDING")).thenReturn(List.of(testOrder));

        ResponseEntity<List<SalesOrderDTO>> response = salesOrderController.getSalesOrdersByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesOrdersByDateRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(salesOrderService.getSalesOrdersByDateRange(start, end)).thenReturn(List.of(testOrder));

        ResponseEntity<List<SalesOrderDTO>> response =
                salesOrderController.getSalesOrdersByDateRange(start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateSalesOrder() {
        SalesOrderDTO input = new SalesOrderDTO();
        input.setOrderNumber("SO-002");

        SalesOrderDTO created = new SalesOrderDTO();
        created.setId(2L);
        created.setOrderNumber("SO-002");

        when(salesOrderService.createSalesOrder(any(SalesOrderDTO.class))).thenReturn(created);

        ResponseEntity<SalesOrderDTO> response = salesOrderController.createSalesOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateSalesOrder() {
        SalesOrderDTO input = new SalesOrderDTO();
        input.setOrderNumber("SO-001-UP");

        SalesOrderDTO updated = new SalesOrderDTO();
        updated.setId(1L);
        updated.setOrderNumber("SO-001-UP");

        when(salesOrderService.updateSalesOrder(eq(1L), any(SalesOrderDTO.class))).thenReturn(updated);

        ResponseEntity<SalesOrderDTO> response = salesOrderController.updateSalesOrder(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SO-001-UP", response.getBody().getOrderNumber());
    }

    @Test
    void testDeleteSalesOrder() {
        doNothing().when(salesOrderService).deleteSalesOrder(1L);

        ResponseEntity<Void> response = salesOrderController.deleteSalesOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(salesOrderService).deleteSalesOrder(1L);
    }

    @Test
    void testUpdateSalesOrderStatus() {
        SalesOrderDTO updated = new SalesOrderDTO();
        updated.setId(1L);
        updated.setStatus("CONFIRMED");

        when(salesOrderService.updateSalesOrderStatus(eq(1L), eq("CONFIRMED"))).thenReturn(updated);

        ResponseEntity<SalesOrderDTO> response = salesOrderController.updateSalesOrderStatus(1L, "CONFIRMED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFIRMED", response.getBody().getStatus());
    }

    @Test
    void testGetTotalSalesByCustomerId() {
        when(salesOrderService.getTotalSalesByCustomerId(100L)).thenReturn(9999.99);

        ResponseEntity<Double> response = salesOrderController.getTotalSalesByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(9999.99, response.getBody());
    }
}
