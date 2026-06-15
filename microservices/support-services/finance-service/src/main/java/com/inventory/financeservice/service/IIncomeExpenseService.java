package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.dto.ExpenseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IIncomeExpenseService {

    IncomeDTO createIncome(IncomeDTO incomeDTO);

    IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO);

    Optional<IncomeDTO> getIncomeById(Long id);

    Optional<IncomeDTO> getIncomeByNumber(String incomeNumber);

    List<IncomeDTO> getAllIncomes();

    List<IncomeDTO> getIncomesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<IncomeDTO> getIncomesByType(String incomeType);

    List<IncomeDTO> getIncomesByStatus(String status);

    List<IncomeDTO> getIncomesBySettlementAccount(Long accountId);

    List<IncomeDTO> searchIncomes(String keyword);

    IncomeDTO approveIncome(Long id, String approverId);

    IncomeDTO rejectIncome(Long id, String rejectorId, String reason);

    IncomeDTO cancelIncome(Long id);

    ExpenseDTO createExpense(ExpenseDTO expenseDTO);

    ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO);

    Optional<ExpenseDTO> getExpenseById(Long id);

    Optional<ExpenseDTO> getExpenseByNumber(String expenseNumber);

    List<ExpenseDTO> getAllExpenses();

    List<ExpenseDTO> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<ExpenseDTO> getExpensesByType(String expenseType);

    List<ExpenseDTO> getExpensesByStatus(String status);

    List<ExpenseDTO> getExpensesBySettlementAccount(Long accountId);

    List<ExpenseDTO> searchExpenses(String keyword);

    ExpenseDTO approveExpense(Long id, String approverId);

    ExpenseDTO rejectExpense(Long id, String rejectorId, String reason);

    ExpenseDTO cancelExpense(Long id);

    BigDecimal getTotalIncomeByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getTotalExpenseByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getNetProfitByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<IncomeDTO> getPendingApprovalIncomes();

    List<ExpenseDTO> getPendingApprovalExpenses();

    byte[] exportIncomesToExcel(LocalDateTime startDate, LocalDateTime endDate);

    byte[] exportExpensesToExcel(LocalDateTime startDate, LocalDateTime endDate);
}
