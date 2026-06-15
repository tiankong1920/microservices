package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.StockTransferOrderDTO;
import com.inventory.inventoryservice.service.IStockTransferService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class StockTransferControllerTest {

    @Mock
    private IStockTransferService stockTransferService;

    @InjectMocks
    private StockTransferController stockTransferController;

    private StockTransferOrderDTO testTransfer;

    @BeforeEach
    void setUp() {
        testTransfer = new StockTransferOrderDTO();
        testTransfer.setId(1L);
        testTransfer.setTransferNumber("TR-001");
        testTransfer.setSourceWarehouseId(1L);
        testTransfer.setTargetWarehouseId(2L);
        testTransfer.setStatus("PENDING");
    }

    @Test
    void testGetAllStockTransferOrders() {
        when(stockTransferService.getAllStockTransferOrders()).thenReturn(List.of(testTransfer));

        ResponseEntity<List<StockTransferOrderDTO>> response =
                stockTransferController.getAllStockTransferOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetStockTransferOrderById() {
        when(stockTransferService.getStockTransferOrderById(1L)).thenReturn(testTransfer);

        ResponseEntity<StockTransferOrderDTO> response = stockTransferController.getStockTransferOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TR-001", response.getBody().getTransferNumber());
    }

    @Test
    void testGetStockTransferOrderByTransferNumber() {
        when(stockTransferService.getStockTransferOrderByTransferNumber("TR-001")).thenReturn(testTransfer);

        ResponseEntity<StockTransferOrderDTO> response =
                stockTransferController.getStockTransferOrderByTransferNumber("TR-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetStockTransferOrdersBySourceWarehouse() {
        when(stockTransferService.getStockTransferOrdersBySourceWarehouse(1L))
                .thenReturn(List.of(testTransfer));

        ResponseEntity<List<StockTransferOrderDTO>> response =
                stockTransferController.getStockTransferOrdersBySourceWarehouse(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetStockTransferOrdersByTargetWarehouse() {
        when(stockTransferService.getStockTransferOrdersByTargetWarehouse(2L))
                .thenReturn(List.of(testTransfer));

        ResponseEntity<List<StockTransferOrderDTO>> response =
                stockTransferController.getStockTransferOrdersByTargetWarehouse(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetStockTransferOrdersByStatus() {
        when(stockTransferService.getStockTransferOrdersByStatus("PENDING"))
                .thenReturn(List.of(testTransfer));

        ResponseEntity<List<StockTransferOrderDTO>> response =
                stockTransferController.getStockTransferOrdersByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().get(0).getStatus());
    }

    @Test
    void testCreateStockTransferOrder() {
        StockTransferOrderDTO input = new StockTransferOrderDTO();
        input.setTransferNumber("TR-002");
        input.setSourceWarehouseId(1L);
        input.setTargetWarehouseId(2L);

        StockTransferOrderDTO created = new StockTransferOrderDTO();
        created.setId(2L);
        created.setTransferNumber("TR-002");
        created.setStatus("PENDING");

        when(stockTransferService.createStockTransferOrder(any(StockTransferOrderDTO.class)))
                .thenReturn(created);

        ResponseEntity<StockTransferOrderDTO> response =
                stockTransferController.createStockTransferOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateStockTransferOrder() {
        StockTransferOrderDTO input = new StockTransferOrderDTO();
        input.setStatus("APPROVED");

        StockTransferOrderDTO updated = new StockTransferOrderDTO();
        updated.setId(1L);
        updated.setTransferNumber("TR-001");
        updated.setStatus("APPROVED");

        when(stockTransferService.updateStockTransferOrder(eq(1L), any(StockTransferOrderDTO.class)))
                .thenReturn(updated);

        ResponseEntity<StockTransferOrderDTO> response =
                stockTransferController.updateStockTransferOrder(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APPROVED", response.getBody().getStatus());
    }

    @Test
    void testDeleteStockTransferOrder() {
        doNothing().when(stockTransferService).deleteStockTransferOrder(1L);

        ResponseEntity<Void> response = stockTransferController.deleteStockTransferOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(stockTransferService).deleteStockTransferOrder(1L);
    }

    @Test
    void testUpdateStockTransferOrderStatus() {
        StockTransferOrderDTO updated = new StockTransferOrderDTO();
        updated.setId(1L);
        updated.setStatus("IN_TRANSIT");

        when(stockTransferService.updateStockTransferOrderStatus(1L, "IN_TRANSIT")).thenReturn(updated);

        ResponseEntity<StockTransferOrderDTO> response =
                stockTransferController.updateStockTransferOrderStatus(1L, "IN_TRANSIT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IN_TRANSIT", response.getBody().getStatus());
    }

    @Test
    void testGetTotalTransferByWarehouse() {
        when(stockTransferService.getTotalTransferByWarehouse(1L)).thenReturn(150);

        ResponseEntity<Integer> response = stockTransferController.getTotalTransferByWarehouse(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(150, response.getBody());
    }
}
