package com.invoice.invoiceservice.controller;

import com.invoice.invoiceservice.dto.InvoiceCalculationRequest;
import com.invoice.invoiceservice.dto.InvoiceCalculationResult;
import com.invoice.invoiceservice.dto.InvoiceItemDTO;
import com.invoice.invoiceservice.service.InvoiceCalculationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice/calculation")
@RequiredArgsConstructor
@Tag(name = "Invoice Calculation", description = "金额自动计算API")
@Validated
public class InvoiceCalculationController {

    private final InvoiceCalculationService calculationService;

    /**
     * 计算单个商品条目金额
     *
     * @param item 商品条目数据
     * @return 计算后的商品条目
     */
    @PostMapping("/item")
    @Operation(summary = "计算单个商品条目金额")
    public ResponseEntity<InvoiceItemDTO> calculateItem(@Valid @RequestBody InvoiceItemDTO item) {
        InvoiceItemDTO result = calculationService.calculateItemAmount(item);
        return ResponseEntity.ok(result);
    }

    /**
     * 计算整张发票金额（含多商品条目汇总）
     *
     * @param request 发票计算请求数据
     * @return 发票计算结果
     */
    @PostMapping("/invoice")
    @Operation(summary = "计算整张发票金额（含多商品条目汇总）")
    public ResponseEntity<InvoiceCalculationResult> calculateInvoice(
            @Valid @RequestBody InvoiceCalculationRequest request) {
        InvoiceCalculationResult result = calculationService.calculateInvoice(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 校验金额计算结果
     *
     * @param item 商品条目数据
     * @return 校验是否通过
     */
    @PostMapping("/validate")
    @Operation(summary = "校验金额计算结果")
    public ResponseEntity<Boolean> validateCalculation(@Valid @RequestBody InvoiceItemDTO item) {
        boolean valid = calculationService.validateCalculation(item);
        return ResponseEntity.ok(valid);
    }
}
