package com.inventory.reportservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportDTO {
    private Long id;
    private String reportName;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private List<IncomeDetailDTO> incomeDetails;
    private List<ExpenseDetailDTO> expenseDetails;
    private List<AccountBalanceDTO> accountBalances;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeDetailDTO {
        private Long incomeId;
        private LocalDateTime incomeDate;
        private String incomeType;
        private BigDecimal amount;
        private String description;
        private String source;
        private Long relatedId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpenseDetailDTO {
        private Long expenseId;
        private LocalDateTime expenseDate;
        private String expenseType;
        private BigDecimal amount;
        private String description;
        private String category;
        private Long relatedId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountBalanceDTO {
        private Long accountId;
        private String accountName;
        private String accountType;
        private BigDecimal initialBalance;
        private BigDecimal currentBalance;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
    }
}
