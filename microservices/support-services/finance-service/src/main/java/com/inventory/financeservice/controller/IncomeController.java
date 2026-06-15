package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.exception.ResourceNotFoundException;
import com.inventory.financeservice.service.IIncomeService;
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
@RequestMapping("/api/v1/finance/incomes")
@Tag(name = "Income", description = "收入管理接口")
@RequiredArgsConstructor
@Validated
public class IncomeController {
    private final IIncomeService incomeService;

    /**
     * 创建收入记录
     *
     * @param incomeDTO 收入数据
     * @return 创建的收入信息
     */
    @PostMapping
    @Operation(summary = "创建收入记录")
    public ResponseEntity<IncomeDTO> createIncome(
            @Valid @RequestBody IncomeDTO incomeDTO) {
        IncomeDTO createdIncome = incomeService.createIncome(incomeDTO);
        return new ResponseEntity<>(createdIncome, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取收入记录
     *
     * @param id 收入ID
     * @return 收入信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取收入记录")
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable Long id) {
        IncomeDTO incomeDTO = incomeService.findIncomeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", id));
        return ResponseEntity.ok(incomeDTO);
    }

    /**
     * 获取所有收入记录
     *
     * @return 收入列表
     */
    @GetMapping
    @Operation(summary = "获取所有收入记录")
    public ResponseEntity<List<IncomeDTO>> getAllIncomes() {
        List<IncomeDTO> incomeDTOs = incomeService.getAllIncomes();
        return ResponseEntity.ok(incomeDTOs);
    }

    /**
     * 更新收入记录
     *
     * @param id 收入ID
     * @param incomeDTO 更新后的收入数据
     * @return 更新后的收入信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新收入记录")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeDTO incomeDTO) {
        IncomeDTO updatedIncome = incomeService.updateIncome(id, incomeDTO);
        return ResponseEntity.ok(updatedIncome);
    }

    /**
     * 删除收入记录
     *
     * @param id 收入ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收入记录")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据日期范围获取收入记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入列表
     */
    @GetMapping("/by-date-range")
    @Operation(summary = "根据日期范围获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<IncomeDTO> incomeDTOs = incomeService.getIncomesByDateRange(startDate, endDate);
        return ResponseEntity.ok(incomeDTOs);
    }

    /**
     * 根据类型获取收入记录
     *
     * @param incomeType 收入类型
     * @return 收入列表
     */
    @GetMapping("/by-type")
    @Operation(summary = "根据类型获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesByType(@RequestParam String incomeType) {
        List<IncomeDTO> incomeDTOs = incomeService.getIncomesByType(incomeType);
        return ResponseEntity.ok(incomeDTOs);
    }

    /**
     * 根据状态获取收入记录
     *
     * @param incomeStatus 收入状态
     * @return 收入列表
     */
    @GetMapping("/by-status")
    @Operation(summary = "根据状态获取收入记录")
    public ResponseEntity<List<IncomeDTO>> getIncomesByStatus(@RequestParam String incomeStatus) {
        List<IncomeDTO> incomeDTOs = incomeService.getIncomesByStatus(incomeStatus);
        return ResponseEntity.ok(incomeDTOs);
    }

    /**
     * 搜索收入记录
     *
     * @param keyword 搜索关键词
     * @return 收入列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索收入记录")
    public ResponseEntity<List<IncomeDTO>> searchIncomes(@RequestParam String keyword) {
        List<IncomeDTO> results = incomeService.searchIncomes(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取待审批的收入记录
     *
     * @return 待审批的收入列表
     */
    @GetMapping("/pending-approval")
    @Operation(summary = "获取待审批的收入记录")
    public ResponseEntity<List<IncomeDTO>> getPendingApprovalIncomes() {
        List<IncomeDTO> pendingIncomes = incomeService.getPendingApprovalIncomes();
        return ResponseEntity.ok(pendingIncomes);
    }

    /**
     * 获取指定日期范围内的收入总额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入总额
     */
    @GetMapping("/total")
    @Operation(summary = "获取指定日期范围内的收入总额")
    public ResponseEntity<BigDecimal> getTotalIncomeByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal total = incomeService.getTotalIncomeByDateRange(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    /**
     * 导出收入记录到Excel
     *
     * @return Excel 文件字节数组
     */
    @GetMapping("/export/excel")
    @Operation(summary = "导出收入记录到Excel")
    public ResponseEntity<byte[]> exportIncomesToExcel() {
        byte[] data = incomeService.exportIncomesToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "incomes.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * 导出收入记录到CSV
     *
     * @return CSV 文件字节数组
     */
    @GetMapping("/export/csv")
    @Operation(summary = "导出收入记录到CSV")
    public ResponseEntity<byte[]> exportIncomesToCsv() {
        byte[] data = incomeService.exportIncomesToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "incomes.csv");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
