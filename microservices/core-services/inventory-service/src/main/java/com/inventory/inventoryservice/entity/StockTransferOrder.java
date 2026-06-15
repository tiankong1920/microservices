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
@Table(name = "stock_transfer_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_number", nullable = false, unique = true, length = 50)
    private String transferNumber;

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "source_warehouse_name", length = 200)
    private String sourceWarehouseName;

    @Column(name = "target_warehouse_id", nullable = false)
    private Long targetWarehouseId;

    @Column(name = "target_warehouse_name", length = 200)
    private String targetWarehouseName;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(name = "expected_transfer_date")
    private LocalDateTime expectedTransferDate;

    @Column(name = "actual_transfer_date")
    private LocalDateTime actualTransferDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

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
    public String getTransferNumber() { return transferNumber; }
    public Long getSourceWarehouseId() { return sourceWarehouseId; }
    public String getSourceWarehouseName() { return sourceWarehouseName; }
    public Long getTargetWarehouseId() { return targetWarehouseId; }
    public String getTargetWarehouseName() {
        return targetWarehouseName;
    }
    public LocalDateTime getTransferDate() { return transferDate; }
    public LocalDateTime getExpectedTransferDate() { return expectedTransferDate; }
    public LocalDateTime getActualTransferDate() { return actualTransferDate; }
    public String getStatus() { return status; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }
    public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public void setSourceWarehouseName(String sourceWarehouseName) { this.sourceWarehouseName = sourceWarehouseName; }
    public void setTargetWarehouseId(Long targetWarehouseId) { this.targetWarehouseId = targetWarehouseId; }
    public void setTargetWarehouseName(String targetWarehouseName) {
        this.targetWarehouseName = targetWarehouseName;
    }
    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }
    public void setExpectedTransferDate(LocalDateTime expectedTransferDate) {
        this.expectedTransferDate = expectedTransferDate;
    }
    public void setActualTransferDate(LocalDateTime actualTransferDate) {
        this.actualTransferDate = actualTransferDate;
    }
    public void setStatus(String status) { this.status = status; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public void setReason(String reason) { this.reason = reason; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transferDate == null) {
            transferDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
