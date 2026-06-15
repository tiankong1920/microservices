package com.inventory.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferItemDTO {

    private Long id;
    private Long transferOrderId;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private String unit;
    private String batchNumber;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public Long getTransferOrderId() { return transferOrderId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductSku() { return productSku; }
    public Integer getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getBatchNumber() { return batchNumber; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTransferOrderId(Long transferOrderId) { this.transferOrderId = transferOrderId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public void setReason(String reason) { this.reason = reason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
