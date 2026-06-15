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
@Table(name = "expense")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    public static final String SEQUENCE_NAME = "expense_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_number", nullable = false, unique = true, length = 50)
    private String expenseNumber;

    @Column(name = "expense_date", nullable = false)
    private LocalDateTime expenseDate;

    @Column(name = "expense_amount", precision = 19, scale = 4)
    private BigDecimal expenseAmount;

    @Column(name = "expense_type", nullable = false, length = 50)
    private String expenseType;

    @Column(name = "expense_category", length = 50)
    private String expenseCategory;

    @Column(name = "expense_status", nullable = false, length = 20)
    private String expenseStatus;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    @Column(name = "settlement_account_id")
    private Long settlementAccountId;

    @Column(name = "settlement_account_name", length = 200)
    private String settlementAccountName;

    @Column(name = "counterparty_name", length = 200)
    private String counterpartyName;

    @Column(name = "counterparty_account", length = 100)
    private String counterpartyAccount;

    @Column(name = "project_id", length = 50)
    private String projectId;

    @Column(name = "project_name", length = 200)
    private String projectName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 200)
    private String departmentName;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_type", length = 50)
    private String invoiceType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approver_id", length = 50)
    private String approverId;

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (expenseDate == null) {
            expenseDate = LocalDateTime.now();
        }
        if (expenseStatus == null) {
            expenseStatus = "PENDING";
        }
        if (approvalStatus == null) {
            approvalStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getStatus() {
        return this.expenseStatus;
    }

    public void setStatus(String status) {
        this.expenseStatus = status;
    }
}
