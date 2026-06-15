package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.dto.SettlementAccountDTO;
import com.inventory.financeservice.entity.Income;
import com.inventory.financeservice.entity.Expense;
import com.inventory.financeservice.entity.SettlementAccount;
import com.inventory.financeservice.repository.IIncomeRepository;
import com.inventory.financeservice.repository.IExpenseRepository;
import com.inventory.financeservice.repository.ISettlementAccountRepository;
import com.inventory.financeservice.service.IFinanceAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FinanceAccountServiceImpl implements IFinanceAccountService {

    private final IIncomeRepository incomeRepository;
    private final IExpenseRepository expenseRepository;
    private final ISettlementAccountRepository settlementAccountRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_INCOME_NOT_FOUND = "Income not found with id: ";
    private static final String ERROR_EXPENSE_NOT_FOUND = "Expense not found with id: ";
    private static final String ERROR_SETTLEMENT_ACCOUNT_NOT_FOUND = "Settlement account not found with id: ";

    @Override
    public List<IncomeDTO> getAllIncomes() {
        log.info("Getting all incomes");
        final List<Income> incomes = incomeRepository.findAll();
        log.info("Found {} incomes", incomes.size());
        return incomes.stream()
                .map(income -> modelMapper.map(income, IncomeDTO.class))
                .toList();
    }

    @Override
    public IncomeDTO getIncomeById(Long id) {
        log.info("Getting income by id: {}", id);
        final Income income = incomeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_INCOME_NOT_FOUND, id);
                    return new RuntimeException("Income not found with id: " + id);
                });
        return modelMapper.map(income, IncomeDTO.class);
    }

    @Override
    public IncomeDTO getIncomeByIncomeNumber(String incomeNumber) {
        log.info("Getting income by income number: {}", incomeNumber);
        final Income income = incomeRepository.findByIncomeNumber(incomeNumber)
                .orElseThrow(() -> {
                    log.error("Income not found with income number: {}", incomeNumber);
                    return new RuntimeException("Income not found with number: " + incomeNumber);
                });
        return modelMapper.map(income, IncomeDTO.class);
    }

    @Override
    public List<IncomeDTO> getIncomesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting incomes by date range: {} to {}", startDate, endDate);
        final List<Income> incomes = incomeRepository.findByIncomeDateBetween(startDate, endDate);
        log.info("Found {} incomes in date range", incomes.size());
        return incomes.stream()
                .map(income -> modelMapper.map(income, IncomeDTO.class))
                .toList();
    }

    @Override
    public List<IncomeDTO> getIncomesByIncomeType(String incomeType) {
        log.info("Getting incomes by income type: {}", incomeType);
        final List<Income> incomes = incomeRepository.findByIncomeType(incomeType);
        log.info("Found {} incomes with income type: {}", incomes.size(), incomeType);
        return incomes.stream()
                .map(income -> modelMapper.map(income, IncomeDTO.class))
                .toList();
    }

    @Override
    public List<IncomeDTO> getIncomesBySettlementAccountId(Long settlementAccountId) {
        log.info("Getting incomes by settlement account id: {}", settlementAccountId);
        final List<Income> incomes = incomeRepository.findBySettlementAccountId(settlementAccountId);
        log.info("Found {} incomes for settlement account id: {}", incomes.size(), settlementAccountId);
        return incomes.stream()
                .map(income -> modelMapper.map(income, IncomeDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public IncomeDTO createIncome(IncomeDTO incomeDTO) {
        log.info("Creating income: {}", incomeDTO.getIncomeNumber());

        final Income income = modelMapper.map(incomeDTO, Income.class);
        income.setIncomeDate(LocalDateTime.now());
        if (income.getIncomeStatus() == null) {
            income.setIncomeStatus("COMPLETED");
        }

        final Income savedIncome = incomeRepository.save(income);
        log.info("Income created successfully with id: {}", savedIncome.getId());
        return modelMapper.map(savedIncome, IncomeDTO.class);
    }

    @Override
    @Transactional
    public IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO) {
        log.info("Updating income with id: {}", id);

        final Income existingIncome = incomeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_INCOME_NOT_FOUND, id);
                    return new RuntimeException("Income not found with id: " + id);
                });

        modelMapper.map(incomeDTO, existingIncome);
        final Income updatedIncome = incomeRepository.save(existingIncome);
        log.info("Income updated successfully with id: {}", updatedIncome.getId());
        return modelMapper.map(updatedIncome, IncomeDTO.class);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id) {
        log.info("Deleting income with id: {}", id);

        if (!incomeRepository.existsById(id)) {
            log.error(ERROR_INCOME_NOT_FOUND, id);
            throw new IllegalStateException("Income not found with id: " + id);
        }

        incomeRepository.deleteById(id);
        log.info("Income deleted successfully with id: {}", id);
    }

    @Override
    public List<ExpenseDTO> getAllExpenses() {
        log.info("Getting all expenses");
        final List<Expense> expenses = expenseRepository.findAll();
        log.info("Found {} expenses", expenses.size());
        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        log.info("Getting expense by id: {}", id);
        final Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_EXPENSE_NOT_FOUND, id);
                    return new RuntimeException("Expense not found with id: " + id);
                });
        return modelMapper.map(expense, ExpenseDTO.class);
    }

    @Override
    public ExpenseDTO getExpenseByExpenseNumber(String expenseNumber) {
        log.info("Getting expense by expense number: {}", expenseNumber);
        final Expense expense = expenseRepository.findByExpenseNumber(expenseNumber)
                .orElseThrow(() -> {
                    log.error("Expense not found with expense number: {}", expenseNumber);
                    return new RuntimeException("Expense not found with number: " + expenseNumber);
                });
        return modelMapper.map(expense, ExpenseDTO.class);
    }

    @Override
    public List<ExpenseDTO> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting expenses by date range: {} to {}", startDate, endDate);
        final List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        log.info("Found {} expenses in date range", expenses.size());
        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();
    }

    @Override
    public List<ExpenseDTO> getExpensesByExpenseType(String expenseType) {
        log.info("Getting expenses by expense type: {}", expenseType);
        final List<Expense> expenses = expenseRepository.findByExpenseType(expenseType);
        log.info("Found {} expenses with expense type: {}", expenses.size(), expenseType);
        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();
    }

    @Override
    public List<ExpenseDTO> getExpensesBySettlementAccountId(Long settlementAccountId) {
        log.info("Getting expenses by settlement account id: {}", settlementAccountId);
        final List<Expense> expenses = expenseRepository.findBySettlementAccountId(settlementAccountId);
        log.info("Found {} expenses for settlement account id: {}", expenses.size(), settlementAccountId);
        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        log.info("Creating expense: {}", expenseDTO.getExpenseNumber());

        final Expense expense = modelMapper.map(expenseDTO, Expense.class);
        expense.setExpenseDate(LocalDateTime.now());
        if (expense.getStatus() == null) {
            expense.setStatus("COMPLETED");
        }

        final Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created successfully with id: {}", savedExpense.getId());
        return modelMapper.map(savedExpense, ExpenseDTO.class);
    }

    @Override
    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        log.info("Updating expense with id: {}", id);

        final Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_EXPENSE_NOT_FOUND, id);
                    return new RuntimeException("Expense not found with id: " + id);
                });

        modelMapper.map(expenseDTO, existingExpense);
        final Expense updatedExpense = expenseRepository.save(existingExpense);
        log.info("Expense updated successfully with id: {}", updatedExpense.getId());
        return modelMapper.map(updatedExpense, ExpenseDTO.class);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        log.info("Deleting expense with id: {}", id);

        if (!expenseRepository.existsById(id)) {
            log.error(ERROR_EXPENSE_NOT_FOUND, id);
            throw new IllegalStateException("Expense not found with id: " + id);
        }

        expenseRepository.deleteById(id);
        log.info("Expense deleted successfully with id: {}", id);
    }

    @Override
    public List<SettlementAccountDTO> getAllSettlementAccounts() {
        log.info("Getting all settlement accounts");
        final List<SettlementAccount> accounts = settlementAccountRepository.findAll();
        log.info("Found {} settlement accounts", accounts.size());
        return accounts.stream()
                .map(account -> modelMapper.map(account, SettlementAccountDTO.class))
                .toList();
    }

    @Override
    public SettlementAccountDTO getSettlementAccountById(Long id) {
        log.info("Getting settlement account by id: {}", id);
        final SettlementAccount account = settlementAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SETTLEMENT_ACCOUNT_NOT_FOUND, id);
                    return new RuntimeException("Settlement account not found with id: " + id);
                });
        return modelMapper.map(account, SettlementAccountDTO.class);
    }

    @Override
    public SettlementAccountDTO getSettlementAccountByAccountNumber(String accountNumber) {
        log.info("Getting settlement account by account number: {}", accountNumber);
        final SettlementAccount account = settlementAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Settlement account not found with account number: {}", accountNumber);
                    return new RuntimeException("Settlement account not found with number: " + accountNumber);
                });
        return modelMapper.map(account, SettlementAccountDTO.class);
    }

    @Override
    public List<SettlementAccountDTO> getSettlementAccountsByAccountType(String accountType) {
        log.info("Getting settlement accounts by account type: {}", accountType);
        final List<SettlementAccount> accounts = settlementAccountRepository.findByAccountType(accountType);
        log.info("Found {} settlement accounts with account type: {}", accounts.size(), accountType);
        return accounts.stream()
                .map(account -> modelMapper.map(account, SettlementAccountDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public SettlementAccountDTO createSettlementAccount(SettlementAccountDTO accountDTO) {
        log.info("Creating settlement account: {}", accountDTO.getAccountNumber());

        final SettlementAccount account = modelMapper.map(accountDTO, SettlementAccount.class);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        if (account.getIsActive() == null) {
            account.setIsActive(true);
        }

        final SettlementAccount savedAccount = settlementAccountRepository.save(account);
        log.info("Settlement account created successfully with id: {}", savedAccount.getId());
        return modelMapper.map(savedAccount, SettlementAccountDTO.class);
    }

    @Override
    @Transactional
    public SettlementAccountDTO updateSettlementAccount(Long id, SettlementAccountDTO accountDTO) {
        log.info("Updating settlement account with id: {}", id);

        final SettlementAccount existingAccount = settlementAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SETTLEMENT_ACCOUNT_NOT_FOUND, id);
                    return new RuntimeException("Settlement account not found with id: " + id);
                });

        modelMapper.map(accountDTO, existingAccount);
        final SettlementAccount updatedAccount = settlementAccountRepository.save(existingAccount);
        log.info("Settlement account updated successfully with id: {}", updatedAccount.getId());
        return modelMapper.map(updatedAccount, SettlementAccountDTO.class);
    }

    @Override
    @Transactional
    public void deleteSettlementAccount(Long id) {
        log.info("Deleting settlement account with id: {}", id);

        if (!settlementAccountRepository.existsById(id)) {
            log.error(ERROR_SETTLEMENT_ACCOUNT_NOT_FOUND, id);
            throw new IllegalStateException("Settlement account not found with id: " + id);
        }

        settlementAccountRepository.deleteById(id);
        log.info("Settlement account deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public SettlementAccountDTO adjustAccountBalance(Long id, BigDecimal amount) {
        log.info("Adjusting account balance for id: {} by amount: {}", id, amount);

        final SettlementAccount account = settlementAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SETTLEMENT_ACCOUNT_NOT_FOUND, id);
                    return new RuntimeException("Settlement account not found with id: " + id);
                });

        final BigDecimal currentBalance =
                account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        account.setBalance(currentBalance.add(amount));
        final SettlementAccount updatedAccount = settlementAccountRepository.save(account);
        log.info("Account balance adjusted successfully with id: {}", updatedAccount.getId());
        return modelMapper.map(updatedAccount, SettlementAccountDTO.class);
    }
}
