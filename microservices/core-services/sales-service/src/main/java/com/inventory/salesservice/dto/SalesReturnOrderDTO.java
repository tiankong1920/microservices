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
public class SalesReturnOrderDTO {

    private Long id;
    private String returnNumber;
    private Long originalOrderId;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime returnDate;
    private LocalDateTime expectedRefundDate;
    private LocalDateTime actualRefundDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<SalesReturnItemDTO> items;
}
