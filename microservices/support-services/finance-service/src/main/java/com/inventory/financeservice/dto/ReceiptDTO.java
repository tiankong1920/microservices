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
public class ReceiptDTO {

    private Long id;
    private String receiptNumber;
    private Long customerId;
    private String customerName;
    private LocalDateTime receiptDate;
    private BigDecimal receiptAmount;
    private String paymentMethod;
    private String relatedDocumentType;
    private String relatedDocumentNumber;
    private Long relatedDocumentId;
    private String receiptStatus;
    private String receiver;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
