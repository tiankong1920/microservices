package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.dto.SettlementAccountDTO;
import com.inventory.financeservice.service.IFinanceAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/finance/accounts")
@RequiredArgsConstructor
@Tag(name = "Finance Account", description = "财务账户管理接口")
@Validated
public class FinanceAccountController {

    private final IFinanceAccountService financeAccountService;

    /**
     * 获取所有收入记录
     *
     * @return 收入列表
     */
    @GetMapping("/incomes")
    @Operation(summary = "获取所有收入记录")
    public ResponseEntity<List<IncomeDTO>> getAllIncomes() {
        final List<IncomeDTO> incomes = financeAccountService.getAllIncomes();
        return ResponseEntity.ok(incomes);
    }

    /**
     * 根据ID获取收入记录
     *
     * @param id 收入ID
     * @return 收入信息
     */
    @GetMapping("/incomes/{id}")
    @Operation(summary = "根据ID获取收入记录")
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable Long id) {
        final IncomeDTO income = financeAccountService.getIncomeById(id);
        return ResponseEntity.ok(income);
    }

    /**
     * 根据收入编号获取收入记录
     *
     * @param incomeNumber 收入编号
     * @return 收入信息
     */
    @GetMapping("/incomes/number/{incomeNumber}")
    @Operation(summary = "根据收入编号获取收入记录")
    public ResponseEntity<IncomeDTO> getIncomeByIncomeNumber(@PathVariable String incomeNumber) {
        final IncomeDTO income = financeAccountService.getIncomeByIncomeNumber(incomeNumber);
        return ResponseEntity.ok(income);
    }

    /**
     * 根据日期范围获取收入记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入列表
     */
    @GetMapping("/incomes/date-range")
    @Operation(summary = "根据日期范围获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<IncomeDTO> incomes = financeAccountService.getIncomesByDateRange(startDate, endDate);
        return ResponseEntity.ok(incomes);
    }

    /**
     * 根据收入类型获取收入记录
     *
     * @param incomeType 收入类型
     * @return 收入列表
     */
    @GetMapping("/incomes/type/{incomeType}")
    @Operation(summary = "根据收入类型获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesByIncomeType(@PathVariable String incomeType) {
        final List<IncomeDTO> incomes = financeAccountService.getIncomesByIncomeType(incomeType);
        return ResponseEntity.ok(incomes);
    }

    /**
     * 根据结算账户获取收入记录
     *
     * @param settlementAccountId 结算账户ID
     * @return 收入列表
     */
    @GetMapping("/incomes/account/{settlementAccountId}")
    @Operation(summary = "根据结算账户获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesBySettlementAccountId(@PathVariable Long settlementAccountId) {
        final List<IncomeDTO> incomes = financeAccountService.getIncomesBySettlementAccountId(settlementAccountId);
        return ResponseEntity.ok(incomes);
    }

    /**
     * 创建收入记录
     *
     * @param incomeDTO 收入数据
     * @return 创建的收入信息
     */
    @PostMapping("/incomes")
    @Operation(summary = "创建收入记录")
    public ResponseEntity<IncomeDTO> createIncome(@Valid @RequestBody IncomeDTO incomeDTO) {
        final IncomeDTO createdIncome = financeAccountService.createIncome(incomeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIncome);
    }

    /**
     * 更新收入记录
     *
     * @param id 收入ID
     * @param incomeDTO 更新后的收入数据
     * @return 更新后的收入信息
     */
    @PutMapping("/incomes/{id}")
    @Operation(summary = "更新收入记录")
    public ResponseEntity<IncomeDTO> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeDTO incomeDTO) {
        final IncomeDTO updatedIncome = financeAccountService.updateIncome(id, incomeDTO);
        return ResponseEntity.ok(updatedIncome);
    }

    /**
     * 删除收入记录
     *
     * @param id 收入ID
     * @return 无内容响应
     */
    @DeleteMapping("/incomes/{id}")
    @Operation(summary = "删除收入记录")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        financeAccountService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取所有支出记录
     *
     * @return 支出列表
     */
    @GetMapping("/expenses")
    @Operation(summary = "获取所有支出记录")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        final List<ExpenseDTO> expenses = financeAccountService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    /**
     * 根据ID获取支出记录
     *
     * @param id 支出ID
     * @return 支出信息
     */
    @GetMapping("/expenses/{id}")
    @Operation(summary = "根据ID获取支出记录")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        final ExpenseDTO expense = financeAccountService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * 根据支出编号获取支出记录
     *
     * @param expenseNumber 支出编号
     * @return 支出信息
     */
    @GetMapping("/expenses/number/{expenseNumber}")
    @Operation(summary = "根据支出编号获取支出记录")
    public ResponseEntity<ExpenseDTO> getExpenseByExpenseNumber(@PathVariable String expenseNumber) {
        final ExpenseDTO expense = financeAccountService.getExpenseByExpenseNumber(expenseNumber);
        return ResponseEntity.ok(expense);
    }

    /**
     * 根据日期范围获取支出记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出列表
     */
    @GetMapping("/expenses/date-range")
    @Operation(summary = "根据日期范围获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<ExpenseDTO> expenses = financeAccountService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    /**
     * 根据支出类型获取支出记录
     *
     * @param expenseType 支出类型
     * @return 支出列表
     */
    @GetMapping("/expenses/type/{expenseType}")
    @Operation(summary = "根据支出类型获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByExpenseType(@PathVariable String expenseType) {
        final List<ExpenseDTO> expenses = financeAccountService.getExpensesByExpenseType(expenseType);
        return ResponseEntity.ok(expenses);
    }

    /**
     * 根据结算账户获取支出记录
     *
     * @param settlementAccountId 结算账户ID
     * @return 支出列表
     */
    @GetMapping("/expenses/account/{settlementAccountId}")
    @Operation(summary = "根据结算账户获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesBySettlementAccountId(@PathVariable Long settlementAccountId) {
        final List<ExpenseDTO> expenses = financeAccountService.getExpensesBySettlementAccountId(settlementAccountId);
        return ResponseEntity.ok(expenses);
    }

    /**
     * 创建支出记录
     *
     * @param expenseDTO 支出数据
     * @return 创建的支出信息
     */
    @PostMapping("/expenses")
    @Operation(summary = "创建支出记录")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        final ExpenseDTO createdExpense = financeAccountService.createExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    /**
     * 更新支出记录
     *
     * @param id 支出ID
     * @param expenseDTO 更新后的支出数据
     * @return 更新后的支出信息
     */
    @PutMapping("/expenses/{id}")
    @Operation(summary = "更新支出记录")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        final ExpenseDTO updatedExpense = financeAccountService.updateExpense(id, expenseDTO);
        return ResponseEntity.ok(updatedExpense);
    }

    /**
     * 删除支出记录
     *
     * @param id 支出ID
     * @return 无内容响应
     */
    @DeleteMapping("/expenses/{id}")
    @Operation(summary = "删除支出记录")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        financeAccountService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取所有结算账户
     *
     * @return 结算账户列表
     */
    @GetMapping("/settlement-accounts")
    @Operation(summary = "获取所有结算账户")
    public ResponseEntity<List<SettlementAccountDTO>> getAllSettlementAccounts() {
        final List<SettlementAccountDTO> accounts = financeAccountService.getAllSettlementAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * 根据ID获取结算账户
     *
     * @param id 结算账户ID
     * @return 结算账户信息
     */
    @GetMapping("/settlement-accounts/{id}")
    @Operation(summary = "根据ID获取结算账户")
    public ResponseEntity<SettlementAccountDTO> getSettlementAccountById(@PathVariable Long id) {
        final SettlementAccountDTO account = financeAccountService.getSettlementAccountById(id);
        return ResponseEntity.ok(account);
    }

    /**
     * 根据账户编号获取结算账户
     *
     * @param accountNumber 账户编号
     * @return 结算账户信息
     */
    @GetMapping("/settlement-accounts/number/{accountNumber}")
    @Operation(summary = "根据账户编号获取结算账户")
    public ResponseEntity<SettlementAccountDTO> getSettlementAccountByAccountNumber(
            @PathVariable String accountNumber) {
        final SettlementAccountDTO account =
                financeAccountService.getSettlementAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    /**
     * 根据账户类型获取结算账户
     *
     * @param accountType 账户类型
     * @return 结算账户列表
     */
    @GetMapping("/settlement-accounts/type/{accountType}")
    @Operation(summary = "根据账户类型获取结算账户")
    public ResponseEntity<List<SettlementAccountDTO>> getSettlementAccountsByAccountType(
            @PathVariable String accountType) {
        final List<SettlementAccountDTO> accounts =
                financeAccountService.getSettlementAccountsByAccountType(accountType);
        return ResponseEntity.ok(accounts);
    }

    /**
     * 创建结算账户
     *
     * @param accountDTO 结算账户数据
     * @return 创建的结算账户信息
     */
    @PostMapping("/settlement-accounts")
    @Operation(summary = "创建结算账户")
    public ResponseEntity<SettlementAccountDTO> createSettlementAccount(@Valid @RequestBody SettlementAccountDTO accountDTO) {
        final SettlementAccountDTO createdAccount = financeAccountService.createSettlementAccount(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    /**
     * 更新结算账户
     *
     * @param id 结算账户ID
     * @param accountDTO 更新后的账户数据
     * @return 更新后的结算账户信息
     */
    @PutMapping("/settlement-accounts/{id}")
    @Operation(summary = "更新结算账户")
    public ResponseEntity<SettlementAccountDTO> updateSettlementAccount(
            @PathVariable Long id,
            @Valid @RequestBody SettlementAccountDTO accountDTO) {
        final SettlementAccountDTO updatedAccount = financeAccountService.updateSettlementAccount(id, accountDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * 删除结算账户
     *
     * @param id 结算账户ID
     * @return 无内容响应
     */
    @DeleteMapping("/settlement-accounts/{id}")
    @Operation(summary = "删除结算账户")
    public ResponseEntity<Void> deleteSettlementAccount(@PathVariable Long id) {
        financeAccountService.deleteSettlementAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 调整结算账户余额
     *
     * @param id 结算账户ID
     * @param amount 调整金额
     * @return 更新后的结算账户信息
     */
    @PatchMapping("/settlement-accounts/{id}/balance")
    @Operation(summary = "调整结算账户余额")
    public ResponseEntity<SettlementAccountDTO> adjustAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        final SettlementAccountDTO updatedAccount = financeAccountService.adjustAccountBalance(id, amount);
        return ResponseEntity.ok(updatedAccount);
    }
}
