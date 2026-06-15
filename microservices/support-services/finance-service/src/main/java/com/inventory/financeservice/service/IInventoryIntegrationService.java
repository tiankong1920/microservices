package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.InventorySyncDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IInventoryIntegrationService {

    void syncSalesData(LocalDateTime startDate, LocalDateTime endDate);

    void syncPurchaseData(LocalDateTime startDate, LocalDateTime endDate);

    void syncInventoryAdjustment(LocalDateTime startDate, LocalDateTime endDate);

    void createAccountsReceivableFromSales(String salesOrderId);

    void createAccountsPayableFromPurchase(String purchaseOrderId);

    List<InventorySyncDTO> getSyncHistory(String syncType, LocalDateTime startDate, LocalDateTime endDate);

    SyncResult synchronizeAll(LocalDateTime startDate, LocalDateTime endDate);

    void processInventoryAlert(String alertType, String productId, BigDecimal quantity, BigDecimal threshold);

    class SyncResult {
        private int salesRecordsSynced;
        private int purchaseRecordsSynced;
        private int adjustmentsSynced;
        private int receivablesCreated;
        private int payablesCreated;
        private List<String> errors;
        private boolean success;

        public int getSalesRecordsSynced() { return salesRecordsSynced; }
        public void setSalesRecordsSynced(int salesRecordsSynced) { this.salesRecordsSynced = salesRecordsSynced; }
        public int getPurchaseRecordsSynced() { return purchaseRecordsSynced; }
        public void setPurchaseRecordsSynced(int purchaseRecordsSynced) { this.purchaseRecordsSynced = purchaseRecordsSynced; }
        public int getAdjustmentsSynced() { return adjustmentsSynced; }
        public void setAdjustmentsSynced(int adjustmentsSynced) { this.adjustmentsSynced = adjustmentsSynced; }
        public int getReceivablesCreated() { return receivablesCreated; }
        public void setReceivablesCreated(int receivablesCreated) { this.receivablesCreated = receivablesCreated; }
        public int getPayablesCreated() { return payablesCreated; }
        public void setPayablesCreated(int payablesCreated) { this.payablesCreated = payablesCreated; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
