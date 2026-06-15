package com.invoice.invoiceservice.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCalculationResult {

    private List<InvoiceItemDTO> items;
    private BigDecimal totalAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal grandTotal;
    private BigDecimal totalDiscountAmount;
    private boolean valid;
    private List<String> validationErrors;
}
