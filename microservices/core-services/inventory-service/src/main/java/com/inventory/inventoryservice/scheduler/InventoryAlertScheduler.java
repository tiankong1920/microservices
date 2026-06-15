package com.inventory.inventoryservice.scheduler;

import com.inventory.inventoryservice.entity.InventoryAlertThreshold;
import com.inventory.inventoryservice.entity.InventoryAlertLog.AlertType;
import com.inventory.inventoryservice.repository.IInventoryAlertThresholdRepository;
import com.inventory.inventoryservice.repository.IInventoryRepository;
import com.inventory.inventoryservice.service.InventoryAlertEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertScheduler {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryAlertThresholdRepository thresholdRepository;
    private final InventoryAlertEmailService emailService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyLowStockCheck() {
        log.info("Starting daily low stock check...");
        performStockCheck();
        log.info("Daily low stock check completed");
    }

    @Scheduled(cron = "0 0 */4 * * ?")
    public void fourHourlyCriticalStockCheck() {
        log.info("Starting 4-hourly critical stock check...");
        performCriticalStockCheck();
        log.info("4-hourly critical stock check completed");
    }

    private void performStockCheck() {
        List<InventoryAlertThreshold> thresholds = thresholdRepository.findByEnabled(true);

        int lowStockCount = 0;
        int criticalCount = 0;
        int outOfStockCount = 0;
        int reorderCount = 0;

        for (InventoryAlertThreshold threshold : thresholds) {
            int[] counts = checkThresholdInventory(threshold);
            lowStockCount += counts[0];
            criticalCount += counts[1];
            outOfStockCount += counts[2];
            reorderCount += counts[3];
        }

        sendBatchAlertSummaries(thresholds, lowStockCount, criticalCount, outOfStockCount, reorderCount);
    }

    private int[] checkThresholdInventory(InventoryAlertThreshold threshold) {
        int[] counts = new int[4];
        var inventoryOpt = inventoryRepository.findByProductIdAndWarehouseId(
                threshold.getProductId(), threshold.getWarehouseId());

        if (inventoryOpt.isEmpty()) {
            return counts;
        }

        var inventory = inventoryOpt.get();
        int quantity = inventory.getAvailableQuantity() != null ?
                inventory.getAvailableQuantity() : inventory.getQuantity();

        if (quantity <= 0) {
            counts[2] = 1;
            sendAlert(inventory.getProductId(), threshold, AlertType.OUT_OF_STOCK, quantity, 0);
        } else if (quantity <= threshold.getCriticalStockThreshold()) {
            counts[1] = 1;
            sendAlert(inventory.getProductId(), threshold, AlertType.CRITICAL_STOCK,
                    quantity, threshold.getCriticalStockThreshold());
        } else if (quantity <= threshold.getLowStockThreshold()) {
            counts[0] = 1;
            sendAlert(inventory.getProductId(), threshold, AlertType.LOW_STOCK,
                    quantity, threshold.getLowStockThreshold());
        } else if (quantity <= threshold.getReorderPoint()) {
            counts[3] = 1;
            sendAlert(inventory.getProductId(), threshold, AlertType.REORDER_POINT,
                    quantity, threshold.getReorderPoint());
        }
        return counts;
    }

    private void sendBatchAlertSummaries(List<InventoryAlertThreshold> thresholds,
            int lowStockCount, int criticalCount, int outOfStockCount, int reorderCount) {
        for (InventoryAlertThreshold threshold : thresholds) {
            if (threshold.getAlertEmail() != null && !threshold.getAlertEmail().isEmpty()) {
                if (lowStockCount > 0 || criticalCount > 0 || outOfStockCount > 0 || reorderCount > 0) {
                    emailService.sendBatchAlertSummary(
                            threshold.getAlertEmail(),
                            lowStockCount,
                            criticalCount,
                            outOfStockCount,
                            reorderCount
                    );
                }
            }
        }
    }

    private void performCriticalStockCheck() {
        List<InventoryAlertThreshold> thresholds = thresholdRepository.findByEnabled(true);

        for (InventoryAlertThreshold threshold : thresholds) {
            var inventoryOpt = inventoryRepository.findByProductIdAndWarehouseId(
                    threshold.getProductId(), threshold.getWarehouseId());

            if (inventoryOpt.isEmpty()) {
                continue;
            }

            var inventory = inventoryOpt.get();
            int quantity = inventory.getAvailableQuantity() != null ?
                    inventory.getAvailableQuantity() : inventory.getQuantity();

            if (quantity <= threshold.getCriticalStockThreshold()) {
                if (quantity <= 0) {
                    sendAlert(inventory.getProductId(), threshold, AlertType.OUT_OF_STOCK, quantity, 0);
                } else {
                    sendAlert(inventory.getProductId(), threshold, AlertType.CRITICAL_STOCK,
                            quantity, threshold.getCriticalStockThreshold());
                }
            }
        }
    }

    private void sendAlert(Long productId, InventoryAlertThreshold threshold,
                          AlertType alertType, int currentQty, int thresholdValue) {
        if (threshold.getAlertEmail() == null || threshold.getAlertEmail().isEmpty()) {
            return;
        }

        String productName = "Product-" + productId;

        switch (alertType) {
            case OUT_OF_STOCK:
                emailService.sendOutOfStockAlert(threshold.getAlertEmail(), productName,
                        productId, threshold.getWarehouseId());
                break;
            case CRITICAL_STOCK:
                emailService.sendCriticalStockAlert(threshold.getAlertEmail(), productName,
                        productId, threshold.getWarehouseId(), currentQty, thresholdValue);
                break;
            case LOW_STOCK:
                emailService.sendLowStockAlert(threshold.getAlertEmail(), productName,
                        productId, threshold.getWarehouseId(), currentQty, thresholdValue);
                break;
            case REORDER_POINT:
                emailService.sendReorderAlert(threshold.getAlertEmail(), productName,
                        productId, threshold.getWarehouseId(), currentQty, thresholdValue);
                break;
        }

        log.info("Alert sent for product {}: {} (qty: {}, threshold: {})",
                productId, alertType, currentQty, thresholdValue);
    }
}
