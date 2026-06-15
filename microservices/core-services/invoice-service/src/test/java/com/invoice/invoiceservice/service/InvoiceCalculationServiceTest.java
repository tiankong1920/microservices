package com.invoice.invoiceservice.service;

import java.math.BigDecimal;

import com.invoice.invoiceservice.dto.InvoiceCalculationRequest;
import com.invoice.invoiceservice.dto.InvoiceCalculationResult;
import com.invoice.invoiceservice.dto.InvoiceItemDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceCalculationServiceTest {

    private InvoiceCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new com.invoice.invoiceservice.service.impl.InvoiceCalculationServiceImpl();
    }

    @Test
    void testCalculateItemAmount13Percent() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .productName("测试商品")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("2"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("200.00"), result.getAmount());
        assertEquals(new BigDecimal("26.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("226.00"), result.getTotalAmount());
    }

    @Test
    void testCalculateItemAmount9Percent() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("1000.00"))
                .quantity(new BigDecimal("1"))
                .taxRate(new BigDecimal("9"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(new BigDecimal("90.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("1090.00"), result.getTotalAmount());
    }

    @Test
    void testCalculateItemAmount6Percent() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("500.00"))
                .quantity(new BigDecimal("3"))
                .taxRate(new BigDecimal("6"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("1500.00"), result.getAmount());
        assertEquals(new BigDecimal("90.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("1590.00"), result.getTotalAmount());
    }

    @Test
    void testCalculateItemAmount3Percent() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("200.00"))
                .quantity(new BigDecimal("5"))
                .taxRate(new BigDecimal("3"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(new BigDecimal("30.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("1030.00"), result.getTotalAmount());
    }

    @Test
    void testCalculateItemWithDiscount() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("10"))
                .taxRate(new BigDecimal("13"))
                .discountAmount(new BigDecimal("50.00"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(new BigDecimal("130.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("1080.00"), result.getTotalAmount());
    }

    @Test
    void testCalculateInvoiceMultipleItems() {
        InvoiceItemDTO item1 = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("2"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO item2 = InvoiceItemDTO.builder()
                .productId(2L)
                .unitPrice(new BigDecimal("200.00"))
                .quantity(new BigDecimal("1"))
                .taxRate(new BigDecimal("9"))
                .build();

        InvoiceCalculationRequest request =
                InvoiceCalculationRequest.builder()
                        .items(java.util.List.of(item1, item2))
                        .build();

        InvoiceCalculationResult result = calculationService.calculateInvoice(request);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(new BigDecimal("400.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("44.00"), result.getTotalTaxAmount());
        assertEquals(new BigDecimal("444.00"), result.getGrandTotal());
    }

    @Test
    void testValidateCalculationCorrect() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("2"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO calculated = calculationService.calculateItemAmount(item);
        assertTrue(calculationService.validateCalculation(calculated));
    }

    @Test
    void testValidateCalculationIncorrect() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("2"))
                .taxRate(new BigDecimal("13"))
                .amount(new BigDecimal("999.00"))
                .taxAmount(new BigDecimal("26.00"))
                .totalAmount(new BigDecimal("226.00"))
                .build();

        assertFalse(calculationService.validateCalculation(item));
    }

    @Test
    void testRoundingHalfUp() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("99.995"))
                .quantity(new BigDecimal("1"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("13.00"), result.getTaxAmount());
    }

    @Test
    void testLargeQuantityCalculation() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("0.01"))
                .quantity(new BigDecimal("10000"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("13.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("113.00"), result.getTotalAmount());
    }

    @Test
    void testZeroDiscountCalculation() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("1"))
                .taxRate(new BigDecimal("13"))
                .discountAmount(BigDecimal.ZERO)
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("113.00"), result.getTotalAmount());
    }

    @Test
    void testNullDiscountCalculation() {
        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .productId(1L)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(new BigDecimal("1"))
                .taxRate(new BigDecimal("13"))
                .build();

        InvoiceItemDTO result = calculationService.calculateItemAmount(item);

        assertEquals(new BigDecimal("113.00"), result.getTotalAmount());
    }
}
