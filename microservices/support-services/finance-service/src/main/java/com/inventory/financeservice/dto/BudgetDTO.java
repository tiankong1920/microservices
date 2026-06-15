package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.Budget;
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
public class BudgetDTO {

    private Long id;
    private String budgetCode;
    private String budgetName;
    private String budgetType;
    private String budgetPeriod;
    private Integer fiscalYear;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private Long departmentId;
    private String departmentName;
    private Long projectId;
    private String projectName;
    private String expenseCategory;
    private String status;
    private BigDecimal warningThreshold;
    private BigDecimal utilizationRate;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BudgetDTO fromEntity(Budget budget) {
        if (budget == null) return null;

        BigDecimal utilizationRate = BigDecimal.ZERO;
        if (budget.getTotalAmount() != null && budget.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            utilizationRate = budget.getUsedAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(budget.getTotalAmount(), 2, BigDecimal.ROUND_HALF_UP);
        }

        return BudgetDTO.builder()
                .id(budget.getId())
                .budgetCode(budget.getBudgetCode())
                .budgetName(budget.getBudgetName())
                .budgetType(budget.getBudgetType() != null ? budget.getBudgetType().name() : null)
                .budgetPeriod(budget.getBudgetPeriod() != null ? budget.getBudgetPeriod().name() : null)
                .fiscalYear(budget.getFiscalYear())
                .periodStart(budget.getPeriodStart())
                .periodEnd(budget.getPeriodEnd())
                .totalAmount(budget.getTotalAmount())
                .usedAmount(budget.getUsedAmount())
                .remainingAmount(budget.getRemainingAmount())
                .departmentId(budget.getDepartmentId())
                .departmentName(budget.getDepartmentName())
                .projectId(budget.getProjectId())
                .projectName(budget.getProjectName())
                .expenseCategory(budget.getExpenseCategory())
                .status(budget.getStatus() != null ? budget.getStatus().name() : null)
                .warningThreshold(budget.getWarningThreshold())
                .utilizationRate(utilizationRate)
                .description(budget.getDescription())
                .createdBy(budget.getCreatedBy())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }

    public Budget toEntity() {
        return Budget.builder()
                .id(this.id)
                .budgetCode(this.budgetCode)
                .budgetName(this.budgetName)
                .budgetType(this.budgetType != null ? Budget.BudgetType.valueOf(this.budgetType) : null)
                .budgetPeriod(this.budgetPeriod != null ? Budget.BudgetPeriod.valueOf(this.budgetPeriod) : null)
                .fiscalYear(this.fiscalYear)
                .periodStart(this.periodStart)
                .periodEnd(this.periodEnd)
                .totalAmount(this.totalAmount)
                .usedAmount(this.usedAmount != null ? this.usedAmount : BigDecimal.ZERO)
                .remainingAmount(this.remainingAmount != null ? this.remainingAmount : this.totalAmount)
                .departmentId(this.departmentId)
                .departmentName(this.departmentName)
                .projectId(this.projectId)
                .projectName(this.projectName)
                .expenseCategory(this.expenseCategory)
                .status(this.status != null ? Budget.BudgetStatus.valueOf(this.status) : Budget.BudgetStatus.DRAFT)
                .warningThreshold(this.warningThreshold)
                .description(this.description)
                .build();
    }
}
