package com.inventory.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySyncDTO {

    private Long id;
    private String syncType;
    private String sourceSystem;
    private String sourceDocumentId;
    private String sourceDocumentNumber;
    private String documentType;
    private LocalDateTime documentDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String counterpartyName;
    private String counterpartyAccount;
    private String status;
    private String relatedFinanceDocumentId;
    private String relatedFinanceDocumentNumber;
    private String syncStatus;
    private String errorMessage;
    private LocalDateTime syncedAt;
    private String syncedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final String SYNC_TYPE_SALES = "SALES";
    public static final String SYNC_TYPE_PURCHASE = "PURCHASE";
    public static final String SYNC_TYPE_ADJUSTMENT = "ADJUSTMENT";
    public static final String SYNC_TYPE_RETURN = "RETURN";

    public static final String SYNC_STATUS_PENDING = "PENDING";
    public static final String SYNC_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String SYNC_STATUS_COMPLETED = "COMPLETED";
    public static final String SYNC_STATUS_FAILED = "FAILED";
}
