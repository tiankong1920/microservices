package com.inventory.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存转移订单数据传输对象
 *
 * <p>用于在服务层和表示层之间传输库存转移订单信息。
 * 包含库存转移订单的基本信息，如转移单号、源仓库、目标仓库、转移日期等。</p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>创建库存转移订单</li>
 *   <li>更新库存转移订单信息</li>
 *   <li>查询库存转移订单信息</li>
 *   <li>在服务间传递库存转移订单数据</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see StockTransferItemDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferOrderDTO {

    /**
     * 库存转移订单唯一标识符
     */
    private Long id;
    
    /**
     * 转移单号
     */
    private String transferNumber;
    
    /**
     * 源仓库ID
     */
    private Long sourceWarehouseId;
    
    /**
     * 源仓库名称
     */
    private String sourceWarehouseName;
    
    /**
     * 目标仓库ID
     */
    private Long targetWarehouseId;
    
    /**
     * 目标仓库名称
     */
    private String targetWarehouseName;
    
    /**
     * 转移日期
     */
    private LocalDateTime transferDate;
    
    /**
     * 预期转移日期
     */
    private LocalDateTime expectedTransferDate;
    
    /**
     * 实际转移日期
     */
    private LocalDateTime actualTransferDate;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 总数量
     */
    private Integer totalQuantity;
    
    /**
     * 转移原因
     */
    private String reason;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 转移项列表
     */
    private List<StockTransferItemDTO> items;

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
    public List<StockTransferItemDTO> getItems() { return items; }

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
    public void setItems(List<StockTransferItemDTO> items) { this.items = items; }
}
