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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class InventoryServiceImplTest {

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

    // Constants for magic numbers
    private static final Long NON_EXISTENT_ID = 999L;
    private static final Integer TEST_QUANTITY = 100;
    private static final Integer TEST_AVAILABLE_QUANTITY = 100;
    private static final Integer TEST_RESERVED_QUANTITY = 0;
    private static final Integer RESERVE_QUANTITY = 50;
    private static final Integer INSUFFICIENT_RESERVE_QUANTITY = 30;
    private static final Integer ADJUST_QUANTITY = 150;
    private static final Integer NEGATIVE_ADJUST_QUANTITY = -60;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, modelMapper, kafkaMessageService, objectMapper);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(1L);
        testInventory.setWarehouseId(1L);
        testInventory.setBatchId(1L);
        testInventory.setQuantity(TEST_QUANTITY);
        testInventory.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        testInventory.setReservedQuantity(TEST_RESERVED_QUANTITY);
        testInventory.setStatus("AVAILABLE");

        testInventoryDTO = new InventoryDTO();
        testInventoryDTO.setId(1L);
        testInventoryDTO.setProductId(1L);
        testInventoryDTO.setWarehouseId(1L);
        testInventoryDTO.setBatchId(1L);
        testInventoryDTO.setQuantity(TEST_QUANTITY);
        testInventoryDTO.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
testInventoryDTO.setReservedQuantity(TEST_RESERVED_QUANTITY);
        testInventoryDTO.setStatus("AVAILABLE");

        // Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(modelMapper.map(any(Inventory.class), eq(InventoryDTO.class))).thenReturn(testInventoryDTO);
        lenient().when(modelMapper.map(any(InventoryDTO.class), eq(Inventory.class))).thenReturn(testInventory);
        // Additional stubbing for update method (dto to existing entity mapping)
        lenient().doNothing().when(modelMapper).map(any(InventoryDTO.class), any(Inventory.class));
        
        // Mock KafkaMessageService
        lenient().doNothing().when(kafkaMessageService).sendMessage(any(String.class), any(String.class));
    }

    @Test
    void testGetInventoryById() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final InventoryDTO result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getWarehouseId());
        verify(inventoryRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testGetInventoryByIdNotFound() {
        when(inventoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.getInventoryById(NON_EXISTENT_ID);
        });

        verify(inventoryRepository, times(1)).findById(NON_EXISTENT_ID);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testGetInventoryByProductAndWarehouse() {
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final InventoryDTO result = inventoryService.getInventoryByProductAndWarehouse(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getWarehouseId());
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testGetInventoryByProductId() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Arrays.asList(testInventory));
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final List<InventoryDTO> result = inventoryService.getInventoryByProductId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductId());
        verify(inventoryRepository, times(1)).findByProductId(1L);
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testGetInventoryByWarehouseId() {
        when(inventoryRepository.findByWarehouseId(1L)).thenReturn(Arrays.asList(testInventory));
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final List<InventoryDTO> result = inventoryService.getInventoryByWarehouseId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getWarehouseId());
        verify(inventoryRepository, times(1)).findByWarehouseId(1L);
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }



    @Test
    void testCreateInventory() {
        when(modelMapper.map(testInventoryDTO, Inventory.class)).thenReturn(testInventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(modelMapper, times(1)).map(testInventoryDTO, Inventory.class);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testCreateInventoryWithAvailableQuantity() {
        testInventoryDTO.setAvailableQuantity(null);
        // Create a copy of testInventoryDTO with availableQuantity set to TEST_AVAILABLE_QUANTITY
        final InventoryDTO expectedDTO = new InventoryDTO();
        expectedDTO.setId(testInventoryDTO.getId());
        expectedDTO.setProductId(testInventoryDTO.getProductId());
        expectedDTO.setWarehouseId(testInventoryDTO.getWarehouseId());
        expectedDTO.setBatchId(testInventoryDTO.getBatchId());
        expectedDTO.setQuantity(testInventoryDTO.getQuantity());
        expectedDTO.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        expectedDTO.setReservedQuantity(testInventoryDTO.getReservedQuantity());
        expectedDTO.setStatus(testInventoryDTO.getStatus());

        when(modelMapper.map(testInventoryDTO, Inventory.class)).thenReturn(testInventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(expectedDTO);

        final InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertNotNull(result);
        assertEquals(TEST_AVAILABLE_QUANTITY, result.getAvailableQuantity());
        verify(modelMapper, times(1)).map(testInventoryDTO, Inventory.class);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testCreateInventoryWithReservedQuantity() {
        testInventoryDTO.setReservedQuantity(null);
        // Create a copy of testInventoryDTO with reservedQuantity set to TEST_RESERVED_QUANTITY
        final InventoryDTO expectedDTO = new InventoryDTO();
        expectedDTO.setId(testInventoryDTO.getId());
        expectedDTO.setProductId(testInventoryDTO.getProductId());
        expectedDTO.setWarehouseId(testInventoryDTO.getWarehouseId());
        expectedDTO.setBatchId(testInventoryDTO.getBatchId());
        expectedDTO.setQuantity(testInventoryDTO.getQuantity());
        expectedDTO.setAvailableQuantity(testInventoryDTO.getAvailableQuantity());
        expectedDTO.setReservedQuantity(TEST_RESERVED_QUANTITY);
        expectedDTO.setStatus(testInventoryDTO.getStatus());

        when(modelMapper.map(testInventoryDTO, Inventory.class)).thenReturn(testInventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(modelMapper.map(testInventory, InventoryDTO.class)).thenReturn(expectedDTO);

        final InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertNotNull(result);
        assertEquals(TEST_RESERVED_QUANTITY, result.getReservedQuantity());
        verify(modelMapper, times(1)).map(testInventoryDTO, Inventory.class);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(modelMapper, times(1)).map(testInventory, InventoryDTO.class);
    }

    @Test
    void testUpdateInventory() {
        final Inventory updatedInventory = new Inventory();
        updatedInventory.setId(1L);
        updatedInventory.setProductId(2L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);
        when(modelMapper.map(updatedInventory, InventoryDTO.class)).thenReturn(testInventoryDTO);

        final InventoryDTO result = inventoryService.updateInventory(1L, testInventoryDTO);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testUpdateInventoryNotFound() {
        when(inventoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.updateInventory(NON_EXISTENT_ID, testInventoryDTO);
        });

        verify(inventoryRepository, times(1)).findById(NON_EXISTENT_ID);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testDeleteInventory() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        doNothing().when(inventoryRepository).delete(any(Inventory.class));

        inventoryService.deleteInventory(1L);

        verify(inventoryRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).delete(any(Inventory.class));
    }

    @Test
    void testDeleteInventoryNotFound() {
        when(inventoryRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.deleteInventory(NON_EXISTENT_ID);
        });

        verify(inventoryRepository, times(1)).findById(NON_EXISTENT_ID);
        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void testReserveInventory() {
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            final Inventory inv = invocation.getArgument(0);
            inv.setAvailableQuantity(TEST_AVAILABLE_QUANTITY - RESERVE_QUANTITY);
            inv.setReservedQuantity(TEST_RESERVED_QUANTITY + RESERVE_QUANTITY);
            return inv;
        });

        final Integer result = inventoryService.reserveInventory(1L, 1L, RESERVE_QUANTITY);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReserveInventoryInsufficient() {
        testInventory.setAvailableQuantity(INSUFFICIENT_RESERVE_QUANTITY);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.reserveInventory(1L, 1L, RESERVE_QUANTITY);
        });

        assertTrue(exception.getMessage().contains("Insufficient inventory"));
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testReserveInventoryNotFound() {
        when(inventoryRepository.findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.reserveInventory(NON_EXISTENT_ID, 1L, RESERVE_QUANTITY);
        });

        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testReleaseInventory() {
        testInventory.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        testInventory.setReservedQuantity(RESERVE_QUANTITY);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            final Inventory inv = invocation.getArgument(0);
            inv.setAvailableQuantity(TEST_AVAILABLE_QUANTITY + RESERVE_QUANTITY);
            inv.setReservedQuantity(0);
            return inv;
        });

        final Integer result = inventoryService.releaseInventory(1L, 1L, RESERVE_QUANTITY);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReleaseInventoryInsufficientReserved() {
        testInventory.setAvailableQuantity(TEST_AVAILABLE_QUANTITY);
        testInventory.setReservedQuantity(INSUFFICIENT_RESERVE_QUANTITY);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.releaseInventory(1L, 1L, RESERVE_QUANTITY);
        });

        assertTrue(exception.getMessage().contains("Cannot release more than reserved"));
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testReleaseInventoryNotFound() {
        when(inventoryRepository.findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.releaseInventory(NON_EXISTENT_ID, 1L, RESERVE_QUANTITY);
        });

        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testAdjustInventory() {
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            final Inventory inv = invocation.getArgument(0);
            inv.setQuantity(TEST_QUANTITY + ADJUST_QUANTITY);
            inv.setAvailableQuantity(TEST_AVAILABLE_QUANTITY + ADJUST_QUANTITY);
            return inv;
        });

        final Integer result = inventoryService.adjustInventory(1L, 1L, ADJUST_QUANTITY);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testAdjustInventoryNegative() {
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            final Inventory inv = invocation.getArgument(0);
            inv.setQuantity(TEST_QUANTITY + NEGATIVE_ADJUST_QUANTITY);
            inv.setAvailableQuantity(TEST_AVAILABLE_QUANTITY + NEGATIVE_ADJUST_QUANTITY);
            return inv;
        });

        final Integer result = inventoryService.adjustInventory(1L, 1L, NEGATIVE_ADJUST_QUANTITY);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testAdjustInventoryNotFound() {
        when(inventoryRepository.findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.adjustInventory(NON_EXISTENT_ID, 1L, RESERVE_QUANTITY);
        });

        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(NON_EXISTENT_ID, 1L);
        verify(inventoryRepository, never()).save(any());
    }
}
