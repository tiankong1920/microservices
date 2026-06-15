package com.inventory.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次数据传输对象
 *
 * <p>用于在服务层和表示层之间传输批次信息。
 * 包含批次的基本信息，如批次编码、产品、仓库、供应商、数量、成本、生产日期、过期日期等。</p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>创建新批次</li>
 *   <li>更新现有批次信息</li>
 *   <li>查询批次信息</li>
 *   <li>在服务间传递批次数据</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDTO {

    /**
     * 批次唯一标识符
     */
    private Long id;
    
    /**
     * 批次编码
     */
    private String batchCode;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 仓库ID
     */
    private Long warehouseId;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 批次数量
     */
    private Integer quantity;
    
    /**
     * 可用数量
     */
    private Integer availableQuantity;
    
    /**
     * 单位成本
     */
    private BigDecimal unitCost;
    
    /**
     * 总成本
     */
    private BigDecimal totalCost;
    
    /**
     * 生产日期
     */
    private LocalDate manufactureDate;
    
    /**
     * 过期日期
     */
    private LocalDate expiryDate;
    
    /**
     * 批次位置
     */
    private String location;
    
    /**
     * 批次状态
     */
    private String status;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 产品名称
     */
    private String productName;
    
    /**
     * 仓库名称
     */
    private String warehouseName;
    
    /**
     * 供应商名称
     */
    private String supplierName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

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
    public String getProductName() { return productName; }
    public String getWarehouseName() { return warehouseName; }
    public String getSupplierName() { return supplierName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

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
    public void setProductName(String productName) { this.productName = productName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
