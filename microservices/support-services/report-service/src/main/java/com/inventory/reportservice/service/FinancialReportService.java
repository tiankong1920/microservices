package com.inventory.reportservice.service;

import com.inventory.reportservice.client.FinanceServiceClient;
import com.inventory.reportservice.dto.FinancialReportDTO;
import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.entity.Report;
import com.inventory.reportservice.entity.ReportType;
import com.inventory.reportservice.repository.IReportRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FinancialReportService {
    private final IReportRepository reportRepository;
    private final ModelMapper modelMapper;
    private final IReportService reportService;
    private final FinanceServiceClient financeServiceClient;

    public FinancialReportDTO generateFinancialReport(FinancialReportDTO financialReportDTO) {
        // 1. 生成基础报表
        ReportRequestDTO baseReportDTO = ReportRequestDTO.builder()
                .reportName(financialReportDTO.getReportName())
                .reportType("FINANCIAL_REPORT")
                .startDate(financialReportDTO.getStartDate())
                .endDate(financialReportDTO.getEndDate())
                .build();

        ReportResponseDTO generatedReport = reportService.generateReport(baseReportDTO);

        // 2. 从财务服务获取真实数据
        FinancialReportDTO result = modelMapper.map(generatedReport, FinancialReportDTO.class);

        // 2.1 获取收入明细数据
        List<FinanceServiceClient.IncomeDTO> incomeDTOs = financeServiceClient.getIncomesByDateRange(
                financialReportDTO.getStartDate(), financialReportDTO.getEndDate());

        List<FinancialReportDTO.IncomeDetailDTO> incomeDetails = incomeDTOs.stream()
                .map(incomeDTO -> FinancialReportDTO.IncomeDetailDTO.builder()
                        .incomeId(incomeDTO.id())
                        .incomeDate(incomeDTO.incomeDate())
                        .incomeType(incomeDTO.incomeType())
                        .amount(incomeDTO.amount())
                        .description(incomeDTO.description())
                        .source(incomeDTO.source())
                        .relatedId(incomeDTO.relatedId())
                        .build())
                .toList();
        result.setIncomeDetails(incomeDetails);

        // 2.2 获取支出明细数据
        List<FinanceServiceClient.ExpenseDTO> expenseDTOs = financeServiceClient.getExpensesByDateRange(
                financialReportDTO.getStartDate(), financialReportDTO.getEndDate());

        List<FinancialReportDTO.ExpenseDetailDTO> expenseDetails = expenseDTOs.stream()
                .map(expenseDTO -> FinancialReportDTO.ExpenseDetailDTO.builder()
                        .expenseId(expenseDTO.id())
                        .expenseDate(expenseDTO.expenseDate())
                        .expenseType(expenseDTO.expenseType())
                        .amount(expenseDTO.amount())
                        .description(expenseDTO.description())
                        .category(expenseDTO.category())
                        .relatedId(expenseDTO.relatedId())
                        .build())
                .toList();
        result.setExpenseDetails(expenseDetails);

        // 2.3 获取账户余额数据
        List<FinanceServiceClient.SettlementAccountDTO> accountDTOs = financeServiceClient.getSettlementAccounts();

        List<FinancialReportDTO.AccountBalanceDTO> accountBalances = accountDTOs.stream()
                .map(accountDTO -> FinancialReportDTO.AccountBalanceDTO.builder()
                        .accountId(accountDTO.id())
                        .accountName(accountDTO.accountName())
                        .accountType(accountDTO.accountType())
                        .currentBalance(accountDTO.balance())
                        .build())
                .toList();
        result.setAccountBalances(accountBalances);

        // 3. 计算汇总数据
        java.math.BigDecimal totalIncome = incomeDetails.stream()
                .map(FinancialReportDTO.IncomeDetailDTO::getAmount)
                .reduce(new java.math.BigDecimal(0), java.math.BigDecimal::add);

        java.math.BigDecimal totalExpense = expenseDetails.stream()
                .map(FinancialReportDTO.ExpenseDetailDTO::getAmount)
                .reduce(new java.math.BigDecimal(0), java.math.BigDecimal::add);

        java.math.BigDecimal netProfit = totalIncome.subtract(totalExpense);

        result.setTotalIncome(totalIncome);
        result.setTotalExpense(totalExpense);
        result.setNetProfit(netProfit);

        return result;
    }

    public FinancialReportDTO getFinancialReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financial report not found with id: " + id));

        // 模拟财务报表数据
        FinancialReportDTO result = modelMapper.map(report, FinancialReportDTO.class);

        // 填充模拟数据
        List<FinancialReportDTO.IncomeDetailDTO> incomeDetails = new ArrayList<>();
        incomeDetails.add(FinancialReportDTO.IncomeDetailDTO.builder()
                .incomeId(1L)
                .incomeDate(LocalDateTime.now().minusDays(1))
                .incomeType("销售收入")
                .amount(new java.math.BigDecimal(50000))
                .description("产品销售")
                .source("客户订单")
                .relatedId(1001L)
                .build());
        result.setIncomeDetails(incomeDetails);

        result.setExpenseDetails(new ArrayList<>());
        result.setAccountBalances(new ArrayList<>());

        return result;
    }

    public List<FinancialReportDTO> getFinancialReportsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Report> reports = reportRepository.findByReportTypeAndReportDateBetween(
                ReportType.FINANCIAL_REPORT, startDate, endDate);
        return reports.stream()
                .map(report -> {
                    FinancialReportDTO dto = modelMapper.map(report, FinancialReportDTO.class);
                    dto.setIncomeDetails(new ArrayList<>());
                    dto.setExpenseDetails(new ArrayList<>());
                    dto.setAccountBalances(new ArrayList<>());
                    return dto;
                })
                .toList();
    }
}
