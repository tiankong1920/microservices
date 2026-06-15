package com.inventory.salesservice.dto;

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
public class RetailOrderDTO {

    private Long id;
    private String retailNumber;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime retailDate;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<RetailOrderItemDTO> items;
}
