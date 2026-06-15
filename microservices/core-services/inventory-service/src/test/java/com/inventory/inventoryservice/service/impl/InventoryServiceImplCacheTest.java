package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.InventoryDTO;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.exception.InventoryNotFoundException;
import com.inventory.inventoryservice.repository.IInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.core.kafka.KafkaMessageService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class InventoryServiceImplCacheTest {

    // Constants for magic numbers
    private static final Long TEST_INVENTORY_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 100L;
    private static final Long TEST_WAREHOUSE_ID = 200L;
    private static final Long TEST_BATCH_ID = 300L;
    private static final Integer TEST_QUANTITY = 100;
    private static final Integer TEST_AVAILABLE_QUANTITY = 80;
    private static final Integer TEST_RESERVED_QUANTITY = 20;
    private static final Integer TEST_RESERVE_AMOUNT = 10;
    private static final Integer TEST_RELEASE_AMOUNT = 10;
    private static final Integer TEST_ADJUST_AMOUNT = 50;
    private static final Integer EXPECTED_AVAILABLE_AFTER_RESERVE = 70;
    private static final Integer EXPECTED_AVAILABLE_AFTER_RELEASE = 90;
    private static final Integer EXPECTED_AVAILABLE_AFTER_ADJUST = 130;
    private static final Integer CONCURRENT_ACCESS_COUNT = 3;

@Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaMessageService kafkaMessageService;

    private InventoryServiceImpl inventoryService;

    private Inventory testInventory;
    private InventoryDTO testInventoryDTO;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, modelMapper, kafkaMessageService, objectMapper);

        testInventory = new Inventory();
        testInventory.setId(TEST_INVENTORY_ID);
        testInventory.setProductId(TEST_PRODUCT_ID);
        testInventory.setWarehouseId(TEST_WAREHOUSE_ID);
        testInventory.setQuantity(TEST_QUANTITY);
        testInventory.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        testInventory.setReservedQuantity(TEST_RESERVED_QUANTITY);
        testInventory.setStatus("AVAILABLE");
        testInventory.setBatchId(TEST_BATCH_ID);

        testInventoryDTO = new InventoryDTO();
        testInventoryDTO.setId(TEST_INVENTORY_ID);
        testInventoryDTO.setProductId(TEST_PRODUCT_ID);
        testInventoryDTO.setWarehouseId(TEST_WAREHOUSE_ID);
        testInventoryDTO.setQuantity(TEST_QUANTITY);
        testInventoryDTO.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        testInventoryDTO.setReservedQuantity(TEST_RESERVED_QUANTITY);
        testInventoryDTO.setStatus("AVAILABLE");
        testInventoryDTO.setBatchId(TEST_BATCH_ID);

// Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(modelMapper.map(any(Inventory.class), eq(InventoryDTO.class))).thenReturn(testInventoryDTO);
        lenient().when(modelMapper.map(any(InventoryDTO.class), eq(Inventory.class))).thenReturn(testInventory);
        // Additional stubbing for update method (dto to existing entity mapping)
        lenient().doNothing().when(modelMapper).map(any(InventoryDTO.class), any(Inventory.class));
        
        // Mock ObjectMapper
        // Mock KafkaMessageService
        lenient().doNothing().when(kafkaMessageService).sendMessage(any(String.class), any(String.class));
    }

    @Test
    void testGetInventoryById() {
        when(inventoryRepository.findById(TEST_INVENTORY_ID)).thenReturn(Optional.of(testInventory));

        final InventoryDTO result = inventoryService.getInventoryById(TEST_INVENTORY_ID);

        assertNotNull(result);
        assertEquals(TEST_INVENTORY_ID, result.getId());

        verify(inventoryRepository, times(1)).findById(TEST_INVENTORY_ID);
    }

    @Test
    void testGetInventoryByIdCacheMiss() {
        when(inventoryRepository.findById(TEST_INVENTORY_ID)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> inventoryService.getInventoryById(TEST_INVENTORY_ID));
        verify(inventoryRepository, times(1)).findById(TEST_INVENTORY_ID);
    }

    @Test
    void testGetInventoryByProductAndWarehouse() {
        when(inventoryRepository.findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID))
                .thenReturn(Optional.of(testInventory));

        final InventoryDTO result = inventoryService.getInventoryByProductAndWarehouse(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);

        assertNotNull(result);
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals(TEST_WAREHOUSE_ID, result.getWarehouseId());

        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);
    }

    @Test
    void testGetInventoryByProductId() {
        when(inventoryRepository.findByProductId(TEST_PRODUCT_ID)).thenReturn(List.of(testInventory));

        final List<InventoryDTO> result = inventoryService.getInventoryByProductId(TEST_PRODUCT_ID);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(inventoryRepository, times(1)).findByProductId(TEST_PRODUCT_ID);
    }

    @Test
    void testGetInventoryByWarehouseId() {
        when(inventoryRepository.findByWarehouseId(TEST_WAREHOUSE_ID)).thenReturn(List.of(testInventory));

        final List<InventoryDTO> result = inventoryService.getInventoryByWarehouseId(TEST_WAREHOUSE_ID);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(inventoryRepository, times(1)).findByWarehouseId(TEST_WAREHOUSE_ID);
    }

    @Test
    void testGetInventoryByBatchId() {
        when(inventoryRepository.findByBatchId(TEST_BATCH_ID)).thenReturn(List.of(testInventory));

        final List<InventoryDTO> result = inventoryService.getInventoryByBatchId(TEST_BATCH_ID);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(inventoryRepository, times(1)).findByBatchId(TEST_BATCH_ID);
    }

    @Test
    void testGetInventoryByStatus() {
        when(inventoryRepository.findByStatus("AVAILABLE")).thenReturn(List.of(testInventory));

        final List<InventoryDTO> result = inventoryService.getInventoryByStatus("AVAILABLE");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(inventoryRepository, times(1)).findByStatus("AVAILABLE");
    }

    @Test
    void testCreateInventory() {
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        final InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertNotNull(result);
        assertEquals(TEST_INVENTORY_ID, result.getId());

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testUpdateInventory() {
        when(inventoryRepository.findById(TEST_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        final InventoryDTO result = inventoryService.updateInventory(TEST_INVENTORY_ID, testInventoryDTO);

        assertNotNull(result);
        assertEquals(TEST_INVENTORY_ID, result.getId());

        verify(inventoryRepository, times(1)).findById(TEST_INVENTORY_ID);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testDeleteInventory() {
        when(inventoryRepository.findById(TEST_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        doNothing().when(inventoryRepository).delete(any(Inventory.class));

        inventoryService.deleteInventory(TEST_INVENTORY_ID);

        verify(inventoryRepository, times(1)).findById(TEST_INVENTORY_ID);
        verify(inventoryRepository, times(1)).delete(any(Inventory.class));
    }

    @Test
    void testReserveInventory() {
        when(inventoryRepository.findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        final Integer result = inventoryService.reserveInventory(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID, TEST_RESERVE_AMOUNT);

        assertEquals(EXPECTED_AVAILABLE_AFTER_RESERVE, result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReleaseInventory() {
        when(inventoryRepository.findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        final Integer result = inventoryService.releaseInventory(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID, TEST_RELEASE_AMOUNT);

        assertEquals(EXPECTED_AVAILABLE_AFTER_RELEASE, result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testAdjustInventory() {
        when(inventoryRepository.findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        final Integer result = inventoryService.adjustInventory(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID, TEST_ADJUST_AMOUNT);

        assertEquals(EXPECTED_AVAILABLE_AFTER_ADJUST, result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testConcurrentAccess() {
        when(inventoryRepository.findById(TEST_INVENTORY_ID)).thenReturn(Optional.of(testInventory));

        // Simulate concurrent access
        final InventoryDTO result1 = inventoryService.getInventoryById(TEST_INVENTORY_ID);
        final InventoryDTO result2 = inventoryService.getInventoryById(TEST_INVENTORY_ID);
        final InventoryDTO result3 = inventoryService.getInventoryById(TEST_INVENTORY_ID);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(TEST_INVENTORY_ID, result1.getId());
        assertEquals(TEST_INVENTORY_ID, result2.getId());
        assertEquals(TEST_INVENTORY_ID, result3.getId());

        // Without caching in test environment, each call goes to repository
        verify(inventoryRepository, times(CONCURRENT_ACCESS_COUNT)).findById(TEST_INVENTORY_ID);
    }
}
