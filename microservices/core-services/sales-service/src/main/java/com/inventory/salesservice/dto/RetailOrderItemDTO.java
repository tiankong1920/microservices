package com.inventory.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetailOrderItemDTO {

    private Long id;
    private Long retailOrderId;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxRate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String batchNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
