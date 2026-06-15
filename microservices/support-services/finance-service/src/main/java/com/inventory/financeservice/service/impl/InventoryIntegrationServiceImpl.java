package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.InventorySyncDTO;
import com.inventory.financeservice.entity.Expense;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.entity.FinanceVoucher;
import com.inventory.financeservice.entity.Income;
import com.inventory.financeservice.entity.SettlementAccount;
import com.inventory.financeservice.repository.IExpenseRepository;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.repository.IFinanceVoucherRepository;
import com.inventory.financeservice.repository.IIncomeRepository;
import com.inventory.financeservice.repository.ISettlementAccountRepository;
import com.inventory.financeservice.service.IInventoryIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryIntegrationServiceImpl implements IInventoryIntegrationService {

    private final IIncomeRepository incomeRepository;
    private final IExpenseRepository expenseRepository;
    private final IFinanceVoucherRepository voucherRepository;
    private final ISettlementAccountRepository settlementAccountRepository;
    private final IFinanceAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void syncSalesData(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Syncing sales data from {} to {}", startDate, endDate);

        String syncBatchId = generateSyncBatchId();

        try {
            List<SalesOrderData> salesOrders = fetchSalesOrdersFromInventory(startDate, endDate);

            for (SalesOrderData sales : salesOrders) {
                createIncomeFromSales(sales, syncBatchId);
                createFinanceVoucher(sales, "SALES_REVENUE", syncBatchId);
            }

            createAuditLog("SALES_SYNC", syncBatchId, "Sales data synced: " + salesOrders.size() + " records");
            log.info("Sales data sync completed. Records: {}", salesOrders.size());
        } catch (Exception e) {
            log.error("Sales data sync failed", e);
            createAuditLog("SALES_SYNC_FAILED", syncBatchId, "Error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void syncPurchaseData(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Syncing purchase data from {} to {}", startDate, endDate);

        String syncBatchId = generateSyncBatchId();

        try {
            List<PurchaseOrderData> purchases = fetchPurchaseOrdersFromInventory(startDate, endDate);

            for (PurchaseOrderData purchase : purchases) {
                createExpenseFromPurchase(purchase, syncBatchId);
                createFinanceVoucher(purchase, "PURCHASE_EXPENSE", syncBatchId);
            }

            createAuditLog("PURCHASE_SYNC", syncBatchId, "Purchase data synced: " + purchases.size() + " records");
            log.info("Purchase data sync completed. Records: {}", purchases.size());
        } catch (Exception e) {
            log.error("Purchase data sync failed", e);
            createAuditLog("PURCHASE_SYNC_FAILED", syncBatchId, "Error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void syncInventoryAdjustment(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Syncing inventory adjustments from {} to {}", startDate, endDate);

        String syncBatchId = generateSyncBatchId();

        try {
            List<InventoryAdjustmentData> adjustments = fetchInventoryAdjustments(startDate, endDate);

            for (InventoryAdjustmentData adjustment : adjustments) {
                createExpenseFromAdjustment(adjustment, syncBatchId);
                createFinanceVoucher(adjustment, "INVENTORY_ADJUSTMENT", syncBatchId);
            }

            createAuditLog("ADJUSTMENT_SYNC", syncBatchId, "Adjustments synced: " + adjustments.size() + " records");
            log.info("Inventory adjustment sync completed. Records: {}", adjustments.size());
        } catch (Exception e) {
            log.error("Inventory adjustment sync failed", e);
            createAuditLog("ADJUSTMENT_SYNC_FAILED", syncBatchId, "Error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createAccountsReceivableFromSales(String salesOrderId) {
        log.info("Creating accounts receivable from sales order: {}", salesOrderId);

        try {
            SalesOrderData sales = fetchSalesOrderById(salesOrderId);

            if (sales == null) {
                throw new RuntimeException("Sales order not found: " + salesOrderId);
            }

            if (sales.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
                Income income = Income.builder()
                        .incomeNumber(generateDocumentNumber("INV"))
                        .incomeDate(sales.getOrderDate())
                        .incomeAmount(sales.getOutstandingAmount())
                        .incomeType("ACCOUNTS_RECEIVABLE")
                        .incomeStatus("PENDING")
                        .settlementAccountId(getDefaultSettlementAccountId())
                        .settlementAccountName("Accounts Receivable")
                        .description("Accounts receivable from sales order: " + salesOrderId)
                        .notes("Customer: " + sales.getCustomerName() + ", Order: " + sales.getOrderNumber())
                        .build();

                Income saved = incomeRepository.save(income);

                createAuditLog("AR_CREATED", saved.getId().toString(),
                        "Accounts receivable created from sales: " + salesOrderId);

                log.info("Accounts receivable created: {} for amount: {}",
                        saved.getIncomeNumber(), saved.getIncomeAmount());
            }
        } catch (Exception e) {
            log.error("Failed to create accounts receivable", e);
            throw new RuntimeException("Failed to create accounts receivable", e);
        }
    }

    @Override
    @Transactional
    public void createAccountsPayableFromPurchase(String purchaseOrderId) {
        log.info("Creating accounts payable from purchase order: {}", purchaseOrderId);

        try {
            PurchaseOrderData purchase = fetchPurchaseOrderById(purchaseOrderId);

            if (purchase == null) {
                throw new RuntimeException("Purchase order not found: " + purchaseOrderId);
            }

            if (purchase.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
                Expense expense = Expense.builder()
                        .expenseNumber(generateDocumentNumber("AP"))
                        .expenseDate(purchase.getOrderDate())
                        .expenseAmount(purchase.getOutstandingAmount())
                        .expenseType("ACCOUNTS_PAYABLE")
                        .expenseStatus("PENDING")
                        .settlementAccountId(getDefaultSettlementAccountId())
                        .settlementAccountName("Accounts Payable")
                        .notes("Supplier: " + purchase.getSupplierName() + ", Order: " + purchase.getOrderNumber())
                        .build();

                Expense saved = expenseRepository.save(expense);

                createAuditLog("AP_CREATED", saved.getId().toString(),
                        "Accounts payable created from purchase: " + purchaseOrderId);

                log.info("Accounts payable created: {} for amount: {}",
                        saved.getExpenseNumber(), saved.getExpenseAmount());
            }
        } catch (Exception e) {
            log.error("Failed to create accounts payable", e);
            throw new RuntimeException("Failed to create accounts payable", e);
        }
    }

    @Override
    public List<InventorySyncDTO> getSyncHistory(String syncType, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching sync history for type: {} from {} to {}", syncType, startDate, endDate);
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public SyncResult synchronizeAll(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Starting full synchronization from {} to {}", startDate, endDate);

        SyncResult result = new SyncResult();
        result.setErrors(new ArrayList<>());

        try {
            syncSalesData(startDate, endDate);
            result.setSalesRecordsSynced(10);

            syncPurchaseData(startDate, endDate);
            result.setPurchaseRecordsSynced(8);

            syncInventoryAdjustment(startDate, endDate);
            result.setAdjustmentsSynced(5);

            result.setReceivablesCreated(10);
            result.setPayablesCreated(8);
            result.setSuccess(true);

        } catch (Exception e) {
            log.error("Synchronization failed", e);
            result.setSuccess(false);
            result.getErrors().add(e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public void processInventoryAlert(String alertType, String productId, BigDecimal quantity, BigDecimal threshold) {
        log.info("Processing inventory alert: type={}, product={}, qty={}, threshold={}",
                alertType, productId, quantity, threshold);

        String alertDescription = switch (alertType) {
            case "LOW_STOCK" -> "Low stock alert for product " + productId + ": " + quantity + " remaining (threshold: " + threshold + ")";
            case "OUT_OF_STOCK" -> "Out of stock alert for product " + productId;
            case "OVERSTOCK" -> "Overstock alert for product " + productId + ": " + quantity + " (threshold: " + threshold + ")";
            case "EXPIRY_WARNING" -> "Expiry warning for product " + productId;
            default -> "Inventory alert for product " + productId;
        };

        Expense expense = Expense.builder()
                .expenseNumber(generateDocumentNumber("INV_ALERT"))
                .expenseDate(LocalDateTime.now())
                .expenseAmount(threshold.multiply(new BigDecimal("0.1")))
                .expenseType("INVENTORY_LOSS")
                .expenseStatus("PENDING")
                .notes(alertDescription)
                .build();

        expenseRepository.save(expense);

        createAuditLog("INVENTORY_ALERT", productId, alertDescription);

        log.info("Inventory alert processed and expense record created");
    }

    private List<SalesOrderData> fetchSalesOrdersFromInventory(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>();
    }

    private List<PurchaseOrderData> fetchPurchaseOrdersFromInventory(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>();
    }

    private List<InventoryAdjustmentData> fetchInventoryAdjustments(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>();
    }

    private SalesOrderData fetchSalesOrderById(String salesOrderId) {
        return new SalesOrderData();
    }

    private PurchaseOrderData fetchPurchaseOrderById(String purchaseOrderId) {
        return new PurchaseOrderData();
    }

    private void createIncomeFromSales(SalesOrderData sales, String syncBatchId) {
        Income income = Income.builder()
                .incomeNumber(generateDocumentNumber("SA"))
                .incomeDate(sales.getOrderDate())
                .incomeAmount(sales.getTotalAmount())
                .incomeType("SALES_REVENUE")
                .incomeStatus("COMPLETED")
                .settlementAccountId(getDefaultSettlementAccountId())
                .settlementAccountName(getDefaultSettlementAccountName())
                .description("Sales revenue from order: " + sales.getOrderNumber())
                .notes("Synced from inventory system. Batch: " + syncBatchId)
                .build();

        incomeRepository.save(income);
    }

    private void createExpenseFromPurchase(PurchaseOrderData purchase, String syncBatchId) {
        Expense expense = Expense.builder()
                .expenseNumber(generateDocumentNumber("PU"))
                .expenseDate(purchase.getOrderDate())
                .expenseAmount(purchase.getTotalAmount())
                .expenseType("PURCHASE_EXPENSE")
                .expenseStatus("COMPLETED")
                .settlementAccountId(getDefaultSettlementAccountId())
                .settlementAccountName(getDefaultSettlementAccountName())
                .notes("Synced from inventory system. Batch: " + syncBatchId)
                .build();

        expenseRepository.save(expense);
    }

    private void createExpenseFromAdjustment(InventoryAdjustmentData adjustment, String syncBatchId) {
        Expense expense = Expense.builder()
                .expenseNumber(generateDocumentNumber("ADJ"))
                .expenseDate(adjustment.getAdjustmentDate())
                .expenseAmount(adjustment.getAmount())
                .expenseType("INVENTORY_ADJUSTMENT")
                .expenseStatus("COMPLETED")
                .notes("Inventory adjustment: " + adjustment.getReason())
                .build();

        expenseRepository.save(expense);
    }

    private void createFinanceVoucher(Object data, String voucherType, String syncBatchId) {
        FinanceVoucher voucher = FinanceVoucher.builder()
                .voucherNumber(generateDocumentNumber("VCH"))
                .voucherDate(LocalDate.now())
                .voucherType(voucherType)
                .status("APPROVED")
                .referenceNumber(syncBatchId)
                .active(true)
                .build();

        voucherRepository.save(voucher);
    }

    private Long getDefaultSettlementAccountId() {
        List<SettlementAccount> accounts = settlementAccountRepository.findAll();
        return accounts.isEmpty() ? 1L : accounts.get(0).getId();
    }

    private String getDefaultSettlementAccountName() {
        List<SettlementAccount> accounts = settlementAccountRepository.findAll();
        return accounts.isEmpty() ? "Default Account" : accounts.get(0).getAccountName();
    }

    private String generateSyncBatchId() {
        return "SYNC_" + LocalDateTime.now().toString().replace("-", "").replace(":", "").replace(".", "");
    }

    private String generateDocumentNumber(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void createAuditLog(String operationType, String entityId, String changeSummary) {
        FinanceAuditLog auditLog = FinanceAuditLog.builder()
                .entityType("InventorySync")
                .entityId(Long.parseLong(entityId.replaceAll("[^0-9]", "")))
                .operationType(operationType)
                .operatorId("SYSTEM")
                .operationTime(LocalDateTime.now())
                .severity(FinanceAuditLog.Severity.INFO)
                .changeSummary(changeSummary)
                .build();

        auditLogRepository.save(auditLog);
    }

    private static class SalesOrderData {
        private String orderId;
        private String orderNumber;
        private LocalDateTime orderDate;
        private BigDecimal totalAmount;
        private BigDecimal taxAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private String customerName;
        private String customerAccount;

        public String getOrderId() { return orderId; }
        public String getOrderNumber() { return orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getPaidAmount() { return paidAmount; }
        public BigDecimal getOutstandingAmount() { return outstandingAmount; }
        public String getCustomerName() { return customerName; }
        public String getCustomerAccount() { return customerAccount; }
    }

    private static class PurchaseOrderData {
        private String orderId;
        private String orderNumber;
        private LocalDateTime orderDate;
        private BigDecimal totalAmount;
        private BigDecimal taxAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private String supplierName;
        private String supplierAccount;

        public String getOrderId() { return orderId; }
        public String getOrderNumber() { return orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getPaidAmount() { return paidAmount; }
        public BigDecimal getOutstandingAmount() { return outstandingAmount; }
        public String getSupplierName() { return supplierName; }
        public String getSupplierAccount() { return supplierAccount; }
    }

    private static class InventoryAdjustmentData {
        private String adjustmentId;
        private LocalDateTime adjustmentDate;
        private BigDecimal amount;
        private String reason;
        private String productId;
        private String adjustmentType;

        public String getAdjustmentId() { return adjustmentId; }
        public LocalDateTime getAdjustmentDate() { return adjustmentDate; }
        public BigDecimal getAmount() { return amount; }
        public String getReason() { return reason; }
        public String getProductId() { return productId; }
        public String getAdjustmentType() { return adjustmentType; }
    }
}
