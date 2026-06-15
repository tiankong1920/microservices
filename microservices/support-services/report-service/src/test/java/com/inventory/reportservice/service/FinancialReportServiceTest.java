package com.inventory.reportservice.service;

import com.inventory.reportservice.client.FinanceServiceClient;
import com.inventory.reportservice.dto.FinancialReportDTO;
import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.repository.IReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialReportServiceTest {

    @Mock
    private IReportRepository reportRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IReportService reportService;

    @Mock
    private FinanceServiceClient financeServiceClient;

    private FinancialReportService financialReportService;

    @BeforeEach
    void setUp() {
        financialReportService = new FinancialReportService(reportRepository, modelMapper, reportService, financeServiceClient);
    }

    @Test
    void testGenerateFinancialReport() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        FinancialReportDTO request = FinancialReportDTO.builder()
                .reportName("月度财务报表")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy("admin")
                .build();

        ReportResponseDTO baseReport = ReportResponseDTO.builder()
                .reportId(1L)
                .reportName("月度财务报表")
                .reportType(com.inventory.reportservice.entity.ReportType.FINANCIAL_REPORT)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        FinancialReportDTO response = FinancialReportDTO.builder()
                .id(1L)
                .reportName("月度财务报表")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy("admin")
                .build();

        when(reportService.generateReport(any(ReportRequestDTO.class))).thenReturn(baseReport);
        when(modelMapper.map(baseReport, FinancialReportDTO.class)).thenReturn(response);
        when(financeServiceClient.getIncomesByDateRange(startDate, endDate)).thenReturn(new ArrayList<>());
        when(financeServiceClient.getExpensesByDateRange(startDate, endDate)).thenReturn(new ArrayList<>());
        when(financeServiceClient.getSettlementAccounts()).thenReturn(new ArrayList<>());

        FinancialReportDTO result = financialReportService.generateFinancialReport(request);

        assertNotNull(result);
        assertEquals("月度财务报表", result.getReportName());
        verify(reportService, times(1)).generateReport(any(ReportRequestDTO.class));
        verify(financeServiceClient, times(1)).getIncomesByDateRange(startDate, endDate);
        verify(financeServiceClient, times(1)).getExpensesByDateRange(startDate, endDate);
        verify(financeServiceClient, times(1)).getSettlementAccounts();
    }

    @Test
    void testGenerateFinancialReportWithRealData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        FinancialReportDTO request = FinancialReportDTO.builder()
                .reportName("月度财务报表")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy("admin")
                .build();

        ReportResponseDTO baseReport = ReportResponseDTO.builder()
                .reportId(1L)
                .reportName("月度财务报表")
                .reportType(com.inventory.reportservice.entity.ReportType.FINANCIAL_REPORT)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        FinancialReportDTO response = FinancialReportDTO.builder()
                .id(1L)
                .reportName("月度财务报表")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy("admin")
                .build();

        List<FinanceServiceClient.IncomeDTO> incomes = List.of(
                new FinanceServiceClient.IncomeDTO(
                        1L, "IN20240101", "销售收入", new java.math.BigDecimal(10000), startDate.plusDays(5),
                        "产品销售", "客户订单", 1L, 1L, "已确认"
                )
        );

        List<FinanceServiceClient.ExpenseDTO> expenses = List.of(
                new FinanceServiceClient.ExpenseDTO(
                        1L, "EX20240101", "采购支出", new java.math.BigDecimal(5000), startDate.plusDays(10),
                        "原材料采购", "采购成本", 1L, 1L, "已确认"
                )
        );

        List<FinanceServiceClient.SettlementAccountDTO> accounts = List.of(
                new FinanceServiceClient.SettlementAccountDTO(
                        1L, "ACC001", "银行存款", "流动资产", new java.math.BigDecimal(100000), "CNY", "正常"
                )
        );

        when(reportService.generateReport(any(ReportRequestDTO.class))).thenReturn(baseReport);
        when(modelMapper.map(baseReport, FinancialReportDTO.class)).thenReturn(response);
        when(financeServiceClient.getIncomesByDateRange(startDate, endDate)).thenReturn(incomes);
        when(financeServiceClient.getExpensesByDateRange(startDate, endDate)).thenReturn(expenses);
        when(financeServiceClient.getSettlementAccounts()).thenReturn(accounts);

        FinancialReportDTO result = financialReportService.generateFinancialReport(request);

        assertNotNull(result);
        assertNotNull(result.getIncomeDetails());
        assertNotNull(result.getExpenseDetails());
        assertNotNull(result.getAccountBalances());
        assertEquals(1, result.getIncomeDetails().size());
        assertEquals(1, result.getExpenseDetails().size());
        assertEquals(1, result.getAccountBalances().size());
        assertEquals(new java.math.BigDecimal(10000), result.getTotalIncome());
        assertEquals(new java.math.BigDecimal(5000), result.getTotalExpense());
        assertEquals(new java.math.BigDecimal(5000), result.getNetProfit());
    }
}
