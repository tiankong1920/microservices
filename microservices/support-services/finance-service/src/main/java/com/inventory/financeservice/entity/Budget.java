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
@Table(name = "budget")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_code", nullable = false, unique = true, length = 50)
    private String budgetCode;

    @Column(name = "budget_name", nullable = false, length = 200)
    private String budgetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false, length = 30)
    private BudgetType budgetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_period", nullable = false, length = 30)
    private BudgetPeriod budgetPeriod;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "used_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal usedAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 200)
    private String departmentName;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", length = 200)
    private String projectName;

    @Column(name = "expense_category", length = 100)
    private String expenseCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.DRAFT;

    @Column(name = "warning_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal warningThreshold = new BigDecimal("80.00");

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public enum BudgetType {
        DEPARTMENT, PROJECT, EXPENSE_CATEGORY, OVERALL
    }

    public enum BudgetPeriod {
        MONTHLY, QUARTERLY, YEARLY
    }

    public enum BudgetStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usedAmount == null) usedAmount = BigDecimal.ZERO;
        if (remainingAmount == null) remainingAmount = totalAmount;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
