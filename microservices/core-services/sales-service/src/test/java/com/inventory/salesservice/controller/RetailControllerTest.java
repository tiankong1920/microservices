package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.RetailOrderDTO;
import com.inventory.salesservice.service.IRetailService;
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
class RetailControllerTest {

    @Mock
    private IRetailService retailService;

    @InjectMocks
    private RetailController retailController;

    private RetailOrderDTO testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new RetailOrderDTO();
        testOrder.setId(1L);
        testOrder.setRetailNumber("RT-001");
        testOrder.setCustomerId(100L);
        testOrder.setPaymentStatus("UNPAID");
    }

    @Test
    void testGetAllRetailOrders() {
        when(retailService.getAllRetailOrders()).thenReturn(List.of(testOrder));

        ResponseEntity<List<RetailOrderDTO>> response = retailController.getAllRetailOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRetailOrderById() {
        when(retailService.getRetailOrderById(1L)).thenReturn(testOrder);

        ResponseEntity<RetailOrderDTO> response = retailController.getRetailOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RT-001", response.getBody().getRetailNumber());
    }

    @Test
    void testGetRetailOrderByRetailNumber() {
        when(retailService.getRetailOrderByRetailNumber("RT-001")).thenReturn(testOrder);

        ResponseEntity<RetailOrderDTO> response = retailController.getRetailOrderByRetailNumber("RT-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetRetailOrdersByCustomerId() {
        when(retailService.getRetailOrdersByCustomerId(100L)).thenReturn(List.of(testOrder));

        ResponseEntity<List<RetailOrderDTO>> response = retailController.getRetailOrdersByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRetailOrdersByWarehouseId() {
        when(retailService.getRetailOrdersByWarehouseId(200L)).thenReturn(List.of(testOrder));

        ResponseEntity<List<RetailOrderDTO>> response = retailController.getRetailOrdersByWarehouseId(200L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRetailOrdersByPaymentStatus() {
        when(retailService.getRetailOrdersByPaymentStatus("UNPAID")).thenReturn(List.of(testOrder));

        ResponseEntity<List<RetailOrderDTO>> response = retailController.getRetailOrdersByPaymentStatus("UNPAID");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRetailOrdersByDateRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(retailService.getRetailOrdersByDateRange(start, end)).thenReturn(List.of(testOrder));

        ResponseEntity<List<RetailOrderDTO>> response = retailController.getRetailOrdersByDateRange(start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateRetailOrder() {
        RetailOrderDTO input = new RetailOrderDTO();
        input.setRetailNumber("RT-002");

        RetailOrderDTO created = new RetailOrderDTO();
        created.setId(2L);
        created.setRetailNumber("RT-002");

        when(retailService.createRetailOrder(any(RetailOrderDTO.class))).thenReturn(created);

        ResponseEntity<RetailOrderDTO> response = retailController.createRetailOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateRetailOrder() {
        RetailOrderDTO input = new RetailOrderDTO();
        input.setRetailNumber("RT-001-UP");

        RetailOrderDTO updated = new RetailOrderDTO();
        updated.setId(1L);
        updated.setRetailNumber("RT-001-UP");

        when(retailService.updateRetailOrder(eq(1L), any(RetailOrderDTO.class))).thenReturn(updated);

        ResponseEntity<RetailOrderDTO> response = retailController.updateRetailOrder(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RT-001-UP", response.getBody().getRetailNumber());
    }

    @Test
    void testDeleteRetailOrder() {
        doNothing().when(retailService).deleteRetailOrder(1L);

        ResponseEntity<Void> response = retailController.deleteRetailOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(retailService).deleteRetailOrder(1L);
    }

    @Test
    void testUpdateRetailOrderPaymentStatus() {
        RetailOrderDTO updated = new RetailOrderDTO();
        updated.setId(1L);
        updated.setPaymentStatus("PAID");

        when(retailService.updateRetailOrderPaymentStatus(eq(1L), eq("PAID"))).thenReturn(updated);

        ResponseEntity<RetailOrderDTO> response = retailController.updateRetailOrderPaymentStatus(1L, "PAID");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAID", response.getBody().getPaymentStatus());
    }

    @Test
    void testGetTotalRetailByCustomerId() {
        when(retailService.getTotalRetailByCustomerId(100L)).thenReturn(1500.50);

        ResponseEntity<Double> response = retailController.getTotalRetailByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1500.50, response.getBody());
    }
}
