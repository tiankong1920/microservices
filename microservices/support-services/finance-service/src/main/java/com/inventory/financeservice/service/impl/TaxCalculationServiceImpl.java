package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.TaxRecordDTO;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.entity.TaxRecord;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.repository.ITaxRecordRepository;
import com.inventory.financeservice.service.ITaxCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class TaxCalculationServiceImpl implements ITaxCalculationService {

    private final ITaxRecordRepository taxRecordRepository;
    private final IFinanceAuditLogRepository auditLogRepository;

    private static final BigDecimal VAT_RATE_STANDARD = new BigDecimal("0.13");
    private static final BigDecimal VAT_RATE_REDUCED = new BigDecimal("0.09");
    private static final BigDecimal CIT_RATE = new BigDecimal("0.25");
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public TaxRecordDTO calculateVat(BigDecimal taxableAmount, BigDecimal inputTax,
            BigDecimal outputTax, LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Calculating VAT for period {} to {}", periodStart, periodEnd);

        if (inputTax == null) inputTax = BigDecimal.ZERO;
        if (outputTax == null) outputTax = BigDecimal.ZERO;

        BigDecimal netTax = outputTax.subtract(inputTax);
        BigDecimal taxRate = outputTax.compareTo(BigDecimal.ZERO) > 0
                ? outputTax.divide(taxableAmount, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return TaxRecordDTO.builder()
                .taxNumber(generateTaxNumber("VAT"))
                .taxType("VAT_PAYABLE")
                .taxableAmount(taxableAmount)
                .taxRate(taxRate)
                .taxAmount(netTax)
                .inputTaxAmount(inputTax)
                .outputTaxAmount(outputTax)
                .netTaxAmount(netTax.compareTo(BigDecimal.ZERO) > 0 ? netTax : BigDecimal.ZERO)
                .taxablePeriodStart(periodStart)
                .taxablePeriodEnd(periodEnd)
                .status("PENDING")
                .dueDate(periodEnd.plusDays(15))
                .build();
    }

    @Override
    public TaxRecordDTO calculateCorporateIncomeTax(BigDecimal revenue,
            BigDecimal deductibleExpenses, LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Calculating Corporate Income Tax for period {} to {}", periodStart, periodEnd);

        if (revenue == null) revenue = BigDecimal.ZERO;
        if (deductibleExpenses == null) deductibleExpenses = BigDecimal.ZERO;

        BigDecimal taxableIncome = revenue.subtract(deductibleExpenses);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        BigDecimal estimatedTax = taxableIncome.multiply(CIT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal effectiveRate = taxableIncome.compareTo(BigDecimal.ZERO) > 0
                ? estimatedTax.divide(taxableIncome, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return TaxRecordDTO.builder()
                .taxNumber(generateTaxNumber("CIT"))
                .taxType("CORPORATE_INCOME_TAX")
                .taxableAmount(taxableIncome)
                .taxRate(effectiveRate)
                .taxAmount(estimatedTax)
                .taxablePeriodStart(periodStart)
                .taxablePeriodEnd(periodEnd)
                .status("PENDING")
                .dueDate(periodEnd.plusDays(30))
                .build();
    }

    @Override
    public TaxRecordDTO calculatePersonalIncomeTax(BigDecimal incomeAmount, String incomeType, LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Calculating Personal Income Tax for income type: {}", incomeType);

        if (incomeAmount == null) incomeAmount = BigDecimal.ZERO;

        BigDecimal taxAmount = calculatePIT(incomeAmount);

        return TaxRecordDTO.builder()
                .taxNumber(generateTaxNumber("PIT"))
                .taxType("PERSONAL_INCOME_TAX")
                .taxableAmount(incomeAmount)
                .taxRate(calculatePITRate(incomeAmount))
                .taxAmount(taxAmount)
                .taxablePeriodStart(periodStart)
                .taxablePeriodEnd(periodEnd)
                .status("PENDING")
                .dueDate(periodEnd.plusDays(7))
                .referenceNumber(incomeType)
                .build();
    }

    @Override
    @Transactional
    public TaxRecordDTO createTaxRecord(TaxRecordDTO taxRecordDTO) {
        log.info("Creating tax record: {}", taxRecordDTO.getTaxType());

        TaxRecord record = taxRecordDTO.toEntity();
        if (record.getTaxNumber() == null || record.getTaxNumber().isEmpty()) {
            record.setTaxNumber(generateTaxNumber(record.getTaxType().name()));
        }
        record.setStatus(TaxRecord.TaxStatus.PENDING);

        TaxRecord saved = taxRecordRepository.save(record);

        createAuditLog(saved.getId(), "CREATE", null, saved.toString(), "SYSTEM");

        return TaxRecordDTO.fromEntity(saved);
    }

    @Override
    public Optional<TaxRecordDTO> getTaxRecordById(Long id) {
        return taxRecordRepository.findById(id).map(TaxRecordDTO::fromEntity);
    }

    @Override
    public Optional<TaxRecordDTO> getTaxRecordByNumber(String taxNumber) {
        return taxRecordRepository.findByTaxNumber(taxNumber).map(TaxRecordDTO::fromEntity);
    }

    @Override
    public List<TaxRecordDTO> getAllTaxRecords() {
        return taxRecordRepository.findAll().stream()
                .map(TaxRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaxRecordDTO> getTaxRecordsByType(String taxType) {
        return taxRecordRepository.findByTaxType(TaxRecord.TaxType.valueOf(taxType)).stream()
                .map(TaxRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaxRecordDTO> getTaxRecordsByStatus(String status) {
        return taxRecordRepository.findByStatus(TaxRecord.TaxStatus.valueOf(status)).stream()
                .map(TaxRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaxRecordDTO> getTaxRecordsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return taxRecordRepository.findByTaxablePeriod(startDate, endDate).stream()
                .map(TaxRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaxRecordDTO declareTax(Long id, String declarerId) {
        TaxRecord record = taxRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax record not found: " + id));

        record.setStatus(TaxRecord.TaxStatus.DECLARED);
        record.setDeclarationDate(LocalDateTime.now());

        TaxRecord updated = taxRecordRepository.save(record);

        createAuditLog(updated.getId(), "DECLARE", record.toString(), updated.toString(), declarerId);

        return TaxRecordDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public TaxRecordDTO payTax(Long id, String payerId) {
        TaxRecord record = taxRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax record not found: " + id));

        record.setStatus(TaxRecord.TaxStatus.PAID);
        record.setPaymentDate(LocalDateTime.now());

        TaxRecord updated = taxRecordRepository.save(record);

        createAuditLog(updated.getId(), "PAY", record.toString(), updated.toString(), payerId);

        return TaxRecordDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public TaxRecordDTO adjustTax(Long id, TaxRecordDTO adjustments) {
        TaxRecord record = taxRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax record not found: " + id));

        String oldValue = record.toString();

        if (adjustments.getTaxableAmount() != null) {
            record.setTaxableAmount(adjustments.getTaxableAmount());
        }
        if (adjustments.getTaxRate() != null) {
            record.setTaxRate(adjustments.getTaxRate());
        }
        if (adjustments.getTaxAmount() != null) {
            record.setTaxAmount(adjustments.getTaxAmount());
        }
        if (adjustments.getRemarks() != null) {
            record.setRemarks(adjustments.getRemarks());
        }

        record.setStatus(TaxRecord.TaxStatus.ADJUSTED);

        TaxRecord updated = taxRecordRepository.save(record);

        createAuditLog(updated.getId(), "ADJUST", oldValue, updated.toString(), "SYSTEM");

        return TaxRecordDTO.fromEntity(updated);
    }

    @Override
    public List<TaxRecordDTO> getOverdueTaxes() {
        return taxRecordRepository.findOverdueTaxes(LocalDateTime.now()).stream()
                .map(TaxRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalTaxLiability(LocalDateTime startDate, LocalDateTime endDate) {
        return taxRecordRepository.sumTaxAmountByTypeAndStatus(
                TaxRecord.TaxType.VAT_PAYABLE, TaxRecord.TaxStatus.PENDING);
    }

    @Override
    public BigDecimal getTotalInputTax(LocalDateTime startDate, LocalDateTime endDate) {
        return taxRecordRepository.sumInputTaxByPeriod(startDate, endDate);
    }

    @Override
    public BigDecimal getTotalOutputTax(LocalDateTime startDate, LocalDateTime endDate) {
        return taxRecordRepository.sumOutputTaxByPeriod(startDate, endDate);
    }

    @Override
    public TaxAnalysisResult analyzeTaxBurden(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal inputTax = taxRecordRepository.sumInputTaxByPeriod(startDate, endDate);
        BigDecimal outputTax = taxRecordRepository.sumOutputTaxByPeriod(startDate, endDate);

        if (inputTax == null) inputTax = BigDecimal.ZERO;
        if (outputTax == null) outputTax = BigDecimal.ZERO;

        BigDecimal netTax = outputTax.subtract(inputTax);
        BigDecimal totalRevenue = taxRecordRepository.findByTaxablePeriod(startDate, endDate).stream()
                .map(TaxRecord::getTaxableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxBurdenRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netTax.divide(totalRevenue, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        TaxAnalysisResult result = new TaxAnalysisResult();
        result.setTotalInputTax(inputTax);
        result.setTotalOutputTax(outputTax);
        result.setNetTaxPayable(netTax.compareTo(BigDecimal.ZERO) > 0 ? netTax : BigDecimal.ZERO);
        result.setTaxBurdenRate(taxBurdenRate);
        result.setAnalysisPeriod(startDate + " to " + endDate);

        List<String> suggestions = new ArrayList<>();
        if (netTax.compareTo(BigDecimal.ZERO) > 0) {
            suggestions.add("Consider optimizing input tax credits to reduce net tax payable");
            suggestions.add("Review eligible deductions to lower taxable income");
        }
        if (taxBurdenRate.compareTo(new BigDecimal("5")) > 0) {
            suggestions.add("Tax burden rate is above 5%, consider tax planning strategies");
        }
        result.setOptimizationSuggestions(suggestions);

        return result;
    }

    private String generateTaxNumber(String type) {
        return type + LocalDateTime.now().format(CODE_FORMATTER);
    }

    private BigDecimal calculatePIT(BigDecimal income) {
        if (income.compareTo(new BigDecimal("3000")) <= 0) {
            return income.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
        } else if (income.compareTo(new BigDecimal("12000")) <= 0) {
            return income.subtract(new BigDecimal("3000")).multiply(new BigDecimal("0.10"))
                    .add(new BigDecimal("90")).setScale(2, RoundingMode.HALF_UP);
        } else if (income.compareTo(new BigDecimal("25000")) <= 0) {
            return income.subtract(new BigDecimal("12000")).multiply(new BigDecimal("0.20"))
                    .add(new BigDecimal("990")).setScale(2, RoundingMode.HALF_UP);
        } else if (income.compareTo(new BigDecimal("35000")) <= 0) {
            return income.subtract(new BigDecimal("25000")).multiply(new BigDecimal("0.25"))
                    .add(new BigDecimal("3590")).setScale(2, RoundingMode.HALF_UP);
        } else if (income.compareTo(new BigDecimal("55000")) <= 0) {
            return income.subtract(new BigDecimal("35000")).multiply(new BigDecimal("0.30"))
                    .add(new BigDecimal("6090")).setScale(2, RoundingMode.HALF_UP);
        } else if (income.compareTo(new BigDecimal("80000")) <= 0) {
            return income.subtract(new BigDecimal("55000")).multiply(new BigDecimal("0.35"))
                    .add(new BigDecimal("12090")).setScale(2, RoundingMode.HALF_UP);
        } else {
            return income.subtract(new BigDecimal("80000")).multiply(new BigDecimal("0.45"))
                    .add(new BigDecimal("20840")).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal calculatePITRate(BigDecimal income) {
        if (income.compareTo(new BigDecimal("3000")) <= 0) {
            return new BigDecimal("3");
        } else if (income.compareTo(new BigDecimal("12000")) <= 0) {
            return new BigDecimal("10");
        } else if (income.compareTo(new BigDecimal("25000")) <= 0) {
            return new BigDecimal("20");
        } else if (income.compareTo(new BigDecimal("35000")) <= 0) {
            return new BigDecimal("25");
        } else if (income.compareTo(new BigDecimal("55000")) <= 0) {
            return new BigDecimal("30");
        } else if (income.compareTo(new BigDecimal("80000")) <= 0) {
            return new BigDecimal("35");
        } else {
            return new BigDecimal("45");
        }
    }

    private void createAuditLog(Long entityId, String operation, String oldValue, String newValue, String operatorId) {
        FinanceAuditLog auditLog = FinanceAuditLog.builder()
                .entityType("TaxRecord")
                .entityId(entityId)
                .operationType(operation)
                .operatorId(operatorId)
                .operationTime(LocalDateTime.now())
                .severity(FinanceAuditLog.Severity.INFO)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeSummary("Tax record " + operation.toLowerCase())
                .build();

        auditLogRepository.save(auditLog);
    }
}
