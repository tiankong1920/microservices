package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "bank_statement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statement_number", nullable = false, unique = true, length = 50)
    private String statementNumber;

    @Column(name = "bank_code", nullable = false, length = 50)
    private String bankCode;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(name = "transaction_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal transactionAmount;

    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_name", length = 200)
    private String counterpartyName;

    @Column(name = "counterparty_account", length = 50)
    private String counterpartyAccount;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false, length = 20)
    @Builder.Default
    private ReconciliationStatus reconciliationStatus = ReconciliationStatus.UNRECONCILED;

    @Column(name = "matched_transaction_id")
    private Long matchedTransactionId;

    @Column(name = "matched_transaction_number", length = 50)
    private String matchedTransactionNumber;

    @Column(name = "import_batch_id", length = 50)
    private String importBatchId;

    @Column(name = "import_source", length = 50)
    private String importSource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReconciliationStatus {
        UNRECONCILED, PARTIAL_MATCH, FULL_MATCH, EXCEPTION, MANUAL
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
