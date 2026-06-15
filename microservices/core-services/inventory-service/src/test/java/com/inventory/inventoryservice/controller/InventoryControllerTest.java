package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.BatchDTO;
import com.inventory.inventoryservice.dto.InventoryDTO;
import com.inventory.inventoryservice.dto.WarehouseDTO;
import com.inventory.inventoryservice.service.IBatchService;
import com.inventory.inventoryservice.service.IInventoryService;
import com.inventory.inventoryservice.service.IWarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
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
class InventoryControllerTest {

    @Mock
    private IInventoryService inventoryService;

    @Mock
    private IWarehouseService warehouseService;

    @Mock
    private IBatchService batchService;

    @InjectMocks
    private InventoryController inventoryController;

    private InventoryDTO testInventory;
    private WarehouseDTO testWarehouse;
    private BatchDTO testBatch;

    @BeforeEach
    void setUp() {
        testInventory = new InventoryDTO();
        testInventory.setId(1L);
        testInventory.setProductId(100L);
        testInventory.setWarehouseId(10L);
        testInventory.setQuantity(50);

        testWarehouse = new WarehouseDTO();
        testWarehouse.setId(10L);
        testWarehouse.setWarehouseName("Main Warehouse");
        testWarehouse.setWarehouseCode("WH-001");

        testBatch = new BatchDTO();
        testBatch.setId(100L);
        testBatch.setBatchCode("BATCH-001");
        testBatch.setProductId(100L);
    }

    @Test
    void testGetAllInventory() {
        when(inventoryService.getAllInventory()).thenReturn(List.of(testInventory));

        ResponseEntity<List<InventoryDTO>> response = inventoryController.getAllInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(50, response.getBody().get(0).getQuantity());
        verify(inventoryService).getAllInventory();
    }

    @Test
    void testGetInventoryById() {
        when(inventoryService.getInventoryById(1L)).thenReturn(testInventory);

        ResponseEntity<InventoryDTO> response = inventoryController.getInventoryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetInventoryByProductId() {
        when(inventoryService.getInventoryByProductId(100L)).thenReturn(List.of(testInventory));

        ResponseEntity<List<InventoryDTO>> response = inventoryController.getInventoryByProductId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).getProductId());
    }

    @Test
    void testGetInventoryByWarehouseId() {
        when(inventoryService.getInventoryByWarehouseId(10L)).thenReturn(List.of(testInventory));

        ResponseEntity<List<InventoryDTO>> response = inventoryController.getInventoryByWarehouseId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getWarehouseId());
    }

    @Test
    void testCreateInventory() {
        InventoryDTO input = new InventoryDTO();
        input.setProductId(200L);
        input.setWarehouseId(20L);
        input.setQuantity(100);

        InventoryDTO created = new InventoryDTO();
        created.setId(2L);
        created.setProductId(200L);
        created.setWarehouseId(20L);
        created.setQuantity(100);

        when(inventoryService.createInventory(any(InventoryDTO.class))).thenReturn(created);

        ResponseEntity<InventoryDTO> response = inventoryController.createInventory(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateInventory() {
        InventoryDTO input = new InventoryDTO();
        input.setQuantity(200);

        InventoryDTO updated = new InventoryDTO();
        updated.setId(1L);
        updated.setQuantity(200);

        when(inventoryService.updateInventory(eq(1L), any(InventoryDTO.class))).thenReturn(updated);

        ResponseEntity<InventoryDTO> response = inventoryController.updateInventory(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getQuantity());
    }

    @Test
    void testDeleteInventory() {
        doNothing().when(inventoryService).deleteInventory(1L);

        ResponseEntity<Void> response = inventoryController.deleteInventory(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(inventoryService).deleteInventory(1L);
    }

    @Test
    void testReserveInventory() {
        when(inventoryService.reserveInventory(100L, 10L, 5)).thenReturn(45);

        ResponseEntity<Integer> response = inventoryController.reserveInventory(100L, 10L, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(45, response.getBody());
    }

    @Test
    void testReleaseInventory() {
        when(inventoryService.releaseInventory(100L, 10L, 5)).thenReturn(55);

        ResponseEntity<Integer> response = inventoryController.releaseInventory(100L, 10L, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(55, response.getBody());
    }

    @Test
    void testAdjustInventory() {
        when(inventoryService.adjustInventory(100L, 10L, 10)).thenReturn(60);

        ResponseEntity<Integer> response = inventoryController.adjustInventory(100L, 10L, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(60, response.getBody());
    }

    @Test
    void testAdjustInventoryNegative() {
        when(inventoryService.adjustInventory(100L, 10L, -5)).thenReturn(45);

        ResponseEntity<Integer> response = inventoryController.adjustInventory(100L, 10L, -5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(45, response.getBody());
    }

    @Test
    void testGetAllWarehouses() {
        when(warehouseService.getAllWarehouses()).thenReturn(List.of(testWarehouse));

        ResponseEntity<List<WarehouseDTO>> response = inventoryController.getAllWarehouses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Main Warehouse", response.getBody().get(0).getWarehouseName());
    }

    @Test
    void testGetAllBatches() {
        when(batchService.getAllBatches()).thenReturn(List.of(testBatch));

        ResponseEntity<List<BatchDTO>> response = inventoryController.getAllBatches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetExpiringBatches() {
        LocalDate cutoff = LocalDate.of(2025, 12, 31);
        when(batchService.getBatchesExpiringBefore(cutoff)).thenReturn(List.of(testBatch));

        ResponseEntity<List<BatchDTO>> response = inventoryController.getExpiringBatches(cutoff);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(batchService).getBatchesExpiringBefore(cutoff);
    }
}
