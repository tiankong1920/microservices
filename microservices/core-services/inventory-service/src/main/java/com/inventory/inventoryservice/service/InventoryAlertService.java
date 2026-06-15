package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.InventoryAlertDTO;
import com.inventory.inventoryservice.dto.InventoryAlertThresholdDTO;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.entity.InventoryAlertLog;
import com.inventory.inventoryservice.entity.InventoryAlertLog.AlertType;
import com.inventory.inventoryservice.entity.InventoryAlertLog.EmailStatus;
import com.inventory.inventoryservice.entity.InventoryAlertThreshold;
import com.inventory.inventoryservice.exception.InventoryNotFoundException;
import com.inventory.inventoryservice.repository.IInventoryAlertLogRepository;
import com.inventory.inventoryservice.repository.IInventoryAlertThresholdRepository;
import com.inventory.inventoryservice.repository.IInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAlertService {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryAlertThresholdRepository thresholdRepository;
    private final IInventoryAlertLogRepository alertLogRepository;
    private final InventoryAlertEmailService emailService;

    @Transactional
    public InventoryAlertDTO checkAndAlert(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

        return checkAndAlert(inventory);
    }

    @Transactional
    public List<InventoryAlertDTO> checkAndAlertAll() {
        List<Inventory> allInventory = inventoryRepository.findAll();
        List<InventoryAlertDTO> alerts = new ArrayList<>();

        for (Inventory inventory : allInventory) {
            try {
                Optional<InventoryAlertDTO> alert = Optional.ofNullable(checkAndAlert(inventory));
                alert.ifPresent(alerts::add);
            } catch (Exception e) {
                log.warn("Failed to check inventory {}: {}", inventory.getId(), e.getMessage());
            }
        }

        return alerts;
    }

    private InventoryAlertDTO checkAndAlert(Inventory inventory) {
        Optional<InventoryAlertThreshold> thresholdOpt = thresholdRepository
                .findByProductIdAndWarehouseId(inventory.getProductId(), inventory.getWarehouseId());

        if (thresholdOpt.isEmpty() || !thresholdOpt.get().isEnabled()) {
            return null;
        }

        InventoryAlertThreshold threshold = thresholdOpt.get();
        Integer quantity = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : inventory.getQuantity();

        String alertType = null;
        Integer thresholdValue = null;

        if (quantity <= 0) {
            alertType = AlertType.OUT_OF_STOCK.name();
            thresholdValue = 0;
        } else if (quantity <= threshold.getCriticalStockThreshold()) {
            alertType = AlertType.CRITICAL_STOCK.name();
            thresholdValue = threshold.getCriticalStockThreshold();
        } else if (quantity <= threshold.getLowStockThreshold()) {
            alertType = AlertType.LOW_STOCK.name();
            thresholdValue = threshold.getLowStockThreshold();
        } else if (quantity <= threshold.getReorderPoint()) {
            alertType = AlertType.REORDER_POINT.name();
            thresholdValue = threshold.getReorderPoint();
        }

        if (alertType == null) {
            return null;
        }

        InventoryAlertLog alertLog = createAlertLog(inventory, threshold, alertType, thresholdValue);

        if (threshold.getAlertEmail() != null && !threshold.getAlertEmail().isEmpty()) {
            sendAlertEmail(inventory, threshold, alertType);
            alertLog.setEmailStatus(EmailStatus.SENT.name());
            alertLog.setEmailSentTo(threshold.getAlertEmail());
        } else {
            alertLog.setEmailStatus(EmailStatus.PENDING.name());
        }

        alertLogRepository.save(alertLog);

        return InventoryAlertDTO.builder()
                .id(alertLog.getId())
                .productId(inventory.getProductId())
                .warehouseId(inventory.getWarehouseId())
                .alertType(alertType)
                .currentQuantity(quantity)
                .thresholdValue(thresholdValue)
                .alertEmail(threshold.getAlertEmail())
                .status(alertLog.getEmailStatus())
                .createdAt(alertLog.getSentAt())
                .build();
    }

    private InventoryAlertLog createAlertLog(Inventory inventory, InventoryAlertThreshold threshold,
                                              String alertType, Integer thresholdValue) {
        return InventoryAlertLog.builder()
                .productId(inventory.getProductId())
                .warehouseId(inventory.getWarehouseId())
                .alertType(alertType)
                .currentQuantity(inventory.getAvailableQuantity() != null ?
                        inventory.getAvailableQuantity() : inventory.getQuantity())
                .thresholdValue(thresholdValue)
                .sentAt(LocalDateTime.now())
                .acknowledged(false)
                .build();
    }

    private void sendAlertEmail(Inventory inventory, InventoryAlertThreshold threshold, String alertType) {
        String email = threshold.getAlertEmail();
        Long productId = inventory.getProductId();
        Long warehouseId = inventory.getWarehouseId();
        Integer quantity = inventory.getAvailableQuantity() != null ?
                inventory.getAvailableQuantity() : inventory.getQuantity();

        String productName = "Product-" + productId;

        switch (AlertType.valueOf(alertType)) {
            case OUT_OF_STOCK:
                emailService.sendOutOfStockAlert(email, productName, productId, warehouseId);
                break;
            case CRITICAL_STOCK:
                emailService.sendCriticalStockAlert(email, productName, productId, warehouseId,
                        quantity, threshold.getCriticalStockThreshold());
                break;
            case LOW_STOCK:
                emailService.sendLowStockAlert(email, productName, productId, warehouseId,
                        quantity, threshold.getLowStockThreshold());
                break;
            case REORDER_POINT:
                emailService.sendReorderAlert(email, productName, productId, warehouseId,
                        quantity, threshold.getReorderPoint());
                break;
        }
    }

    @Transactional
    public InventoryAlertThresholdDTO createOrUpdateThreshold(InventoryAlertThresholdDTO dto) {
        Optional<InventoryAlertThreshold> existing = thresholdRepository
                .findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId());

        InventoryAlertThreshold threshold;
        if (existing.isPresent()) {
            threshold = existing.get();
            threshold.setLowStockThreshold(dto.getLowStockThreshold());
            threshold.setCriticalStockThreshold(dto.getCriticalStockThreshold());
            threshold.setReorderPoint(dto.getReorderPoint());
            threshold.setAlertEmail(dto.getAlertEmail());
            threshold.setEnabled(dto.isEnabled());
        } else {
            threshold = InventoryAlertThreshold.builder()
                    .productId(dto.getProductId())
                    .warehouseId(dto.getWarehouseId())
                    .lowStockThreshold(dto.getLowStockThreshold())
                    .criticalStockThreshold(dto.getCriticalStockThreshold())
                    .reorderPoint(dto.getReorderPoint())
                    .alertEmail(dto.getAlertEmail())
                    .enabled(dto.isEnabled())
                    .build();
        }

        threshold = thresholdRepository.save(threshold);

        return mapToDTO(threshold);
    }

    public List<InventoryAlertThresholdDTO> getAllThresholds() {
        return thresholdRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<InventoryAlertLog> getUnacknowledgedAlerts() {
        return alertLogRepository.findByAcknowledged(false);
    }

    @Transactional
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        alertLogRepository.findById(alertId).ifPresent(alert -> {
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(LocalDateTime.now());
            alert.setAcknowledgedBy(acknowledgedBy);
            alertLogRepository.save(alert);
        });
    }

    private InventoryAlertThresholdDTO mapToDTO(InventoryAlertThreshold threshold) {
        return InventoryAlertThresholdDTO.builder()
                .id(threshold.getId())
                .productId(threshold.getProductId())
                .warehouseId(threshold.getWarehouseId())
                .lowStockThreshold(threshold.getLowStockThreshold())
                .criticalStockThreshold(threshold.getCriticalStockThreshold())
                .reorderPoint(threshold.getReorderPoint())
                .alertEmail(threshold.getAlertEmail())
                .enabled(threshold.isEnabled())
                .build();
    }
}
