package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.InventoryDTO;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.exception.InventoryNotFoundException;
import com.inventory.inventoryservice.repository.IInventoryRepository;
import com.inventory.inventoryservice.service.IInventoryService;
import com.inventory.common.core.kafka.KafkaMessageService;
import com.inventory.common.core.kafka.event.InventoryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class InventoryServiceImpl implements IInventoryService {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final IInventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final KafkaMessageService kafkaMessageService;
    private final ObjectMapper objectMapper;

    private static final String ERROR_INVENTORY_NOT_FOUND = "Inventory not found with id: ";
    private static final String ERROR_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE = "Inventory not found for product: {} and warehouse: {}";
    private static final String MSG_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE = "Inventory not found for product: ";
    private static final String MSG_AND_WAREHOUSE = " and warehouse: ";

    @Override
    @Cacheable(value = "inventory")
    public List<InventoryDTO> getAllInventory() {
        LOG.info("Getting all inventory items");
        final List<Inventory> inventoryList = inventoryRepository.findAll();
        return inventoryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "inventory", key = "#id")
    public InventoryDTO getInventoryById(Long id) {
        LOG.info("Getting inventory by id: {}", id);
        final Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND + id);
                    return new InventoryNotFoundException(ERROR_INVENTORY_NOT_FOUND + id);
                });
        return convertToDTO(inventory);
    }

    @Override
    @Cacheable(value = "inventory", key = "#status")
    public List<InventoryDTO> getInventoryByStatus(String status) {
        LOG.info("Getting inventory by status: {}", status);
        final List<Inventory> inventoryList = inventoryRepository.findByStatus(status);
        return inventoryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "inventory", key = "#productId")
    public List<InventoryDTO> getInventoryByProductId(Long productId) {
        LOG.info("Getting inventory by product id: {}", productId);
        final List<Inventory> inventoryList = inventoryRepository.findByProductId(productId);
        return inventoryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "inventory", key = "#warehouseId")
    public List<InventoryDTO> getInventoryByWarehouseId(Long warehouseId) {
        LOG.info("Getting inventory by warehouse id: {}", warehouseId);
        final List<Inventory> inventoryList = inventoryRepository.findByWarehouseId(warehouseId);
        return inventoryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "inventory", key = "#batchId")
    public List<InventoryDTO> getInventoryByBatchId(Long batchId) {
        LOG.info("Getting inventory by batch id: {}", batchId);
        final List<Inventory> inventoryList = inventoryRepository.findByBatchId(batchId);
        return inventoryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "inventory", key = "#productId + '_' + #warehouseId")
    public InventoryDTO getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        LOG.info("Getting inventory by product id: {} and warehouse id: {}", productId, warehouseId);
        final Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE, productId, warehouseId);
                    return new InventoryNotFoundException(
                            MSG_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE + productId + MSG_AND_WAREHOUSE + warehouseId);
                });
        return convertToDTO(inventory);
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    @Transactional
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        LOG.info("Creating inventory for product: {} in warehouse: {}",
                inventoryDTO.getProductId(), inventoryDTO.getWarehouseId());

        final Inventory inventory = modelMapper.map(inventoryDTO, Inventory.class);

        if (inventory.getAvailableQuantity() == null) {
            inventory.setAvailableQuantity(inventory.getQuantity());
        }

        if (inventory.getReservedQuantity() == null) {
            inventory.setReservedQuantity(0);
        }

        final Inventory savedInventory = inventoryRepository.save(inventory);
        LOG.info("Inventory created successfully with id: {}", savedInventory.getId());
        return convertToDTO(savedInventory);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#id")
    @Transactional
    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) {
        LOG.info("Updating inventory with id: {}", id);
        final Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND + id);
                    return new InventoryNotFoundException(ERROR_INVENTORY_NOT_FOUND + id);
                });

        modelMapper.map(inventoryDTO, existingInventory);

        final Inventory updatedInventory = inventoryRepository.save(existingInventory);
        LOG.info("Inventory updated successfully with id: {}", updatedInventory.getId());
        return convertToDTO(updatedInventory);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#id")
    @Transactional
    public void deleteInventory(Long id) {
        LOG.info("Deleting inventory with id: {}", id);
        final Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND + id);
                    return new InventoryNotFoundException(ERROR_INVENTORY_NOT_FOUND + id);
                });
        inventoryRepository.delete(existingInventory);
        LOG.info("Inventory deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId + '_' + #warehouseId")
    @Transactional
    public Integer reserveInventory(Long productId, Long warehouseId, Integer quantity) {
        LOG.info("Reserving {} units of product: {} in warehouse: {}",
                quantity, productId, warehouseId);

        final Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE, productId, warehouseId);
                    return new InventoryNotFoundException(
                            MSG_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE + productId + MSG_AND_WAREHOUSE + warehouseId);
                });

        if (inventory.getAvailableQuantity() < quantity) {
            LOG.error("Insufficient inventory. Available: {}, Requested: {}",
                    inventory.getAvailableQuantity(), quantity);
            throw new IllegalStateException("Insufficient inventory. Available: "
                    + inventory.getAvailableQuantity() + ", Requested: " + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        final Inventory updatedInventory = inventoryRepository.save(inventory);

        LOG.info("Inventory reserved successfully. New available: {}, New reserved: {}",
                updatedInventory.getAvailableQuantity(), updatedInventory.getReservedQuantity());
        return updatedInventory.getAvailableQuantity();
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId + '_' + #warehouseId")
    @Transactional
    public Integer releaseInventory(Long productId, Long warehouseId, Integer quantity) {
        LOG.info("Releasing {} units of product: {} in warehouse: {}",
                quantity, productId, warehouseId);

        final Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE, productId, warehouseId);
                    return new InventoryNotFoundException(
                            MSG_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE + productId + MSG_AND_WAREHOUSE + warehouseId);
                });

        if (inventory.getReservedQuantity() < quantity) {
            LOG.error("Cannot release more than reserved. Reserved: {}, Requested: {}",
                    inventory.getReservedQuantity(), quantity);
            throw new IllegalStateException("Cannot release more than reserved. Reserved: "
                    + inventory.getReservedQuantity() + ", Requested: " + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        final Inventory updatedInventory = inventoryRepository.save(inventory);

        LOG.info("Inventory released successfully. New available: {}, New reserved: {}",
                updatedInventory.getAvailableQuantity(), updatedInventory.getReservedQuantity());
        return updatedInventory.getAvailableQuantity();
    }

    @Override
    @CacheEvict(value = "inventory", key = "#productId + '_' + #warehouseId")
    @Transactional
    public Integer adjustInventory(Long productId, Long warehouseId, Integer quantity) {
        LOG.info("Adjusting inventory by {} units for product: {} in warehouse: {}",
                quantity, productId, warehouseId);

        final Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> {
                    LOG.error(ERROR_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE, productId, warehouseId);
                    return new InventoryNotFoundException(
                            MSG_INVENTORY_NOT_FOUND_PRODUCT_WAREHOUSE + productId + MSG_AND_WAREHOUSE + warehouseId);
                });

        final int newQuantity = inventory.getQuantity() + quantity;
        if (newQuantity < 0) {
            LOG.error("Inventory cannot be negative. Current: {}, Adjustment: {}",
                    inventory.getQuantity(), quantity);
            throw new IllegalStateException("Inventory cannot be negative. Current: "
                    + inventory.getQuantity() + ", Adjustment: " + quantity);
        }

        final Integer previousQuantity = inventory.getQuantity();
        inventory.setQuantity(newQuantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        final Inventory updatedInventory = inventoryRepository.save(inventory);

        sendInventoryEvent(
                InventoryEvent.InventoryEventType.ADJUSTMENT,
                inventory.getProductId(),
                quantity,
                previousQuantity,
                updatedInventory.getQuantity()
        );

        LOG.info("Inventory adjusted successfully. New quantity: {}, New available: {}",
                updatedInventory.getQuantity(), updatedInventory.getAvailableQuantity());
        return updatedInventory.getAvailableQuantity();
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        return modelMapper.map(inventory, InventoryDTO.class);
    }

    @SuppressWarnings("unused")
    private Inventory convertToEntity(InventoryDTO inventoryDTO) {
        return modelMapper.map(inventoryDTO, Inventory.class);
    }

    private void sendInventoryEvent(InventoryEvent.InventoryEventType eventType, Long productId,
                                   Integer quantity, Integer previousQuantity, Integer currentQuantity) {
        try {
            final InventoryEvent inventoryEvent = new InventoryEvent();
            inventoryEvent.setEventId(UUID.randomUUID().toString());
            inventoryEvent.setEventType(eventType);
            inventoryEvent.setProductId(productId);
            inventoryEvent.setQuantity(quantity);
            inventoryEvent.setPreviousQuantity(previousQuantity);
            inventoryEvent.setCurrentQuantity(currentQuantity);
            inventoryEvent.setEventTime(LocalDateTime.now());
            inventoryEvent.setOperator("system");
            inventoryEvent.setRemark("Inventory adjustment via service");

            final String eventJson = objectMapper.writeValueAsString(inventoryEvent);
            kafkaMessageService.sendMessage("inventory-events", eventJson);
        } catch (com.fasterxml.jackson.core.JacksonException e) {
            LOG.error("Failed to serialize inventory event", e);
        }
    }
}
