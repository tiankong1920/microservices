package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.TaxRecordDTO;
import com.inventory.financeservice.exception.ResourceNotFoundException;
import com.inventory.financeservice.service.ITaxCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/finance/taxes")
@Tag(name = "Tax", description = "税务管理接口")
@RequiredArgsConstructor
@Validated
public class TaxController {
    private final ITaxCalculationService taxCalculationService;

    /**
     * 计算增值税
     *
     * @param taxableAmount 应税金额
     * @param inputTax 进项税
     * @param outputTax 销项税
     * @param periodStart 期间开始
     * @param periodEnd 期间结束
     * @return 税务记录
     */
    @PostMapping("/calculate/vat")
    @Operation(summary = "计算增值税")
    public ResponseEntity<TaxRecordDTO> calculateVat(
            @RequestParam BigDecimal taxableAmount,
            @RequestParam BigDecimal inputTax,
            @RequestParam BigDecimal outputTax,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd) {
        TaxRecordDTO taxRecord = taxCalculationService.calculateVat(
                taxableAmount, inputTax, outputTax, periodStart, periodEnd);
        return new ResponseEntity<>(taxRecord, HttpStatus.CREATED);
    }

    /**
     * 计算企业所得税
     *
     * @param revenue 收入
     * @param deductibleExpenses 可扣除费用
     * @param periodStart 期间开始
     * @param periodEnd 期间结束
     * @return 税务记录
     */
    @PostMapping("/calculate/corporate-income")
    @Operation(summary = "计算企业所得税")
    public ResponseEntity<TaxRecordDTO> calculateCorporateIncomeTax(
            @RequestParam BigDecimal revenue,
            @RequestParam BigDecimal deductibleExpenses,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd) {
        TaxRecordDTO taxRecord = taxCalculationService.calculateCorporateIncomeTax(
                revenue, deductibleExpenses, periodStart, periodEnd);
        return new ResponseEntity<>(taxRecord, HttpStatus.CREATED);
    }

    /**
     * 计算个人所得税
     *
     * @param incomeAmount 收入金额
     * @param incomeType 收入类型
     * @param periodStart 期间开始
     * @param periodEnd 期间结束
     * @return 税务记录
     */
    @PostMapping("/calculate/personal-income")
    @Operation(summary = "计算个人所得税")
    public ResponseEntity<TaxRecordDTO> calculatePersonalIncomeTax(
            @RequestParam BigDecimal incomeAmount,
            @RequestParam String incomeType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd) {
        TaxRecordDTO taxRecord = taxCalculationService.calculatePersonalIncomeTax(
                incomeAmount, incomeType, periodStart, periodEnd);
        return new ResponseEntity<>(taxRecord, HttpStatus.CREATED);
    }

    /**
     * 创建税务记录
     *
     * @param taxRecordDTO 税务记录数据
     * @return 创建的税务记录
     */
    @PostMapping
    @Operation(summary = "创建税务记录")
    public ResponseEntity<TaxRecordDTO> createTaxRecord(@Valid @RequestBody TaxRecordDTO taxRecordDTO) {
        TaxRecordDTO createdRecord = taxCalculationService.createTaxRecord(taxRecordDTO);
        return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取税务记录
     *
     * @param id 税务记录ID
     * @return 税务记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取税务记录")
    public ResponseEntity<TaxRecordDTO> getTaxRecordById(@PathVariable Long id) {
        TaxRecordDTO taxRecord = taxCalculationService.getTaxRecordById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxRecord", "id", id));
        return ResponseEntity.ok(taxRecord);
    }

    /**
     * 获取所有税务记录
     *
     * @return 税务记录列表
     */
    @GetMapping
    @Operation(summary = "获取所有税务记录")
    public ResponseEntity<List<TaxRecordDTO>> getAllTaxRecords() {
        List<TaxRecordDTO> taxRecords = taxCalculationService.getAllTaxRecords();
        return ResponseEntity.ok(taxRecords);
    }

    /**
     * 根据税种获取税务记录
     *
     * @param taxType 税种
     * @return 税务记录列表
     */
    @GetMapping("/by-type")
    @Operation(summary = "根据税种获取税务记录")
    public ResponseEntity<List<TaxRecordDTO>> getTaxRecordsByType(@RequestParam String taxType) {
        List<TaxRecordDTO> taxRecords = taxCalculationService.getTaxRecordsByType(taxType);
        return ResponseEntity.ok(taxRecords);
    }

    /**
     * 根据状态获取税务记录
     *
     * @param status 状态
     * @return 税务记录列表
     */
    @GetMapping("/by-status")
    @Operation(summary = "根据状态获取税务记录")
    public ResponseEntity<List<TaxRecordDTO>> getTaxRecordsByStatus(@RequestParam String status) {
        List<TaxRecordDTO> taxRecords = taxCalculationService.getTaxRecordsByStatus(status);
        return ResponseEntity.ok(taxRecords);
    }

    /**
     * 根据期间获取税务记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 税务记录列表
     */
    @GetMapping("/by-period")
    @Operation(summary = "根据期间获取税务记录")
    public ResponseEntity<List<TaxRecordDTO>> getTaxRecordsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TaxRecordDTO> taxRecords = taxCalculationService.getTaxRecordsByPeriod(startDate, endDate);
        return ResponseEntity.ok(taxRecords);
    }

    /**
     * 获取逾期未缴税款
     *
     * @return 逾期税务记录列表
     */
    @GetMapping("/overdue")
    @Operation(summary = "获取逾期未缴税款")
    public ResponseEntity<List<TaxRecordDTO>> getOverdueTaxes() {
        List<TaxRecordDTO> overdueTaxes = taxCalculationService.getOverdueTaxes();
        return ResponseEntity.ok(overdueTaxes);
    }

    /**
     * 获取税务总负债
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 税务总负债
     */
    @GetMapping("/total-liability")
    @Operation(summary = "获取税务总负债")
    public ResponseEntity<BigDecimal> getTotalTaxLiability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal totalLiability = taxCalculationService.getTotalTaxLiability(startDate, endDate);
        return ResponseEntity.ok(totalLiability);
    }

    /**
     * 获取进项税总额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 进项税总额
     */
    @GetMapping("/total-input-tax")
    @Operation(summary = "获取进项税总额")
    public ResponseEntity<BigDecimal> getTotalInputTax(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal totalInputTax = taxCalculationService.getTotalInputTax(startDate, endDate);
        return ResponseEntity.ok(totalInputTax);
    }

    /**
     * 获取销项税总额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销项税总额
     */
    @GetMapping("/total-output-tax")
    @Operation(summary = "获取销项税总额")
    public ResponseEntity<BigDecimal> getTotalOutputTax(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal totalOutputTax = taxCalculationService.getTotalOutputTax(startDate, endDate);
        return ResponseEntity.ok(totalOutputTax);
    }

    /**
     * 分析税负
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 税负分析结果
     */
    @GetMapping("/analysis")
    @Operation(summary = "分析税负")
    public ResponseEntity<Map<String, Object>> analyzeTaxBurden(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        ITaxCalculationService.TaxAnalysisResult result = taxCalculationService.analyzeTaxBurden(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "totalTaxLiability", result.getTotalTaxLiability(),
                "totalInputTax", result.getTotalInputTax(),
                "totalOutputTax", result.getTotalOutputTax(),
                "netTaxPayable", result.getNetTaxPayable(),
                "taxBurdenRate", result.getTaxBurdenRate(),
                "analysisPeriod", result.getAnalysisPeriod(),
                "optimizationSuggestions", result.getOptimizationSuggestions()
        ));
    }

    /**
     * 申报税金
     *
     * @param id 税务记录ID
     * @param declarerId 申报人ID
     * @return 申报后的税务记录
     */
    @PostMapping("/{id}/declare")
    @Operation(summary = "申报税金")
    public ResponseEntity<TaxRecordDTO> declareTax(
            @PathVariable Long id,
            @RequestParam String declarerId) {
        TaxRecordDTO declaredRecord = taxCalculationService.declareTax(id, declarerId);
        return ResponseEntity.ok(declaredRecord);
    }

    /**
     * 缴纳税金
     *
     * @param id 税务记录ID
     * @param payerId 付款人ID
     * @return 缴税后的税务记录
     */
    @PostMapping("/{id}/pay")
    @Operation(summary = "缴纳税金")
    public ResponseEntity<TaxRecordDTO> payTax(
            @PathVariable Long id,
            @RequestParam String payerId) {
        TaxRecordDTO paidRecord = taxCalculationService.payTax(id, payerId);
        return ResponseEntity.ok(paidRecord);
    }

    /**
     * 调整税记录
     *
     * @param id 税务记录ID
     * @param adjustments 调整数据
     * @return 调整后的税务记录
     */
    @PutMapping("/{id}/adjust")
    @Operation(summary = "调整税记录")
    public ResponseEntity<TaxRecordDTO> adjustTax(
            @PathVariable Long id,
            @Valid @RequestBody TaxRecordDTO adjustments) {
        TaxRecordDTO adjustedRecord = taxCalculationService.adjustTax(id, adjustments);
        return ResponseEntity.ok(adjustedRecord);
    }
}
