package com.inventory.reportservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReportDTO {
    private Long id;
    private String reportName;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long totalProducts;
    private Long totalStockQuantity;
    private BigDecimal totalStockValue;
    private List<InventoryByProductDTO> inventoryByProduct;
    private List<InventoryByWarehouseDTO> inventoryByWarehouse;
    private List<InventoryMovementDTO> inventoryMovements;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryByProductDTO {
        private Long productId;
        private String productName;
        private String productCode;
        private Long currentQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
        private String category;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryByWarehouseDTO {
        private Long warehouseId;
        private String warehouseName;
        private Long totalProducts;
        private Long totalQuantity;
        private BigDecimal totalValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryMovementDTO {
        private Long movementId;
        private LocalDateTime movementDate;
        private String movementType;
        private Long productId;
        private String productName;
        private Long quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String sourceWarehouse;
        private String targetWarehouse;
        private String reason;
    }
}
