package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.SalesReturnOrderDTO;
import com.inventory.salesservice.service.ISalesReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
class SalesReturnControllerTest {

    @Mock
    private ISalesReturnService salesReturnService;

    @InjectMocks
    private SalesReturnController salesReturnController;

    private SalesReturnOrderDTO testReturn;

    @BeforeEach
    void setUp() {
        testReturn = new SalesReturnOrderDTO();
        testReturn.setId(1L);
        testReturn.setReturnNumber("SR-001");
        testReturn.setCustomerId(100L);
        testReturn.setStatus("PENDING");
    }

    @Test
    void testGetAllSalesReturnOrders() {
        when(salesReturnService.getAllSalesReturnOrders()).thenReturn(List.of(testReturn));

        ResponseEntity<List<SalesReturnOrderDTO>> response = salesReturnController.getAllSalesReturnOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesReturnOrderById() {
        when(salesReturnService.getSalesReturnOrderById(1L)).thenReturn(testReturn);

        ResponseEntity<SalesReturnOrderDTO> response = salesReturnController.getSalesReturnOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SR-001", response.getBody().getReturnNumber());
    }

    @Test
    void testGetSalesReturnOrderByReturnNumber() {
        when(salesReturnService.getSalesReturnOrderByReturnNumber("SR-001")).thenReturn(testReturn);

        ResponseEntity<SalesReturnOrderDTO> response =
                salesReturnController.getSalesReturnOrderByReturnNumber("SR-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetSalesReturnOrdersByCustomerId() {
        when(salesReturnService.getSalesReturnOrdersByCustomerId(100L)).thenReturn(List.of(testReturn));

        ResponseEntity<List<SalesReturnOrderDTO>> response =
                salesReturnController.getSalesReturnOrdersByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesReturnOrdersByWarehouseId() {
        when(salesReturnService.getSalesReturnOrdersByWarehouseId(200L)).thenReturn(List.of(testReturn));

        ResponseEntity<List<SalesReturnOrderDTO>> response =
                salesReturnController.getSalesReturnOrdersByWarehouseId(200L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesReturnOrdersByStatus() {
        when(salesReturnService.getSalesReturnOrdersByStatus("PENDING")).thenReturn(List.of(testReturn));

        ResponseEntity<List<SalesReturnOrderDTO>> response = salesReturnController.getSalesReturnOrdersByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSalesReturnOrdersByOriginalOrderId() {
        when(salesReturnService.getSalesReturnOrdersByOriginalOrderId(500L)).thenReturn(List.of(testReturn));

        ResponseEntity<List<SalesReturnOrderDTO>> response =
                salesReturnController.getSalesReturnOrdersByOriginalOrderId(500L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateSalesReturnOrder() {
        SalesReturnOrderDTO input = new SalesReturnOrderDTO();
        input.setReturnNumber("SR-002");

        SalesReturnOrderDTO created = new SalesReturnOrderDTO();
        created.setId(2L);
        created.setReturnNumber("SR-002");

        when(salesReturnService.createSalesReturnOrder(any(SalesReturnOrderDTO.class))).thenReturn(created);

        ResponseEntity<SalesReturnOrderDTO> response = salesReturnController.createSalesReturnOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateSalesReturnOrder() {
        SalesReturnOrderDTO input = new SalesReturnOrderDTO();
        input.setReturnNumber("SR-001-UP");

        SalesReturnOrderDTO updated = new SalesReturnOrderDTO();
        updated.setId(1L);
        updated.setReturnNumber("SR-001-UP");

        when(salesReturnService.updateSalesReturnOrder(eq(1L), any(SalesReturnOrderDTO.class))).thenReturn(updated);

        ResponseEntity<SalesReturnOrderDTO> response = salesReturnController.updateSalesReturnOrder(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SR-001-UP", response.getBody().getReturnNumber());
    }

    @Test
    void testDeleteSalesReturnOrder() {
        doNothing().when(salesReturnService).deleteSalesReturnOrder(1L);

        ResponseEntity<Void> response = salesReturnController.deleteSalesReturnOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(salesReturnService).deleteSalesReturnOrder(1L);
    }

    @Test
    void testUpdateSalesReturnOrderStatus() {
        SalesReturnOrderDTO updated = new SalesReturnOrderDTO();
        updated.setId(1L);
        updated.setStatus("APPROVED");

        when(salesReturnService.updateSalesReturnOrderStatus(eq(1L), eq("APPROVED"))).thenReturn(updated);

        ResponseEntity<SalesReturnOrderDTO> response =
                salesReturnController.updateSalesReturnOrderStatus(1L, "APPROVED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APPROVED", response.getBody().getStatus());
    }

    @Test
    void testGetTotalReturnByCustomerId() {
        when(salesReturnService.getTotalReturnByCustomerId(100L)).thenReturn(200.0);

        ResponseEntity<Double> response = salesReturnController.getTotalReturnByCustomerId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200.0, response.getBody());
    }
}
