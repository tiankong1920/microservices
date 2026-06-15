package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.InvoiceCalculationRequest;
import com.invoice.invoiceservice.dto.InvoiceCalculationResult;
import com.invoice.invoiceservice.dto.InvoiceItemDTO;

public interface InvoiceCalculationService {

    InvoiceItemDTO calculateItemAmount(InvoiceItemDTO item);

    InvoiceCalculationResult calculateInvoice(InvoiceCalculationRequest request);

    boolean validateCalculation(InvoiceItemDTO item);

    List<String> validateInvoiceCalculation(InvoiceCalculationResult result);
}
