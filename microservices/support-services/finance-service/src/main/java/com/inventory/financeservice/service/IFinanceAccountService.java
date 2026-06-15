package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.dto.SettlementAccountDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IFinanceAccountService {

    List<IncomeDTO> getAllIncomes();

    IncomeDTO getIncomeById(Long id);

    IncomeDTO getIncomeByIncomeNumber(String incomeNumber);

    List<IncomeDTO> getIncomesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<IncomeDTO> getIncomesByIncomeType(String incomeType);

    List<IncomeDTO> getIncomesBySettlementAccountId(Long settlementAccountId);

    IncomeDTO createIncome(IncomeDTO incomeDTO);

    IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO);

    void deleteIncome(Long id);

    List<ExpenseDTO> getAllExpenses();

    ExpenseDTO getExpenseById(Long id);

    ExpenseDTO getExpenseByExpenseNumber(String expenseNumber);

    List<ExpenseDTO> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<ExpenseDTO> getExpensesByExpenseType(String expenseType);

    List<ExpenseDTO> getExpensesBySettlementAccountId(Long settlementAccountId);

    ExpenseDTO createExpense(ExpenseDTO expenseDTO);

    ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO);

    void deleteExpense(Long id);

    List<SettlementAccountDTO> getAllSettlementAccounts();

    SettlementAccountDTO getSettlementAccountById(Long id);

    SettlementAccountDTO getSettlementAccountByAccountNumber(String accountNumber);

    List<SettlementAccountDTO> getSettlementAccountsByAccountType(String accountType);

    SettlementAccountDTO createSettlementAccount(SettlementAccountDTO accountDTO);

    SettlementAccountDTO updateSettlementAccount(Long id, SettlementAccountDTO accountDTO);

    void deleteSettlementAccount(Long id);

    SettlementAccountDTO adjustAccountBalance(Long id, BigDecimal amount);
}
