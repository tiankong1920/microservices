package com.inventory.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transfer_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_order_id", nullable = false)
    private Long transferOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
