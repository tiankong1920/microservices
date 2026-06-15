package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.FinancialReportDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IFinancialReportService {

    FinancialReportDTO generateBalanceSheet(Integer fiscalYear, String periodType, String generatedBy);

    FinancialReportDTO generateIncomeStatement(Integer fiscalYear, String periodType, String generatedBy);

    FinancialReportDTO generateCashFlowStatement(Integer fiscalYear, String periodType, String generatedBy);

    FinancialReportDTO generateCustomReport(String reportName, String reportType, LocalDate startDate, LocalDate endDate, String generatedBy);

    FinancialReportDTO saveReport(FinancialReportDTO reportDTO);

    Optional<FinancialReportDTO> getReportById(Long id);

    Optional<FinancialReportDTO> getReportByCode(String reportCode);

    List<FinancialReportDTO> getAllReports();

    List<FinancialReportDTO> getReportsByType(String reportType);

    List<FinancialReportDTO> getReportsByStatus(String status);

    List<FinancialReportDTO> getReportsByPeriod(LocalDate startDate, LocalDate endDate);

    List<FinancialReportDTO> getReportsByYear(Integer fiscalYear);

    FinancialReportDTO approveReport(Long id, String approverId);

    FinancialReportDTO publishReport(Long id);

    void deleteReport(Long id);

    byte[] exportToExcel(Long reportId);

    byte[] exportToPdf(Long reportId);

    byte[] exportToCsv(Long reportId);

    ReportSummary getDashboardSummary();

    class ReportSummary {
        private Integer currentYear;
        private Double totalRevenue;
        private Double totalExpense;
        private Double netProfit;
        private Double profitMargin;
        private Double totalAssets;
        private Double totalLiabilities;
        private Double totalEquity;
        private Double cashBalance;
        private Double budgetUtilizationRate;
        private Double yearOverYearGrowth;
        private List<MonthlyData> monthlyTrends;
        private List<ExpenseCategory> expenseBreakdown;

        public static class MonthlyData {
            private String month;
            private Double revenue;
            private Double expense;
            private Double profit;

            public MonthlyData(String month, Double revenue, Double expense, Double profit) {
                this.month = month;
                this.revenue = revenue;
                this.expense = expense;
                this.profit = profit;
            }

            public String getMonth() { return month; }
            public Double getRevenue() { return revenue; }
            public Double getExpense() { return expense; }
            public Double getProfit() { return profit; }
        }

        public static class ExpenseCategory {
            private String category;
            private Double amount;
            private Double percentage;

            public ExpenseCategory(String category, Double amount, Double percentage) {
                this.category = category;
                this.amount = amount;
                this.percentage = percentage;
            }

            public String getCategory() { return category; }
            public Double getAmount() { return amount; }
            public Double getPercentage() { return percentage; }
        }

        public Integer getCurrentYear() { return currentYear; }
        public void setCurrentYear(Integer currentYear) { this.currentYear = currentYear; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Double getTotalExpense() { return totalExpense; }
        public void setTotalExpense(Double totalExpense) { this.totalExpense = totalExpense; }
        public Double getNetProfit() { return netProfit; }
        public void setNetProfit(Double netProfit) { this.netProfit = netProfit; }
        public Double getProfitMargin() { return profitMargin; }
        public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }
        public Double getTotalAssets() { return totalAssets; }
        public void setTotalAssets(Double totalAssets) { this.totalAssets = totalAssets; }
        public Double getTotalLiabilities() { return totalLiabilities; }
        public void setTotalLiabilities(Double totalLiabilities) { this.totalLiabilities = totalLiabilities; }
        public Double getTotalEquity() { return totalEquity; }
        public void setTotalEquity(Double totalEquity) { this.totalEquity = totalEquity; }
        public Double getCashBalance() { return cashBalance; }
        public void setCashBalance(Double cashBalance) { this.cashBalance = cashBalance; }
        public Double getBudgetUtilizationRate() { return budgetUtilizationRate; }
        public void setBudgetUtilizationRate(Double budgetUtilizationRate) { this.budgetUtilizationRate = budgetUtilizationRate; }
        public Double getYearOverYearGrowth() { return yearOverYearGrowth; }
        public void setYearOverYearGrowth(Double yearOverYearGrowth) { this.yearOverYearGrowth = yearOverYearGrowth; }
        public List<MonthlyData> getMonthlyTrends() { return monthlyTrends; }
        public void setMonthlyTrends(List<MonthlyData> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
        public List<ExpenseCategory> getExpenseBreakdown() { return expenseBreakdown; }
        public void setExpenseBreakdown(List<ExpenseCategory> expenseBreakdown) { this.expenseBreakdown = expenseBreakdown; }
    }
}
