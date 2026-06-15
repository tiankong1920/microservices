package com.invoice.invoiceservice.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.InvoiceCalculationRequest;
import com.invoice.invoiceservice.dto.InvoiceCalculationResult;
import com.invoice.invoiceservice.dto.InvoiceItemDTO;
import com.invoice.invoiceservice.service.InvoiceCalculationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
public class InvoiceCalculationServiceImpl implements InvoiceCalculationService {

    private static final int SCALE = 2;
    private static final int CALCULATION_SCALE = 4;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public InvoiceItemDTO calculateItemAmount(InvoiceItemDTO item) {
        BigDecimal quantity = item.getQuantity();
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal taxRate = item.getTaxRate();

        BigDecimal amount = quantity.multiply(unitPrice)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal taxRateDecimal = taxRate.divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
        BigDecimal taxAmount = amount.multiply(taxRateDecimal)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = amount.add(taxAmount).subtract(discountAmount)
                .setScale(SCALE, RoundingMode.HALF_UP);

        item.setAmount(amount);
        item.setTaxAmount(taxAmount);
        item.setTotalAmount(totalAmount);

        return item;
    }

    @Override
    public InvoiceCalculationResult calculateInvoice(InvoiceCalculationRequest request) {
        List<InvoiceItemDTO> calculatedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO item : request.getItems()) {
            InvoiceItemDTO calculated = calculateItemAmount(item);
            calculatedItems.add(calculated);

            totalAmount = totalAmount.add(calculated.getAmount());
            totalTaxAmount = totalTaxAmount.add(calculated.getTaxAmount());
            if (calculated.getDiscountAmount() != null) {
                totalDiscountAmount = totalDiscountAmount.add(calculated.getDiscountAmount());
            }
        }

        BigDecimal grandTotal = totalAmount.add(totalTaxAmount).subtract(totalDiscountAmount)
                .setScale(SCALE, RoundingMode.HALF_UP);

        InvoiceCalculationResult result = InvoiceCalculationResult.builder()
                .items(calculatedItems)
                .totalAmount(totalAmount)
                .totalTaxAmount(totalTaxAmount)
                .grandTotal(grandTotal)
                .totalDiscountAmount(totalDiscountAmount)
                .build();

        List<String> errors = validateInvoiceCalculation(result);
        result.setValid(errors.isEmpty());
        result.setValidationErrors(errors);

        if (!errors.isEmpty()) {
            log.warn("发票计算校验失败: {}", errors);
        }

        return result;
    }

    @Override
    public boolean validateCalculation(InvoiceItemDTO item) {
        if (item.getQuantity() == null || item.getUnitPrice() == null || item.getTaxRate() == null) {
            return false;
        }

        BigDecimal expectedAmount = item.getQuantity().multiply(item.getUnitPrice())
                .setScale(SCALE, RoundingMode.HALF_UP);
        if (item.getAmount() != null && item.getAmount().compareTo(expectedAmount) != 0) {
            return false;
        }

        BigDecimal taxRateDecimal = item.getTaxRate().divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
        BigDecimal expectedTaxAmount = expectedAmount.multiply(taxRateDecimal)
                .setScale(SCALE, RoundingMode.HALF_UP);
        if (item.getTaxAmount() != null && item.getTaxAmount().compareTo(expectedTaxAmount) != 0) {
            return false;
        }

        BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal expectedTotal = expectedAmount.add(expectedTaxAmount).subtract(discountAmount)
                .setScale(SCALE, RoundingMode.HALF_UP);
        if (item.getTotalAmount() != null && item.getTotalAmount().compareTo(expectedTotal) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateInvoiceCalculation(InvoiceCalculationResult result) {
        List<String> errors = new ArrayList<>();

        BigDecimal sumAmount = BigDecimal.ZERO;
        BigDecimal sumTaxAmount = BigDecimal.ZERO;
        BigDecimal sumDiscount = BigDecimal.ZERO;

        for (int i = 0; i < result.getItems().size(); i++) {
            InvoiceItemDTO item = result.getItems().get(i);
            int itemNo = i + 1;

            BigDecimal expectedAmount = item.getQuantity().multiply(item.getUnitPrice())
                    .setScale(SCALE, RoundingMode.HALF_UP);
            if (item.getAmount().compareTo(expectedAmount) != 0) {
                errors.add(String.format("第%d项: 金额计算错误，数量×单价=%s，实际金额=%s",
                        itemNo, expectedAmount, item.getAmount()));
            }

            BigDecimal taxRateDecimal = item.getTaxRate().divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
            BigDecimal expectedTaxAmount = item.getAmount().multiply(taxRateDecimal)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            if (item.getTaxAmount().compareTo(expectedTaxAmount) != 0) {
                errors.add(String.format("第%d项: 税额计算错误，金额×税率=%s，实际税额=%s",
                        itemNo, expectedTaxAmount, item.getTaxAmount()));
            }

            BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal expectedTotal = item.getAmount().add(item.getTaxAmount()).subtract(discountAmount)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            if (item.getTotalAmount().compareTo(expectedTotal) != 0) {
                errors.add(String.format("第%d项: 价税合计计算错误，金额+税额-折扣=%s，实际合计=%s",
                        itemNo, expectedTotal, item.getTotalAmount()));
            }

            sumAmount = sumAmount.add(item.getAmount());
            sumTaxAmount = sumTaxAmount.add(item.getTaxAmount());
            sumDiscount = sumDiscount.add(discountAmount);
        }

        if (result.getTotalAmount().compareTo(sumAmount.setScale(SCALE, RoundingMode.HALF_UP)) != 0) {
            errors.add(String.format("金额合计错误，各项金额之和=%s，合计金额=%s",
                    sumAmount, result.getTotalAmount()));
        }

        if (result.getTotalTaxAmount().compareTo(sumTaxAmount.setScale(SCALE, RoundingMode.HALF_UP)) != 0) {
            errors.add(String.format("税额合计错误，各项税额之和=%s，合计税额=%s",
                    sumTaxAmount, result.getTotalTaxAmount()));
        }

        BigDecimal expectedGrandTotal = sumAmount.add(sumTaxAmount).subtract(sumDiscount)
                .setScale(SCALE, RoundingMode.HALF_UP);
        if (result.getGrandTotal().compareTo(expectedGrandTotal) != 0) {
            errors.add(String.format("价税合计错误，金额合计+税额合计-折扣合计=%s，实际价税合计=%s",
                    expectedGrandTotal, result.getGrandTotal()));
        }

        return errors;
    }
}
