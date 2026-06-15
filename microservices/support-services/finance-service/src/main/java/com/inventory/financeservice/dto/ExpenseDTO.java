package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.Expense;
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
public class ExpenseDTO {

    private Long id;
    private String expenseNumber;
    private LocalDateTime expenseDate;
    private BigDecimal expenseAmount;
    private String expenseType;
    private String expenseCategory;
    private Long settlementAccountId;
    private String settlementAccountName;
    private String counterpartyName;
    private String counterpartyAccount;
    private String projectId;
    private String projectName;
    private Long departmentId;
    private String departmentName;
    private String invoiceNumber;
    private String invoiceType;
    private String notes;
    private String status;
    private String approvalStatus;
    private String approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static ExpenseDTO fromEntity(Expense expense) {
        if (expense == null) return null;

        return ExpenseDTO.builder()
                .id(expense.getId())
                .expenseNumber(expense.getExpenseNumber())
                .expenseDate(expense.getExpenseDate())
                .expenseAmount(expense.getExpenseAmount())
                .expenseType(expense.getExpenseType())
                .expenseCategory(expense.getExpenseCategory())
                .settlementAccountId(expense.getSettlementAccountId())
                .settlementAccountName(expense.getSettlementAccountName())
                .counterpartyName(expense.getCounterpartyName())
                .counterpartyAccount(expense.getCounterpartyAccount())
                .projectId(expense.getProjectId())
                .projectName(expense.getProjectName())
                .departmentId(expense.getDepartmentId())
                .departmentName(expense.getDepartmentName())
                .invoiceNumber(expense.getInvoiceNumber())
                .invoiceType(expense.getInvoiceType())
                .notes(expense.getNotes())
                .status(expense.getStatus())
                .approvalStatus(expense.getApprovalStatus())
                .approverId(expense.getApproverId())
                .approverName(expense.getApproverName())
                .approvedAt(expense.getApprovedAt())
                .rejectionReason(expense.getRejectionReason())
                .createdBy(expense.getCreatedBy())
                .createdAt(expense.getCreatedAt())
                .updatedBy(expense.getUpdatedBy())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    public Expense toEntity() {
        return Expense.builder()
                .id(this.id)
                .expenseNumber(this.expenseNumber)
                .expenseDate(this.expenseDate)
                .expenseAmount(this.expenseAmount)
                .expenseType(this.expenseType)
                .expenseCategory(this.expenseCategory)
                .settlementAccountId(this.settlementAccountId)
                .settlementAccountName(this.settlementAccountName)
                .counterpartyName(this.counterpartyName)
                .counterpartyAccount(this.counterpartyAccount)
                .projectId(this.projectId)
                .projectName(this.projectName)
                .departmentId(this.departmentId)
                .departmentName(this.departmentName)
                .invoiceNumber(this.invoiceNumber)
                .invoiceType(this.invoiceType)
                .notes(this.notes)
                .expenseStatus(this.status != null ? this.status : "PENDING")
                .approvalStatus(this.approvalStatus != null ? this.approvalStatus : "PENDING")
                .approverId(this.approverId)
                .approverName(this.approverName)
                .approvedAt(this.approvedAt)
                .rejectionReason(this.rejectionReason)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TYPE_PURCHASE_EXPENSE = "PURCHASE_EXPENSE";
    public static final String TYPE_OPERATING_EXPENSE = "OPERATING_EXPENSE";
    public static final String TYPE_SALARY_EXPENSE = "SALARY_EXPENSE";
    public static final String TYPE_MARKETING_EXPENSE = "MARKETING_EXPENSE";
    public static final String TYPE_RND_EXPENSE = "RND_EXPENSE";
    public static final String TYPE_ACCOUNTS_PAYABLE = "ACCOUNTS_PAYABLE";
    public static final String TYPE_INVENTORY_LOSS = "INVENTORY_LOSS";
    public static final String TYPE_OTHER_EXPENSE = "OTHER_EXPENSE";
}
