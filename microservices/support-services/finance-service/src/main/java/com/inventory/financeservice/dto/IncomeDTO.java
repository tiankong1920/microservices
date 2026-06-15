package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.Income;
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
public class IncomeDTO {

    private Long id;
    private String incomeNumber;
    private LocalDateTime incomeDate;
    private BigDecimal incomeAmount;
    private String incomeType;
    private String incomeCategory;
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
    private String description;
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

    public static IncomeDTO fromEntity(Income income) {
        if (income == null) return null;

        return IncomeDTO.builder()
                .id(income.getId())
                .incomeNumber(income.getIncomeNumber())
                .incomeDate(income.getIncomeDate())
                .incomeAmount(income.getIncomeAmount())
                .incomeType(income.getIncomeType())
                .incomeCategory(income.getIncomeCategory())
                .settlementAccountId(income.getSettlementAccountId())
                .settlementAccountName(income.getSettlementAccountName())
                .counterpartyName(income.getCounterpartyName())
                .counterpartyAccount(income.getCounterpartyAccount())
                .projectId(income.getProjectId())
                .projectName(income.getProjectName())
                .departmentId(income.getDepartmentId())
                .departmentName(income.getDepartmentName())
                .invoiceNumber(income.getInvoiceNumber())
                .invoiceType(income.getInvoiceType())
                .description(income.getDescription())
                .notes(income.getNotes())
                .status(income.getStatus())
                .approvalStatus(income.getApprovalStatus())
                .approverId(income.getApproverId())
                .approverName(income.getApproverName())
                .approvedAt(income.getApprovedAt())
                .rejectionReason(income.getRejectionReason())
                .createdBy(income.getCreatedBy())
                .createdAt(income.getCreatedAt())
                .updatedBy(income.getUpdatedBy())
                .updatedAt(income.getUpdatedAt())
                .build();
    }

    public Income toEntity() {
        return Income.builder()
                .id(this.id)
                .incomeNumber(this.incomeNumber)
                .incomeDate(this.incomeDate)
                .incomeAmount(this.incomeAmount)
                .incomeType(this.incomeType)
                .incomeCategory(this.incomeCategory)
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
                .description(this.description)
                .notes(this.notes)
                .incomeStatus(this.status != null ? this.status : "PENDING")
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

    public static final String TYPE_SALES_REVENUE = "SALES_REVENUE";
    public static final String TYPE_SERVICE_REVENUE = "SERVICE_REVENUE";
    public static final String TYPE_OTHER_INCOME = "OTHER_INCOME";
    public static final String TYPE_ACCOUNTS_RECEIVABLE = "ACCOUNTS_RECEIVABLE";
    public static final String TYPE_ADVANCE_RECEIVED = "ADVANCE_RECEIVED";
}
