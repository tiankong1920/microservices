package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.FinancialReportDTO;
import com.inventory.financeservice.exception.ResourceNotFoundException;
import com.inventory.financeservice.service.IFinancialReportService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/finance/reports")
@Tag(name = "Financial Report", description = "财务报表接口")
@RequiredArgsConstructor
@Validated
public class FinancialReportController {
    private final IFinancialReportService financialReportService;

    /**
     * 生成资产负债表
     *
     * @param fiscalYear 财年
     * @param periodType 期间类型
     * @param generatedBy 生成人
     * @return 生成的资产负债表
     */
    @PostMapping("/generate/balance-sheet")
    @Operation(summary = "生成资产负债表")
    public ResponseEntity<FinancialReportDTO> generateBalanceSheet(
            @RequestParam Integer fiscalYear,
            @RequestParam String periodType,
            @RequestParam String generatedBy) {
        FinancialReportDTO report = financialReportService.generateBalanceSheet(
                fiscalYear, periodType, generatedBy);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    /**
     * 生成利润表
     *
     * @param fiscalYear 财年
     * @param periodType 期间类型
     * @param generatedBy 生成人
     * @return 生成的利润表
     */
    @PostMapping("/generate/income-statement")
    @Operation(summary = "生成利润表")
    public ResponseEntity<FinancialReportDTO> generateIncomeStatement(
            @RequestParam Integer fiscalYear,
            @RequestParam String periodType,
            @RequestParam String generatedBy) {
        FinancialReportDTO report = financialReportService.generateIncomeStatement(
                fiscalYear, periodType, generatedBy);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    /**
     * 生成现金流量表
     *
     * @param fiscalYear 财年
     * @param periodType 期间类型
     * @param generatedBy 生成人
     * @return 生成的现金流量表
     */
    @PostMapping("/generate/cash-flow")
    @Operation(summary = "生成现金流量表")
    public ResponseEntity<FinancialReportDTO> generateCashFlowStatement(
            @RequestParam Integer fiscalYear,
            @RequestParam String periodType,
            @RequestParam String generatedBy) {
        FinancialReportDTO report = financialReportService.generateCashFlowStatement(
                fiscalYear, periodType, generatedBy);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    /**
     * 生成自定义报表
     *
     * @param reportName 报表名称
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param generatedBy 生成人
     * @return 生成的自定义报表
     */
    @PostMapping("/generate/custom")
    @Operation(summary = "生成自定义报表")
    public ResponseEntity<FinancialReportDTO> generateCustomReport(
            @RequestParam String reportName,
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String generatedBy) {
        FinancialReportDTO report = financialReportService.generateCustomReport(
                reportName, reportType, startDate, endDate, generatedBy);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    /**
     * 保存报表
     *
     * @param reportDTO 报表数据
     * @return 保存的报表信息
     */
    @PostMapping
    @Operation(summary = "保存报表")
    public ResponseEntity<FinancialReportDTO> saveReport(@Valid @RequestBody FinancialReportDTO reportDTO) {
        FinancialReportDTO savedReport = financialReportService.saveReport(reportDTO);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取报表
     *
     * @param id 报表ID
     * @return 报表信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取报表")
    public ResponseEntity<FinancialReportDTO> getReportById(@PathVariable Long id) {
        FinancialReportDTO report = financialReportService.getReportById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialReport", "id", id));
        return ResponseEntity.ok(report);
    }

    /**
     * 获取所有报表
     *
     * @return 报表列表
     */
    @GetMapping
    @Operation(summary = "获取所有报表")
    public ResponseEntity<List<FinancialReportDTO>> getAllReports() {
        List<FinancialReportDTO> reports = financialReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * 根据类型获取报表
     *
     * @param reportType 报表类型
     * @return 报表列表
     */
    @GetMapping("/by-type")
    @Operation(summary = "根据类型获取报表")
    public ResponseEntity<List<FinancialReportDTO>> getReportsByType(@RequestParam String reportType) {
        List<FinancialReportDTO> reports = financialReportService.getReportsByType(reportType);
        return ResponseEntity.ok(reports);
    }

    /**
     * 根据状态获取报表
     *
     * @param status 报表状态
     * @return 报表列表
     */
    @GetMapping("/by-status")
    @Operation(summary = "根据状态获取报表")
    public ResponseEntity<List<FinancialReportDTO>> getReportsByStatus(@RequestParam String status) {
        List<FinancialReportDTO> reports = financialReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * 根据期间获取报表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表列表
     */
    @GetMapping("/by-period")
    @Operation(summary = "根据期间获取报表")
    public ResponseEntity<List<FinancialReportDTO>> getReportsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FinancialReportDTO> reports = financialReportService.getReportsByPeriod(startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    /**
     * 根据年份获取报表
     *
     * @param fiscalYear 财年
     * @return 报表列表
     */
    @GetMapping("/by-year")
    @Operation(summary = "根据年份获取报表")
    public ResponseEntity<List<FinancialReportDTO>> getReportsByYear(@RequestParam Integer fiscalYear) {
        List<FinancialReportDTO> reports = financialReportService.getReportsByYear(fiscalYear);
        return ResponseEntity.ok(reports);
    }

    /**
     * 审批报表
     *
     * @param id 报表ID
     * @param approverId 审批人ID
     * @return 审批后的报表信息
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批报表")
    public ResponseEntity<FinancialReportDTO> approveReport(
            @PathVariable Long id,
            @RequestParam String approverId) {
        FinancialReportDTO approvedReport = financialReportService.approveReport(id, approverId);
        return ResponseEntity.ok(approvedReport);
    }

    /**
     * 发布报表
     *
     * @param id 报表ID
     * @return 发布后的报表信息
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布报表")
    public ResponseEntity<FinancialReportDTO> publishReport(@PathVariable Long id) {
        FinancialReportDTO publishedReport = financialReportService.publishReport(id);
        return ResponseEntity.ok(publishedReport);
    }

    /**
     * 删除报表
     *
     * @param id 报表ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除报表")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        financialReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 导出报表到Excel
     *
     * @param id 报表ID
     * @return Excel 文件字节数组
     */
    @GetMapping("/{id}/export/excel")
    @Operation(summary = "导出报表到Excel")
    public ResponseEntity<byte[]> exportToExcel(@PathVariable Long id) {
        byte[] data = financialReportService.exportToExcel(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "financial_report.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * 导出报表到PDF
     *
     * @param id 报表ID
     * @return PDF 文件字节数组
     */
    @GetMapping("/{id}/export/pdf")
    @Operation(summary = "导出报表到PDF")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable Long id) {
        byte[] data = financialReportService.exportToPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "financial_report.pdf");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * 导出报表到CSV
     *
     * @param id 报表ID
     * @return CSV 文件字节数组
     */
    @GetMapping("/{id}/export/csv")
    @Operation(summary = "导出报表到CSV")
    public ResponseEntity<byte[]> exportToCsv(@PathVariable Long id) {
        byte[] data = financialReportService.exportToCsv(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "financial_report.csv");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * 获取仪表板摘要
     *
     * @return 仪表板摘要数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表板摘要")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        IFinancialReportService.ReportSummary summary = financialReportService.getDashboardSummary();
        Map<String, Object> result = new HashMap<>();
        result.put("currentYear", summary.getCurrentYear());
        result.put("totalRevenue", summary.getTotalRevenue());
        result.put("totalExpense", summary.getTotalExpense());
        result.put("netProfit", summary.getNetProfit());
        result.put("profitMargin", summary.getProfitMargin());
        result.put("totalAssets", summary.getTotalAssets());
        result.put("totalLiabilities", summary.getTotalLiabilities());
        result.put("totalEquity", summary.getTotalEquity());
        result.put("cashBalance", summary.getCashBalance());
        result.put("budgetUtilizationRate", summary.getBudgetUtilizationRate());
        result.put("yearOverYearGrowth", summary.getYearOverYearGrowth());
        result.put("monthlyTrends", summary.getMonthlyTrends());
        result.put("expenseBreakdown", summary.getExpenseBreakdown());
        return ResponseEntity.ok(result);
    }
}
