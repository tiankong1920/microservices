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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", nullable = false, unique = true, length = 50)
    private String batchCode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 19, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "location")
    private String location;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters
    public Long getId() { return id; }
    public String getBatchCode() { return batchCode; }
    public Long getProductId() { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getSupplierId() { return supplierId; }
    public Integer getQuantity() { return quantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (availableQuantity == null) {
            availableQuantity = quantity;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
