package com.invoice.invoiceservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class InvoiceItemDTO {

    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private String productName;

    private String specification;

    private String unitName;

    @NotNull(message = "单价不能为空")
    @PositiveOrZero(message = "单价不能为负数")
    private BigDecimal unitPrice;

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须为正数")
    private BigDecimal quantity;

    @NotNull(message = "税率不能为空")
    @Positive(message = "税率必须为正数")
    private BigDecimal taxRate;

    private BigDecimal amount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    @PositiveOrZero(message = "折扣金额不能为负数")
    private BigDecimal discountAmount;

    private BigDecimal discountRate;
}
