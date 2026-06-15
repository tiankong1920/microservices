package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.BatchDTO;
import com.inventory.inventoryservice.entity.Batch;
import com.inventory.inventoryservice.exception.BatchNotFoundException;
import com.inventory.inventoryservice.repository.IBatchRepository;
import com.inventory.inventoryservice.service.IBatchService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for batch management.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BatchServiceImpl implements IBatchService {

    private final IBatchRepository batchRepository;
    private final ModelMapper modelMapper;

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(BatchServiceImpl.class);

    private static final String ERROR_BATCH_NOT_FOUND = "Batch not found with id: ";
    private static final String ERROR_BATCH_NOT_FOUND_CODE = "Batch not found with code: ";
    private static final String ERROR_BATCH_CODE_EXISTS = "Batch with code ";

    @Override
    @Cacheable(value = "batches")
    public List<BatchDTO> getAllBatches() {
        LOG.info("Getting all batches");
        final List<Batch> batches = batchRepository.findAll();
        LOG.info("Found {} batches", batches.size());
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "#id")
    public BatchDTO getBatchById(Long id) {
        LOG.info("Getting batch by id: {}", id);
        final Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with id: {}", id);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + id);
                });
        return modelMapper.map(batch, BatchDTO.class);
    }

    @Override
    @Cacheable(value = "batches", key = "#batchCode")
    public BatchDTO getBatchByCode(String batchCode) {
        LOG.info("Getting batch by code: {}", batchCode);
        final Batch batch = batchRepository.findByBatchCode(batchCode)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with code: {}", batchCode);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND_CODE + batchCode);
                });
        return modelMapper.map(batch, BatchDTO.class);
    }

    @Override
    @Cacheable(value = "batches", key = "'product-' + #productId")
    public List<BatchDTO> getBatchesByProductId(Long productId) {
        LOG.info("Getting batches for product: {}", productId);
        final List<Batch> batches = batchRepository.findByProductId(productId);
        LOG.info("Found {} batches for product: {}", batches.size(), productId);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "'warehouse-' + #warehouseId")
    public List<BatchDTO> getBatchesByWarehouseId(Long warehouseId) {
        LOG.info("Getting batches for warehouse: {}", warehouseId);
        final List<Batch> batches = batchRepository.findByWarehouseId(warehouseId);
        LOG.info("Found {} batches for warehouse: {}", batches.size(), warehouseId);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "'supplier-' + #supplierId")
    public List<BatchDTO> getBatchesBySupplierId(Long supplierId) {
        LOG.info("Getting batches for supplier: {}", supplierId);
        final List<Batch> batches = batchRepository.findBySupplierId(supplierId);
        LOG.info("Found {} batches for supplier: {}", batches.size(), supplierId);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "'status-' + #status")
    public List<BatchDTO> getBatchesByStatus(String status) {
        LOG.info("Getting batches with status: {}", status);
        final List<Batch> batches = batchRepository.findByStatus(status);
        LOG.info("Found {} batches with status: {}", batches.size(), status);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "'expiring-' + #date")
    public List<BatchDTO> getBatchesExpiringBefore(LocalDate date) {
        LOG.info("Getting batches expiring before: {}", date);
        final List<Batch> batches = batchRepository.findByExpiryDateBefore(date);
        LOG.info("Found {} batches expiring before: {}", batches.size(), date);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "batches", key = "'product-' + #productId + '-warehouse-' + #warehouseId")
    public List<BatchDTO> getBatchesByProductAndWarehouse(Long productId, Long warehouseId) {
        LOG.info("Getting batches for product: {} and warehouse: {}", productId, warehouseId);
        final List<Batch> batches = batchRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        LOG.info("Found {} batches for product: {} and warehouse: {}", batches.size(), productId, warehouseId);
        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    @Override
    @CacheEvict(value = "batches", allEntries = true)
    @Transactional
    public BatchDTO createBatch(BatchDTO batchDTO) {
        LOG.info("Creating batch with code: {}", batchDTO.getBatchCode());

        if (batchRepository.existsByBatchCode(batchDTO.getBatchCode())) {
            LOG.error("Batch with code {} already exists", batchDTO.getBatchCode());
            throw new IllegalStateException(
                    ERROR_BATCH_CODE_EXISTS + batchDTO.getBatchCode() + " already exists");
        }

        final Batch batch = modelMapper.map(batchDTO, Batch.class);
        final Batch savedBatch = batchRepository.save(batch);

        LOG.info("Batch created successfully with id: {}", savedBatch.getId());
        return modelMapper.map(savedBatch, BatchDTO.class);
    }

    @Override
    @CacheEvict(value = "batches", allEntries = true)
    @Transactional
    public BatchDTO updateBatch(Long id, BatchDTO batchDTO) {
        LOG.info("Updating batch with id: {}", id);

        final Batch existingBatch = batchRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with id: {}", id);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + id);
                });

        if (!existingBatch.getBatchCode().equals(batchDTO.getBatchCode())
                && batchRepository.existsByBatchCode(batchDTO.getBatchCode())) {
                LOG.error("Batch with code {} already exists", batchDTO.getBatchCode());
                throw new IllegalStateException(
                        ERROR_BATCH_CODE_EXISTS + batchDTO.getBatchCode() + " already exists");
            }

        modelMapper.map(batchDTO, existingBatch);
        final Batch updatedBatch = batchRepository.save(existingBatch);

        LOG.info("Batch updated successfully with id: {}", updatedBatch.getId());
        return modelMapper.map(updatedBatch, BatchDTO.class);
    }

    @Override
    @CacheEvict(value = "batches", allEntries = true)
    @Transactional
    public void deleteBatch(Long id) {
        LOG.info("Deleting batch with id: {}", id);

        if (!batchRepository.existsById(id)) {
            LOG.error("Batch not found with id: {}", id);
            throw new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + id);
        }

        batchRepository.deleteById(id);
        LOG.info("Batch deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "batches", allEntries = true)
    @Transactional
    public void activateBatch(Long id) {
        LOG.info("Activating batch with id: {}", id);

        final Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with id: {}", id);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + id);
                });

        batch.setStatus("ACTIVE");
        final Batch updatedBatch = batchRepository.save(batch);

        LOG.info("Batch activated successfully with id: {}", updatedBatch.getId());
    }

    @Override
    @CacheEvict(value = "batches", allEntries = true)
    @Transactional
    public void deactivateBatch(Long id) {
        LOG.info("Deactivating batch with id: {}", id);

        final Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with id: {}", id);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + id);
                });

        batch.setStatus("INACTIVE");
        final Batch updatedBatch = batchRepository.save(batch);

        LOG.info("Batch deactivated successfully with id: {}", updatedBatch.getId());
    }

    @Override
    @Cacheable(value = "batches", key = "'available-' + #batchId")
    public Integer getAvailableQuantity(Long batchId) {
        LOG.info("Getting available quantity for batch: {}", batchId);
        final Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOG.error("Batch not found with id: {}", batchId);
                    return new BatchNotFoundException(ERROR_BATCH_NOT_FOUND + batchId);
                });
        return batch.getAvailableQuantity();
    }
}
