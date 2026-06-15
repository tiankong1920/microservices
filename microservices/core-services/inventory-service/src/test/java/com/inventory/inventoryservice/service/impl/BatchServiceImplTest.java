package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.BatchDTO;
import com.inventory.inventoryservice.entity.Batch;
import com.inventory.inventoryservice.exception.BatchNotFoundException;
import com.inventory.inventoryservice.repository.IBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class BatchServiceImplTest {

    // Constants for magic numbers
    private static final Long TEST_BATCH_ID = 1L;
    private static final Long NON_EXISTENT_BATCH_ID = 999L;
    private static final Long TEST_PRODUCT_ID = 1L;
    private static final Long TEST_WAREHOUSE_ID = 1L;
    private static final Long TEST_SUPPLIER_ID = 1L;
    private static final Integer TEST_QUANTITY = 100;
    private static final Integer UPDATED_QUANTITY = 200;
    private static final Integer TEST_DAYS_TO_EXPIRY = 30;

    @Mock
    private IBatchRepository batchRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BatchServiceImpl batchService;

    private Batch testBatch;
    private BatchDTO testBatchDTO;
    private BatchDTO updateBatchDTO;
    private BatchDTO updatedBatchDTO;
    private Batch updatedBatch;

    @BeforeEach
    void setUp() {
        testBatch = new Batch();
        testBatch.setId(TEST_BATCH_ID);
        testBatch.setBatchCode("BATCH-001");
        testBatch.setProductId(TEST_PRODUCT_ID);
        testBatch.setWarehouseId(TEST_WAREHOUSE_ID);
        testBatch.setSupplierId(TEST_SUPPLIER_ID);
        testBatch.setQuantity(TEST_QUANTITY);
        testBatch.setAvailableQuantity(TEST_QUANTITY);
        testBatch.setExpiryDate(LocalDate.now().plusDays(TEST_DAYS_TO_EXPIRY));
        testBatch.setStatus("ACTIVE");

        testBatchDTO = new BatchDTO();
        testBatchDTO.setId(TEST_BATCH_ID);
        testBatchDTO.setBatchCode("BATCH-001");
        testBatchDTO.setProductId(TEST_PRODUCT_ID);
        testBatchDTO.setWarehouseId(TEST_WAREHOUSE_ID);
        testBatchDTO.setSupplierId(TEST_SUPPLIER_ID);
        testBatchDTO.setQuantity(TEST_QUANTITY);
        testBatchDTO.setAvailableQuantity(TEST_QUANTITY);
        testBatchDTO.setExpiryDate(LocalDate.now().plusDays(TEST_DAYS_TO_EXPIRY));
        testBatchDTO.setStatus("ACTIVE");

        // Initialize updatedBatchDTO for update tests
        updatedBatchDTO = new BatchDTO();
        updatedBatchDTO.setId(TEST_BATCH_ID);
        updatedBatchDTO.setBatchCode("BATCH-001-UPDATED");
        updatedBatchDTO.setProductId(TEST_PRODUCT_ID);
        updatedBatchDTO.setWarehouseId(TEST_WAREHOUSE_ID);
        updatedBatchDTO.setSupplierId(TEST_SUPPLIER_ID);
        updatedBatchDTO.setQuantity(UPDATED_QUANTITY);
        updatedBatchDTO.setAvailableQuantity(UPDATED_QUANTITY);
        updatedBatchDTO.setExpiryDate(LocalDate.now().plusDays(TEST_DAYS_TO_EXPIRY * 2));
        updatedBatchDTO.setStatus("ACTIVE");

        // Initialize updateBatchDTO for update tests
        updateBatchDTO = new BatchDTO();
        updateBatchDTO.setId(TEST_BATCH_ID);
        updateBatchDTO.setBatchCode("BATCH-001-UPDATED");
        updateBatchDTO.setQuantity(UPDATED_QUANTITY);
        updateBatchDTO.setAvailableQuantity(UPDATED_QUANTITY);
        updateBatchDTO.setStatus("INACTIVE");

        updatedBatch = new Batch();
        updatedBatch.setId(TEST_BATCH_ID);
        updatedBatch.setBatchCode("BATCH-001-UPDATED");
        updatedBatch.setProductId(TEST_PRODUCT_ID);
        updatedBatch.setWarehouseId(TEST_WAREHOUSE_ID);
        updatedBatch.setSupplierId(TEST_SUPPLIER_ID);
        updatedBatch.setQuantity(UPDATED_QUANTITY);
        updatedBatch.setAvailableQuantity(UPDATED_QUANTITY);
        updatedBatch.setExpiryDate(LocalDate.now().plusDays(TEST_DAYS_TO_EXPIRY * 2));
        updatedBatch.setStatus("ACTIVE");
    }

    @Test
    void testGetAllBatches() {
        when(batchRepository.findAll()).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getAllBatches();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BATCH-001", result.get(0).getBatchCode());
        verify(batchRepository, times(1)).findAll();
    }

    @Test
    void testGetBatchById() {
        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final BatchDTO result = batchService.getBatchById(TEST_BATCH_ID);

        assertNotNull(result);
        assertEquals(TEST_BATCH_ID, result.getId());
        assertEquals("BATCH-001", result.getBatchCode());
        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchByIdNotFound() {
        when(batchRepository.findById(NON_EXISTENT_BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.getBatchById(NON_EXISTENT_BATCH_ID);
        });

        verify(batchRepository, times(1)).findById(NON_EXISTENT_BATCH_ID);
    }

    @Test
    void testGetBatchByCode() {
        when(batchRepository.findByBatchCode("BATCH-001")).thenReturn(Optional.of(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final BatchDTO result = batchService.getBatchByCode("BATCH-001");

        assertNotNull(result);
        assertEquals(TEST_BATCH_ID, result.getId());
        assertEquals("BATCH-001", result.getBatchCode());
        verify(batchRepository, times(1)).findByBatchCode("BATCH-001");
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchByCodeNotFound() {
        when(batchRepository.findByBatchCode("NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.getBatchByCode("NOTFOUND");
        });

        verify(batchRepository, times(1)).findByBatchCode("NOTFOUND");
    }

    @Test
    void testGetBatchesByProductId() {
        when(batchRepository.findByProductId(TEST_PRODUCT_ID)).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesByProductId(TEST_PRODUCT_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_PRODUCT_ID, result.get(0).getProductId());
        verify(batchRepository, times(1)).findByProductId(TEST_PRODUCT_ID);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchesByWarehouseId() {
        when(batchRepository.findByWarehouseId(TEST_WAREHOUSE_ID)).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesByWarehouseId(TEST_WAREHOUSE_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_WAREHOUSE_ID, result.get(0).getWarehouseId());
        verify(batchRepository, times(1)).findByWarehouseId(TEST_WAREHOUSE_ID);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchesBySupplierId() {
        when(batchRepository.findBySupplierId(TEST_SUPPLIER_ID)).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesBySupplierId(TEST_SUPPLIER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_SUPPLIER_ID, result.get(0).getSupplierId());
        verify(batchRepository, times(1)).findBySupplierId(TEST_SUPPLIER_ID);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchesByStatus() {
        when(batchRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesByStatus("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(batchRepository, times(1)).findByStatus("ACTIVE");
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchesExpiringBefore() {
        final LocalDate expiryDate = LocalDate.now().plusDays(TEST_DAYS_TO_EXPIRY);
        when(batchRepository.findByExpiryDateBefore(expiryDate)).thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesExpiringBefore(expiryDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(batchRepository, times(1)).findByExpiryDateBefore(expiryDate);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testGetBatchesByProductAndWarehouse() {
        when(batchRepository.findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID))
                .thenReturn(Arrays.asList(testBatch));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final List<BatchDTO> result = batchService.getBatchesByProductAndWarehouse(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_PRODUCT_ID, result.get(0).getProductId());
        assertEquals(TEST_WAREHOUSE_ID, result.get(0).getWarehouseId());
        verify(batchRepository, times(1)).findByProductIdAndWarehouseId(
                TEST_PRODUCT_ID, TEST_WAREHOUSE_ID);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testCreateBatch() {
        when(batchRepository.existsByBatchCode("BATCH-001")).thenReturn(false);
        when(modelMapper.map(testBatchDTO, Batch.class)).thenReturn(testBatch);
        when(batchRepository.save(any(Batch.class))).thenReturn(testBatch);
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(testBatchDTO);

        final BatchDTO result = batchService.createBatch(testBatchDTO);

        assertNotNull(result);
        assertEquals(TEST_BATCH_ID, result.getId());
        assertEquals("BATCH-001", result.getBatchCode());
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals(TEST_WAREHOUSE_ID, result.getWarehouseId());
        assertEquals(TEST_SUPPLIER_ID, result.getSupplierId());
        assertEquals(TEST_QUANTITY, result.getQuantity());
        assertEquals(TEST_QUANTITY, result.getAvailableQuantity());
        assertEquals("ACTIVE", result.getStatus());

        verify(batchRepository, times(1)).existsByBatchCode("BATCH-001");
        verify(batchRepository, times(1)).save(any(Batch.class));
        verify(modelMapper, times(1)).map(testBatchDTO, Batch.class);
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testCreateBatchBatchCodeExists() {
        when(batchRepository.existsByBatchCode("BATCH-001")).thenReturn(true);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.createBatch(testBatchDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(batchRepository, times(1)).existsByBatchCode("BATCH-001");
        verify(batchRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateBatch() {
        updateBatchDTO.setId(TEST_BATCH_ID);
        updateBatchDTO.setBatchCode("BATCH-001-UPDATED");
        updateBatchDTO.setQuantity(UPDATED_QUANTITY);
        updateBatchDTO.setAvailableQuantity(UPDATED_QUANTITY);
        updateBatchDTO.setStatus("INACTIVE");

        // Update the existing testBatch with new values for verification
        testBatch.setBatchCode("BATCH-001-UPDATED");
        testBatch.setQuantity(UPDATED_QUANTITY);
        testBatch.setAvailableQuantity(UPDATED_QUANTITY);
        testBatch.setStatus("INACTIVE");

        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(Batch.class))).thenReturn(testBatch);
        // Mock the modelMapper.map(batchDTO, existingBatch) call
        doNothing().when(modelMapper).map(any(BatchDTO.class), any(Batch.class));
        when(modelMapper.map(testBatch, BatchDTO.class)).thenReturn(updateBatchDTO);

        final BatchDTO result = batchService.updateBatch(TEST_BATCH_ID, updateBatchDTO);

        assertNotNull(result);
        assertEquals(TEST_BATCH_ID, result.getId());
        assertEquals("BATCH-001-UPDATED", result.getBatchCode());
        assertEquals(UPDATED_QUANTITY, result.getQuantity());
        assertEquals(UPDATED_QUANTITY, result.getAvailableQuantity());
        assertEquals("INACTIVE", result.getStatus());

        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
        verify(batchRepository, times(1)).save(any(Batch.class));
        verify(modelMapper, times(1)).map(any(BatchDTO.class), any(Batch.class));
        verify(modelMapper, times(1)).map(testBatch, BatchDTO.class);
    }

    @Test
    void testUpdateBatchNotFound() {
        when(batchRepository.findById(NON_EXISTENT_BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.updateBatch(NON_EXISTENT_BATCH_ID, updatedBatchDTO);
        });

        verify(batchRepository, times(1)).findById(NON_EXISTENT_BATCH_ID);
        verify(batchRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateBatchBatchCodeChanged() {
        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));
        when(batchRepository.existsByBatchCode("BATCH-001-UPDATED")).thenReturn(true);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchService.updateBatch(TEST_BATCH_ID, updatedBatchDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
        verify(batchRepository, times(1)).existsByBatchCode("BATCH-001-UPDATED");
        verify(batchRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testDeleteBatch() {
        when(batchRepository.existsById(TEST_BATCH_ID)).thenReturn(true);
        doNothing().when(batchRepository).deleteById(TEST_BATCH_ID);

        batchService.deleteBatch(TEST_BATCH_ID);

        verify(batchRepository, times(1)).existsById(TEST_BATCH_ID);
        verify(batchRepository, times(1)).deleteById(TEST_BATCH_ID);
    }

    @Test
    void testDeleteBatchNotFound() {
        when(batchRepository.existsById(NON_EXISTENT_BATCH_ID)).thenReturn(false);

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.deleteBatch(NON_EXISTENT_BATCH_ID);
        });

        verify(batchRepository, times(1)).existsById(NON_EXISTENT_BATCH_ID);
        verify(batchRepository, never()).deleteById(any());
    }

    @Test
    void testActivateBatch() {
        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(Batch.class))).thenAnswer(batch -> {
            final Batch savedBatch = batch.getArgument(0);
            savedBatch.setStatus("ACTIVE");
            return savedBatch;
        });

        batchService.activateBatch(TEST_BATCH_ID);

        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
        verify(batchRepository, times(1)).save(any(Batch.class));
    }

    @Test
    void testActivateBatchNotFound() {
        when(batchRepository.findById(NON_EXISTENT_BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.activateBatch(NON_EXISTENT_BATCH_ID);
        });

        verify(batchRepository, times(1)).findById(NON_EXISTENT_BATCH_ID);
        verify(batchRepository, never()).save(any());
    }

    @Test
    void testDeactivateBatch() {
        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(Batch.class))).thenAnswer(batch -> {
            final Batch savedBatch = batch.getArgument(0);
            savedBatch.setStatus("INACTIVE");
            return savedBatch;
        });

        batchService.deactivateBatch(TEST_BATCH_ID);

        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
        verify(batchRepository, times(1)).save(any(Batch.class));
    }

    @Test
    void testDeactivateBatchNotFound() {
        when(batchRepository.findById(NON_EXISTENT_BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.deactivateBatch(NON_EXISTENT_BATCH_ID);
        });

        verify(batchRepository, times(1)).findById(NON_EXISTENT_BATCH_ID);
        verify(batchRepository, never()).save(any());
    }

    @Test
    void testGetAvailableQuantity() {
        when(batchRepository.findById(TEST_BATCH_ID)).thenReturn(Optional.of(testBatch));

        final Integer result = batchService.getAvailableQuantity(TEST_BATCH_ID);

        assertNotNull(result);
        assertEquals(TEST_QUANTITY, result);

        verify(batchRepository, times(1)).findById(TEST_BATCH_ID);
    }

    @Test
    void testGetAvailableQuantityNotFound() {
        when(batchRepository.findById(NON_EXISTENT_BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> {
            batchService.getAvailableQuantity(NON_EXISTENT_BATCH_ID);
        });

        verify(batchRepository, times(1)).findById(NON_EXISTENT_BATCH_ID);
    }

}
