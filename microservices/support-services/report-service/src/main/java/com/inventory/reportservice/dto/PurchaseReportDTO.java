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
public class PurchaseReportDTO {
    private Long id;
    private String reportName;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalPurchaseAmount;
    private Long totalPurchaseQuantity;
    private Long totalOrders;
    private List<PurchaseByProductDTO> purchaseByProduct;
    private List<PurchaseByDateDTO> purchaseByDate;
    private List<PurchaseBySupplierDTO> purchaseBySupplier;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseByProductDTO {
        private Long productId;
        private String productName;
        private String productCode;
        private BigDecimal purchaseAmount;
        private Long purchaseQuantity;
        private Integer orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseByDateDTO {
        private LocalDateTime date;
        private BigDecimal purchaseAmount;
        private Long purchaseQuantity;
        private Long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseBySupplierDTO {
        private Long supplierId;
        private String supplierName;
        private BigDecimal purchaseAmount;
        private Long purchaseQuantity;
        private Long orderCount;
    }
}
