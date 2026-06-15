package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.TaxRecordDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ITaxCalculationService {

    TaxRecordDTO calculateVat(BigDecimal taxableAmount, BigDecimal inputTax,
            BigDecimal outputTax, LocalDateTime periodStart, LocalDateTime periodEnd);

    TaxRecordDTO calculateCorporateIncomeTax(BigDecimal revenue,
            BigDecimal deductibleExpenses, LocalDateTime periodStart, LocalDateTime periodEnd);

    TaxRecordDTO calculatePersonalIncomeTax(BigDecimal incomeAmount, String incomeType, LocalDateTime periodStart, LocalDateTime periodEnd);

    TaxRecordDTO createTaxRecord(TaxRecordDTO taxRecordDTO);

    Optional<TaxRecordDTO> getTaxRecordById(Long id);

    Optional<TaxRecordDTO> getTaxRecordByNumber(String taxNumber);

    List<TaxRecordDTO> getAllTaxRecords();

    List<TaxRecordDTO> getTaxRecordsByType(String taxType);

    List<TaxRecordDTO> getTaxRecordsByStatus(String status);

    List<TaxRecordDTO> getTaxRecordsByPeriod(LocalDateTime startDate, LocalDateTime endDate);

    TaxRecordDTO declareTax(Long id, String declarerId);

    TaxRecordDTO payTax(Long id, String payerId);

    TaxRecordDTO adjustTax(Long id, TaxRecordDTO adjustments);

    List<TaxRecordDTO> getOverdueTaxes();

    BigDecimal getTotalTaxLiability(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getTotalInputTax(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getTotalOutputTax(LocalDateTime startDate, LocalDateTime endDate);

    TaxAnalysisResult analyzeTaxBurden(LocalDateTime startDate, LocalDateTime endDate);

    class TaxAnalysisResult {
        private BigDecimal totalTaxLiability;
        private BigDecimal totalInputTax;
        private BigDecimal totalOutputTax;
        private BigDecimal netTaxPayable;
        private BigDecimal taxBurdenRate;
        private String analysisPeriod;
        private List<String> optimizationSuggestions;

        public BigDecimal getTotalTaxLiability() { return totalTaxLiability; }
        public void setTotalTaxLiability(BigDecimal totalTaxLiability) { this.totalTaxLiability = totalTaxLiability; }
        public BigDecimal getTotalInputTax() { return totalInputTax; }
        public void setTotalInputTax(BigDecimal totalInputTax) { this.totalInputTax = totalInputTax; }
        public BigDecimal getTotalOutputTax() { return totalOutputTax; }
        public void setTotalOutputTax(BigDecimal totalOutputTax) { this.totalOutputTax = totalOutputTax; }
        public BigDecimal getNetTaxPayable() { return netTaxPayable; }
        public void setNetTaxPayable(BigDecimal netTaxPayable) { this.netTaxPayable = netTaxPayable; }
        public BigDecimal getTaxBurdenRate() { return taxBurdenRate; }
        public void setTaxBurdenRate(BigDecimal taxBurdenRate) { this.taxBurdenRate = taxBurdenRate; }
        public String getAnalysisPeriod() { return analysisPeriod; }
        public void setAnalysisPeriod(String analysisPeriod) { this.analysisPeriod = analysisPeriod; }
        public List<String> getOptimizationSuggestions() { return optimizationSuggestions; }
        public void setOptimizationSuggestions(List<String> optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }
    }
}
