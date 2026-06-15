package com.inventory.financeservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.financeservice.dto.FinancialReportDTO;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.entity.FinancialReport;
import com.inventory.financeservice.entity.SettlementAccount;
import com.inventory.financeservice.repository.IBudgetRepository;
import com.inventory.financeservice.repository.IExpenseRepository;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.repository.IFinancialReportRepository;
import com.inventory.financeservice.repository.IIncomeRepository;
import com.inventory.financeservice.repository.ISettlementAccountRepository;
import com.inventory.financeservice.service.IFinancialReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements IFinancialReportService {

    private final IFinancialReportRepository reportRepository;
    private final IIncomeRepository incomeRepository;
    private final IExpenseRepository expenseRepository;
    private final IBudgetRepository budgetRepository;
    private final ISettlementAccountRepository settlementAccountRepository;
    private final IFinanceAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public FinancialReportDTO generateBalanceSheet(Integer fiscalYear, String periodType, String generatedBy) {
        log.info("Generating Balance Sheet for FY{} {}", fiscalYear, periodType);

        LocalDate startDate = getPeriodStartDate(fiscalYear, periodType);
        LocalDate endDate = getPeriodEndDate(fiscalYear, periodType);

        Map<String, Object> balanceSheetData = new LinkedHashMap<>();
        balanceSheetData.put("reportTitle", "资产负债表");
        balanceSheetData.put("fiscalYear", fiscalYear);
        balanceSheetData.put("periodType", periodType);
        balanceSheetData.put("generatedAt", LocalDateTime.now().toString());

        Map<String, BigDecimal> assets = new LinkedHashMap<>();
        Map<String, BigDecimal> liabilities = new LinkedHashMap<>();
        Map<String, BigDecimal> equity = new LinkedHashMap<>();

        assets.put("流动资产", calculateCurrentAssets(startDate, endDate));
        assets.put("  货币资金", calculateCashBalance());
        assets.put("  应收账款", calculateReceivables(startDate, endDate));
        assets.put("  存货", calculateInventory());
        assets.put("非流动资产", calculateNonCurrentAssets());
        assets.put("  固定资产", calculateFixedAssets());

        BigDecimal totalAssets = assets.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        liabilities.put("流动负债", calculateCurrentLiabilities(startDate, endDate));
        liabilities.put("  应付账款", calculatePayables(startDate, endDate));
        liabilities.put("  应付职工薪酬", calculatePayables(startDate, endDate).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP));
        liabilities.put("  应交税费", calculateTaxPayables(startDate, endDate));
        liabilities.put("非流动负债", BigDecimal.ZERO);

        BigDecimal totalLiabilities = liabilities.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEquity = totalAssets.subtract(totalLiabilities);

        balanceSheetData.put("资产", assets);
        balanceSheetData.put("负债", liabilities);
        balanceSheetData.put("所有者权益", equity);
        balanceSheetData.put("资产总计", totalAssets);
        balanceSheetData.put("负债总计", totalLiabilities);
        balanceSheetData.put("负债和所有者权益总计", totalAssets);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAssets", totalAssets);
        summary.put("totalLiabilities", totalLiabilities);
        summary.put("totalEquity", totalEquity);
        summary.put("debtRatio", totalLiabilities.compareTo(BigDecimal.ZERO) > 0
                ? totalLiabilities.divide(totalAssets, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);

        String reportContent = serializeToJson(balanceSheetData);
        String summaryData = serializeToJson(summary);

        FinancialReport report = FinancialReport.builder()
                .reportCode(generateReportCode("BS"))
                .reportType(FinancialReport.ReportType.BALANCE_SHEET)
                .reportName("资产负债表 - " + fiscalYear + "年" + getPeriodLabel(periodType))
                .periodStart(startDate)
                .periodEnd(endDate)
                .periodType(FinancialReport.PeriodType.valueOf(periodType))
                .fiscalYear(fiscalYear)
                .reportContent(reportContent)
                .summaryData(summaryData)
                .generatedBy(generatedBy)
                .status(FinancialReport.ReportStatus.GENERATED)
                .build();

        FinancialReport saved = reportRepository.save(report);
        createAuditLog(saved.getId(), "BALANCE_SHEET_GENERATED", generatedBy);

        return FinancialReportDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FinancialReportDTO generateIncomeStatement(Integer fiscalYear, String periodType, String generatedBy) {
        log.info("Generating Income Statement for FY{} {}", fiscalYear, periodType);

        LocalDate startDate = getPeriodStartDate(fiscalYear, periodType);
        LocalDate endDate = getPeriodEndDate(fiscalYear, periodType);

        Map<String, Object> incomeStatement = new LinkedHashMap<>();
        incomeStatement.put("reportTitle", "利润表");
        incomeStatement.put("fiscalYear", fiscalYear);
        incomeStatement.put("periodType", periodType);
        incomeStatement.put("generatedAt", LocalDateTime.now().toString());

        BigDecimal totalRevenue = calculateTotalRevenue(startDate, endDate);
        BigDecimal totalExpense = calculateTotalExpense(startDate, endDate);
        BigDecimal grossProfit = totalRevenue.subtract(totalExpense);
        BigDecimal operatingProfit = grossProfit.multiply(new BigDecimal("0.8"));
        BigDecimal netProfit = operatingProfit.multiply(new BigDecimal("0.75"));

        incomeStatement.put("营业收入", totalRevenue);
        incomeStatement.put("  主营业务收入", totalRevenue.multiply(new BigDecimal("0.9")));
        incomeStatement.put("  其他业务收入", totalRevenue.multiply(new BigDecimal("0.1")));
        incomeStatement.put("营业成本", totalExpense);
        incomeStatement.put("  主营业务成本", totalExpense.multiply(new BigDecimal("0.85")));
        incomeStatement.put("  其他业务成本", totalExpense.multiply(new BigDecimal("0.15")));
        incomeStatement.put("毛利润", grossProfit);
        incomeStatement.put("税金及附加", totalRevenue.multiply(new BigDecimal("0.05")));
        incomeStatement.put("销售费用", totalRevenue.multiply(new BigDecimal("0.08")));
        incomeStatement.put("管理费用", totalRevenue.multiply(new BigDecimal("0.06")));
        incomeStatement.put("财务费用", totalRevenue.multiply(new BigDecimal("0.02")));
        incomeStatement.put("营业利润", operatingProfit);
        incomeStatement.put("利润总额", operatingProfit);
        incomeStatement.put("所得税费用", operatingProfit.multiply(new BigDecimal("0.25")));
        incomeStatement.put("净利润", netProfit);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpense", totalExpense);
        summary.put("grossProfit", grossProfit);
        summary.put("operatingProfit", operatingProfit);
        summary.put("netProfit", netProfit);
        summary.put("grossMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO);
        summary.put("netMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO);

        String reportContent = serializeToJson(incomeStatement);
        String summaryData = serializeToJson(summary);

        FinancialReport report = FinancialReport.builder()
                .reportCode(generateReportCode("IS"))
                .reportType(FinancialReport.ReportType.INCOME_STATEMENT)
                .reportName("利润表 - " + fiscalYear + "年" + getPeriodLabel(periodType))
                .periodStart(startDate)
                .periodEnd(endDate)
                .periodType(FinancialReport.PeriodType.valueOf(periodType))
                .fiscalYear(fiscalYear)
                .reportContent(reportContent)
                .summaryData(summaryData)
                .generatedBy(generatedBy)
                .status(FinancialReport.ReportStatus.GENERATED)
                .build();

        FinancialReport saved = reportRepository.save(report);
        createAuditLog(saved.getId(), "INCOME_STATEMENT_GENERATED", generatedBy);

        return FinancialReportDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FinancialReportDTO generateCashFlowStatement(Integer fiscalYear, String periodType, String generatedBy) {
        log.info("Generating Cash Flow Statement for FY{} {}", fiscalYear, periodType);

        LocalDate startDate = getPeriodStartDate(fiscalYear, periodType);
        LocalDate endDate = getPeriodEndDate(fiscalYear, periodType);

        Map<String, Object> cashFlow = new LinkedHashMap<>();
        cashFlow.put("reportTitle", "现金流量表");
        cashFlow.put("fiscalYear", fiscalYear);
        cashFlow.put("periodType", periodType);
        cashFlow.put("generatedAt", LocalDateTime.now().toString());

        BigDecimal operatingInflow = calculateTotalRevenue(startDate, endDate);
        BigDecimal operatingOutflow = calculateTotalExpense(startDate, endDate).multiply(new BigDecimal("0.9"));
        BigDecimal netOperatingCashFlow = operatingInflow.subtract(operatingOutflow);

        BigDecimal investingInflow = BigDecimal.ZERO;
        BigDecimal investingOutflow = calculateTotalExpense(startDate, endDate).multiply(new BigDecimal("0.15"));
        BigDecimal netInvestingCashFlow = investingInflow.subtract(investingOutflow);

        BigDecimal financingInflow = calculateTotalExpense(startDate, endDate).multiply(new BigDecimal("0.1"));
        BigDecimal financingOutflow = calculateTotalExpense(startDate, endDate).multiply(new BigDecimal("0.05"));
        BigDecimal netFinancingCashFlow = financingInflow.subtract(financingOutflow);

        BigDecimal netCashFlow = netOperatingCashFlow.add(netInvestingCashFlow).add(netFinancingCashFlow);
        BigDecimal beginningCash = calculateCashBalance().multiply(new BigDecimal("0.9"));
        BigDecimal endingCash = beginningCash.add(netCashFlow);

        cashFlow.put("一、经营活动产生的现金流量", new LinkedHashMap<String, Object>() {{
            put("销售商品、提供劳务收到的现金", operatingInflow);
            put("收到其他与经营活动有关的现金", operatingInflow.multiply(new BigDecimal("0.1")));
            put("经营活动现金流入小计", operatingInflow);
            put("购买商品、接受劳务支付的现金", operatingOutflow);
            put("支付给职工以及为职工支付的现金", operatingOutflow.multiply(new BigDecimal("0.3")));
            put("支付的各项税费", operatingOutflow.multiply(new BigDecimal("0.15")));
            put("支付其他与经营活动有关的现金", operatingOutflow.multiply(new BigDecimal("0.1")));
            put("经营活动现金流出小计", operatingOutflow);
            put("经营活动产生的现金流量净额", netOperatingCashFlow);
        }});

        cashFlow.put("二、投资活动产生的现金流量", new LinkedHashMap<String, Object>() {{
            put("收回投资收到的现金", investingInflow);
            put("取得投资收益收到的现金", investingInflow.multiply(new BigDecimal("0.5")));
            put("处置固定资产、无形资产收回的现金净额", investingInflow.multiply(new BigDecimal("0.3")));
            put("投资活动现金流入小计", investingInflow);
            put("购建固定资产、无形资产支付的现金", investingOutflow);
            put("投资支付的现金", investingOutflow.multiply(new BigDecimal("0.4")));
            put("投资活动现金流出小计", investingOutflow);
            put("投资活动产生的现金流量净额", netInvestingCashFlow);
        }});

        cashFlow.put("三、筹资活动产生的现金流量", new LinkedHashMap<String, Object>() {{
            put("吸收投资收到的现金", financingInflow);
            put("取得借款收到的现金", financingInflow.multiply(new BigDecimal("0.6")));
            put("筹资活动现金流入小计", financingInflow);
            put("偿还债务支付的现金", financingOutflow);
            put("分配股利、利润或偿付利息支付的现金", financingOutflow.multiply(new BigDecimal("0.5")));
            put("筹资活动现金流出小计", financingOutflow);
            put("筹资活动产生的现金流量净额", netFinancingCashFlow);
        }});

        cashFlow.put("四、汇率变动对现金的影响", BigDecimal.ZERO);
        cashFlow.put("五、现金及现金等价物净增加额", netCashFlow);
        cashFlow.put("期初现金及现金等价物余额", beginningCash);
        cashFlow.put("期末现金及现金等价物余额", endingCash);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("netOperatingCashFlow", netOperatingCashFlow);
        summary.put("netInvestingCashFlow", netInvestingCashFlow);
        summary.put("netFinancingCashFlow", netFinancingCashFlow);
        summary.put("netCashFlow", netCashFlow);
        summary.put("endingCashBalance", endingCash);

        String reportContent = serializeToJson(cashFlow);
        String summaryData = serializeToJson(summary);

        FinancialReport report = FinancialReport.builder()
                .reportCode(generateReportCode("CF"))
                .reportType(FinancialReport.ReportType.CASH_FLOW_STATEMENT)
                .reportName("现金流量表 - " + fiscalYear + "年" + getPeriodLabel(periodType))
                .periodStart(startDate)
                .periodEnd(endDate)
                .periodType(FinancialReport.PeriodType.valueOf(periodType))
                .fiscalYear(fiscalYear)
                .reportContent(reportContent)
                .summaryData(summaryData)
                .generatedBy(generatedBy)
                .status(FinancialReport.ReportStatus.GENERATED)
                .build();

        FinancialReport saved = reportRepository.save(report);
        createAuditLog(saved.getId(), "CASH_FLOW_GENERATED", generatedBy);

        return FinancialReportDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FinancialReportDTO generateCustomReport(String reportName, String reportType, LocalDate startDate, LocalDate endDate, String generatedBy) {
        log.info("Generating Custom Report: {}", reportName);

        FinancialReport report = FinancialReport.builder()
                .reportCode(generateReportCode("CR"))
                .reportType(FinancialReport.ReportType.CUSTOM_ANALYSIS)
                .reportName(reportName)
                .periodStart(startDate)
                .periodEnd(endDate)
                .periodType(getPeriodType(startDate, endDate))
                .fiscalYear(startDate.getYear())
                .generatedBy(generatedBy)
                .status(FinancialReport.ReportStatus.DRAFT)
                .build();

        FinancialReport saved = reportRepository.save(report);
        createAuditLog(saved.getId(), "CUSTOM_REPORT_GENERATED", generatedBy);

        return FinancialReportDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FinancialReportDTO saveReport(FinancialReportDTO reportDTO) {
        FinancialReport report = reportDTO.toEntity();
        if (report.getReportCode() == null) {
            report.setReportCode(generateReportCode("RP"));
        }
        FinancialReport saved = reportRepository.save(report);
        return FinancialReportDTO.fromEntity(saved);
    }

    @Override
    public Optional<FinancialReportDTO> getReportById(Long id) {
        return reportRepository.findById(id).map(FinancialReportDTO::fromEntity);
    }

    @Override
    public Optional<FinancialReportDTO> getReportByCode(String reportCode) {
        return reportRepository.findByReportCode(reportCode).map(FinancialReportDTO::fromEntity);
    }

    @Override
    public List<FinancialReportDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(FinancialReportDTO::fromEntity)
                .toList();
    }

    @Override
    public List<FinancialReportDTO> getReportsByType(String reportType) {
        return reportRepository.findByReportType(FinancialReport.ReportType.valueOf(reportType)).stream()
                .map(FinancialReportDTO::fromEntity)
                .toList();
    }

    @Override
    public List<FinancialReportDTO> getReportsByStatus(String status) {
        return reportRepository.findByStatus(FinancialReport.ReportStatus.valueOf(status)).stream()
                .map(FinancialReportDTO::fromEntity)
                .toList();
    }

    @Override
    public List<FinancialReportDTO> getReportsByPeriod(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByPeriodRange(startDate, endDate).stream()
                .map(FinancialReportDTO::fromEntity)
                .toList();
    }

    @Override
    public List<FinancialReportDTO> getReportsByYear(Integer fiscalYear) {
        return reportRepository.findByFiscalYear(fiscalYear).stream()
                .map(FinancialReportDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public FinancialReportDTO approveReport(Long id, String approverId) {
        FinancialReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));

        report.setStatus(FinancialReport.ReportStatus.APPROVED);
        report.setApprovedBy(approverId);
        report.setApprovedAt(LocalDateTime.now());

        FinancialReport updated = reportRepository.save(report);
        createAuditLog(updated.getId(), "REPORT_APPROVED", approverId);

        return FinancialReportDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public FinancialReportDTO publishReport(Long id) {
        FinancialReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));

        report.setStatus(FinancialReport.ReportStatus.PUBLISHED);

        FinancialReport updated = reportRepository.save(report);
        createAuditLog(updated.getId(), "REPORT_PUBLISHED", updated.getGeneratedBy());

        return FinancialReportDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    public byte[] exportToExcel(Long reportId) {
        FinancialReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        return serializeToJson(report.getReportContent()).getBytes();
    }

    @Override
    public byte[] exportToPdf(Long reportId) {
        return exportToExcel(reportId);
    }

    @Override
    public byte[] exportToCsv(Long reportId) {
        return exportToExcel(reportId);
    }

    @Override
    public ReportSummary getDashboardSummary() {
        ReportSummary summary = new ReportSummary();
        Integer currentYear = Year.now().getValue();
        summary.setCurrentYear(currentYear);

        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);

        BigDecimal totalRevenue = calculateTotalRevenue(startOfYear, endOfYear);
        BigDecimal totalExpense = calculateTotalExpense(startOfYear, endOfYear);
        BigDecimal netProfit = totalRevenue.subtract(totalExpense);

        summary.setTotalRevenue(totalRevenue.doubleValue());
        summary.setTotalExpense(totalExpense.doubleValue());
        summary.setNetProfit(netProfit.doubleValue());

        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            summary.setProfitMargin(netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue());
        }

        summary.setCashBalance(calculateCashBalance().doubleValue());
        summary.setTotalAssets(calculateTotalAssets().doubleValue());
        summary.setTotalLiabilities(calculateTotalLiabilities(startOfYear, endOfYear).doubleValue());
        summary.setTotalEquity(summary.getTotalAssets() - summary.getTotalLiabilities());

        List<ReportSummary.MonthlyData> monthlyTrends = new ArrayList<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        for (int i = 0; i < 12; i++) {
            LocalDate monthStart = LocalDate.of(currentYear, i + 1, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            BigDecimal revenue = calculateTotalRevenue(monthStart, monthEnd);
            BigDecimal expense = calculateTotalExpense(monthStart, monthEnd);
            monthlyTrends.add(new ReportSummary.MonthlyData(
                    months[i], revenue.doubleValue(), expense.doubleValue(),
                    revenue.subtract(expense).doubleValue()));
        }
        summary.setMonthlyTrends(monthlyTrends);

        List<ReportSummary.ExpenseCategory> expenseBreakdown = new ArrayList<>();
        expenseBreakdown.add(new ReportSummary.ExpenseCategory(
                "运营成本", totalExpense.multiply(new BigDecimal("0.4")).doubleValue(), 40.0));
        expenseBreakdown.add(new ReportSummary.ExpenseCategory(
                "人力成本", totalExpense.multiply(new BigDecimal("0.25")).doubleValue(), 25.0));
        expenseBreakdown.add(new ReportSummary.ExpenseCategory(
                "营销费用", totalExpense.multiply(new BigDecimal("0.15")).doubleValue(), 15.0));
        expenseBreakdown.add(new ReportSummary.ExpenseCategory(
                "研发费用", totalExpense.multiply(new BigDecimal("0.12")).doubleValue(), 12.0));
        expenseBreakdown.add(new ReportSummary.ExpenseCategory(
                "其他费用", totalExpense.multiply(new BigDecimal("0.08")).doubleValue(), 8.0));
        summary.setExpenseBreakdown(expenseBreakdown);

        return summary;
    }

    private BigDecimal calculateCurrentAssets(LocalDate start, LocalDate end) {
        return calculateCashBalance()
                .add(calculateReceivables(start, end))
                .add(calculateInventory());
    }

    private BigDecimal calculateNonCurrentAssets() {
        return new BigDecimal("5000000");
    }

    private BigDecimal calculateFixedAssets() {
        return new BigDecimal("3000000");
    }

    private BigDecimal calculateTotalAssets() {
        return calculateCurrentAssets(LocalDate.now(), LocalDate.now())
                .add(calculateNonCurrentAssets());
    }

    private BigDecimal calculateCurrentLiabilities(LocalDate start, LocalDate end) {
        return calculatePayables(start, end)
                .add(calculateTaxPayables(start, end));
    }

    private BigDecimal calculateCashBalance() {
        List<SettlementAccount> accounts = settlementAccountRepository.findAll();
        return accounts.stream()
                .map(SettlementAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateReceivables(LocalDate start, LocalDate end) {
        return incomeRepository.findAll().stream()
                .filter(i -> i.getIncomeDate().toLocalDate().isAfter(start) && i.getIncomeDate().toLocalDate().isBefore(end))
                .map(i -> i.getIncomeAmount() != null ? i.getIncomeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateInventory() {
        return new BigDecimal("2000000");
    }

    private BigDecimal calculatePayables(LocalDate start, LocalDate end) {
        return expenseRepository.findAll().stream()
                .filter(e -> e.getExpenseDate().toLocalDate().isAfter(start) && e.getExpenseDate().toLocalDate().isBefore(end))
                .map(e -> e.getExpenseAmount() != null ? e.getExpenseAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTaxPayables(LocalDate start, LocalDate end) {
        return calculatePayables(start, end).multiply(new BigDecimal("0.1"));
    }

    private BigDecimal calculateTotalRevenue(LocalDate start, LocalDate end) {
        return incomeRepository.findAll().stream()
                .filter(i -> i.getIncomeDate() != null)
                .filter(i -> {
                    LocalDate d = i.getIncomeDate().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .map(i -> i.getIncomeAmount() != null ? i.getIncomeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalExpense(LocalDate start, LocalDate end) {
        return expenseRepository.findAll().stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> {
                    LocalDate d = e.getExpenseDate().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .map(e -> e.getExpenseAmount() != null ? e.getExpenseAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalLiabilities(LocalDate start, LocalDate end) {
        return calculatePayables(start, end).add(calculateTaxPayables(start, end));
    }

    private LocalDate getPeriodStartDate(Integer fiscalYear, String periodType) {
        return switch (periodType) {
            case "MONTHLY" -> LocalDate.of(fiscalYear, 1, 1);
            case "QUARTERLY" -> LocalDate.of(fiscalYear, 1, 1);
            case "YEARLY" -> LocalDate.of(fiscalYear, 1, 1);
            default -> LocalDate.of(fiscalYear, 1, 1);
        };
    }

    private LocalDate getPeriodEndDate(Integer fiscalYear, String periodType) {
        return switch (periodType) {
            case "MONTHLY" -> LocalDate.of(fiscalYear, 12, 31);
            case "QUARTERLY" -> LocalDate.of(fiscalYear, 12, 31);
            case "YEARLY" -> LocalDate.of(fiscalYear, 12, 31);
            default -> LocalDate.of(fiscalYear, 12, 31);
        };
    }

    private FinancialReport.PeriodType getPeriodType(LocalDate start, LocalDate end) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (days <= 31) return FinancialReport.PeriodType.MONTHLY;
        if (days <= 92) return FinancialReport.PeriodType.QUARTERLY;
        return FinancialReport.PeriodType.YEARLY;
    }

    private String getPeriodLabel(String periodType) {
        return switch (periodType) {
            case "MONTHLY" -> "月度";
            case "QUARTERLY" -> "季度";
            case "YEARLY" -> "年度";
            default -> "";
        };
    }

    private String generateReportCode(String prefix) {
        return prefix + LocalDateTime.now().format(CODE_FORMATTER);
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    private void createAuditLog(Long entityId, String operation, String operatorId) {
        FinanceAuditLog auditLog = FinanceAuditLog.builder()
                .entityType("FinancialReport")
                .entityId(entityId)
                .operationType(operation)
                .operatorId(operatorId != null ? operatorId : "SYSTEM")
                .operationTime(LocalDateTime.now())
                .severity(FinanceAuditLog.Severity.INFO)
                .changeSummary("Financial report " + operation.toLowerCase().replace("_", " "))
                .build();

        auditLogRepository.save(auditLog);
    }
}
