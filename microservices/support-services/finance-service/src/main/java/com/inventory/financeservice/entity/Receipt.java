package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @Column(name = "receipt_amount", precision = 19, scale = 4)
    private BigDecimal receiptAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "related_document_type", length = 50)
    private String relatedDocumentType;

    @Column(name = "related_document_number", length = 50)
    private String relatedDocumentNumber;

    @Column(name = "related_document_id")
    private Long relatedDocumentId;

    @Column(name = "receipt_status", nullable = false, length = 20)
    private String receiptStatus;

    @Column(name = "receiver", length = 100)
    private String receiver;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (receiptDate == null) {
            receiptDate = LocalDateTime.now();
        }
        if (receiptStatus == null) {
            receiptStatus = "RECEIVED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
