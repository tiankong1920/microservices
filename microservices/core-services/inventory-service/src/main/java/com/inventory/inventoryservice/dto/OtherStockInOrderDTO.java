package com.inventory.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherStockInOrderDTO {
    private Long id;
    private String orderNumber;
    private Long warehouseId;
    private String warehouseName;
    private String orderType;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<OtherStockInItemDTO> items;
}
