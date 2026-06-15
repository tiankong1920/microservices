package com.inventory.financeservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementAccount {
    public static final String SEQUENCE_NAME = "settlement_account_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 200)
    private String bankBranch;

    @Column(name = "account_holder", length = 100)
    private String accountHolder;

    @Column(name = "balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "CNY";

    @Column(name = "minimum_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
        if (minimumBalance == null) minimumBalance = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
