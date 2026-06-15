package com.inventory.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存数据传输对象
 *
 * <p>用于在服务层和表示层之间传输库存信息。
 * 包含库存的基本信息，如产品、仓库、批次、数量、成本等。</p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>创建新库存记录</li>
 *   <li>更新现有库存信息</li>
 *   <li>查询库存信息</li>
 *   <li>在服务间传递库存数据</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    /**
     * 库存唯一标识符
     */
    private Long id;

    /**
     * 产品ID
     */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /**
     * 仓库ID
     */
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 库存数量
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    /**
     * 可用数量
     */
    private Integer availableQuantity;

    /**
     * 预留数量
     */
    private Integer reservedQuantity;

    /**
     * 单位成本
     */
    @Positive(message = "Unit cost must be positive")
    private BigDecimal unitCost;

    /**
     * 总价值
     */
    private BigDecimal totalValue;

    /**
     * 库存位置
     */
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    /**
     * 库存状态
     */
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 批次编码
     */
    private String batchCode;
}
