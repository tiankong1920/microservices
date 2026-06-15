package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.exception.ResourceNotFoundException;
import com.inventory.financeservice.service.IIncomeExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/finance/expenses")
@Tag(name = "Expense", description = "支出管理接口")
@RequiredArgsConstructor
@Validated
public class ExpenseController {
    private final IIncomeExpenseService incomeExpenseService;

    /**
     * 创建支出记录
     *
     * @param expenseDTO 支出数据
     * @return 创建的支出信息
     */
    @PostMapping
    @Operation(summary = "创建支出记录")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO createdExpense = incomeExpenseService.createExpense(expenseDTO);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取支出记录
     *
     * @param id 支出ID
     * @return 支出信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取支出记录")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        ExpenseDTO expenseDTO = incomeExpenseService.getExpenseById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return ResponseEntity.ok(expenseDTO);
    }

    /**
     * 获取所有支出记录
     *
     * @return 支出列表
     */
    @GetMapping
    @Operation(summary = "获取所有支出记录")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        List<ExpenseDTO> expenseDTOs = incomeExpenseService.getAllExpenses();
        return ResponseEntity.ok(expenseDTOs);
    }

    /**
     * 更新支出记录
     *
     * @param id 支出ID
     * @param expenseDTO 更新后的支出数据
     * @return 更新后的支出信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新支出记录")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO updatedExpense = incomeExpenseService.updateExpense(id, expenseDTO);
        return ResponseEntity.ok(updatedExpense);
    }

    /**
     * 删除支出记录
     *
     * @param id 支出ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除支出记录")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        incomeExpenseService.cancelExpense(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据日期范围获取支出记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出列表
     */
    @GetMapping("/by-date-range")
    @Operation(summary = "根据日期范围获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ExpenseDTO> expenseDTOs = incomeExpenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenseDTOs);
    }

    /**
     * 根据类型获取支出记录
     *
     * @param expenseType 支出类型
     * @return 支出列表
     */
    @GetMapping("/by-type")
    @Operation(summary = "根据类型获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByType(@RequestParam String expenseType) {
        List<ExpenseDTO> expenseDTOs = incomeExpenseService.getExpensesByType(expenseType);
        return ResponseEntity.ok(expenseDTOs);
    }

    /**
     * 根据状态获取支出记录
     *
     * @param expenseStatus 支出状态
     * @return 支出列表
     */
    @GetMapping("/by-status")
    @Operation(summary = "根据状态获取支出记录")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByStatus(@RequestParam String expenseStatus) {
        List<ExpenseDTO> expenseDTOs = incomeExpenseService.getExpensesByStatus(expenseStatus);
        return ResponseEntity.ok(expenseDTOs);
    }

    /**
     * 搜索支出记录
     *
     * @param keyword 搜索关键词
     * @return 支出列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索支出记录")
    public ResponseEntity<List<ExpenseDTO>> searchExpenses(@RequestParam String keyword) {
        List<ExpenseDTO> results = incomeExpenseService.searchExpenses(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取待审批的支出记录
     *
     * @return 待审批的支出列表
     */
    @GetMapping("/pending-approval")
    @Operation(summary = "获取待审批的支出记录")
    public ResponseEntity<List<ExpenseDTO>> getPendingApprovalExpenses() {
        List<ExpenseDTO> pendingExpenses = incomeExpenseService.getPendingApprovalExpenses();
        return ResponseEntity.ok(pendingExpenses);
    }

    /**
     * 获取指定日期范围内的支出总额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出总额
     */
    @GetMapping("/total")
    @Operation(summary = "获取指定日期范围内的支出总额")
    public ResponseEntity<BigDecimal> getTotalExpenseByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal total = incomeExpenseService.getTotalExpenseByDateRange(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    /**
     * 获取指定日期范围内的净利润
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 净利润
     */
    @GetMapping("/net-profit")
    @Operation(summary = "获取指定日期范围内的净利润")
    public ResponseEntity<BigDecimal> getNetProfitByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal netProfit = incomeExpenseService.getNetProfitByDateRange(startDate, endDate);
        return ResponseEntity.ok(netProfit);
    }

    /**
     * 导出支出记录到Excel
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel 文件字节数组
     */
    @GetMapping("/export/excel")
    @Operation(summary = "导出支出记录到Excel")
    public ResponseEntity<byte[]> exportExpensesToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        byte[] data = incomeExpenseService.exportExpensesToExcel(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "expenses.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
