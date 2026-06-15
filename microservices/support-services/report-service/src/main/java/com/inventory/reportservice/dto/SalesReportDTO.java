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
public class SalesReportDTO {
    private Long id;
    private String reportName;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalSalesAmount;
    private Long totalSalesQuantity;
    private Long totalOrders;
    private List<SalesByProductDTO> salesByProduct;
    private List<SalesByDateDTO> salesByDate;
    private List<SalesByCustomerDTO> salesByCustomer;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesByProductDTO {
        private Long productId;
        private String productName;
        private String productCode;
        private BigDecimal salesAmount;
        private Long salesQuantity;
        private Integer orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesByDateDTO {
        private LocalDateTime date;
        private BigDecimal salesAmount;
        private Long salesQuantity;
        private Long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesByCustomerDTO {
        private Long customerId;
        private String customerName;
        private BigDecimal salesAmount;
        private Long salesQuantity;
        private Long orderCount;
    }
}
