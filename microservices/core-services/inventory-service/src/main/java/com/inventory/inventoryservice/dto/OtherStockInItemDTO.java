package com.inventory.inventoryservice.dto;

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
public class OtherStockInItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String batchNumber;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
