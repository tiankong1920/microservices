package com.inventory.reportservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "finance-service")
public interface FinanceServiceClient {

    // 获取收入明细
    @GetMapping("/api/finance-accounts/incomes/date-range")
    List<IncomeDTO> getIncomesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);

    // 获取支出明细
    @GetMapping("/api/finance-accounts/expenses/date-range")
    List<ExpenseDTO> getExpensesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);

    // 获取结算账户余额
    @GetMapping("/api/finance-accounts/settlement-accounts")
    List<SettlementAccountDTO> getSettlementAccounts();

    // 收入DTO
    record IncomeDTO(
            Long id,
            String incomeNumber,
            String incomeType,
            java.math.BigDecimal amount,
            LocalDateTime incomeDate,
            String description,
            String source,
            Long relatedId,
            Long settlementAccountId,
            String status
    ) {
    }

    // 支出DTO
    record ExpenseDTO(
            Long id,
            String expenseNumber,
            String expenseType,
            java.math.BigDecimal amount,
            LocalDateTime expenseDate,
            String description,
            String category,
            Long relatedId,
            Long settlementAccountId,
            String status
    ) {
    }

    // 结算账户DTO
    record SettlementAccountDTO(
            Long id,
            String accountNumber,
            String accountName,
            String accountType,
            java.math.BigDecimal balance,
            String currency,
            String status
    ) {
    }
}
