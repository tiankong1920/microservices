package com.inventory.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单数据传输对象
 *
 * <p>用于在服务层和表示层之间传输销售订单信息。
 * 包含销售订单的基本信息，如订单编号、客户、订单日期、金额等。</p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>创建新销售订单</li>
 *   <li>更新现有销售订单信息</li>
 *   <li>查询销售订单信息</li>
 *   <li>在服务间传递销售订单数据</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see SalesOrderItemDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDTO {

    /**
     * 销售订单唯一标识符
     */
    private Long id;

    /**
     * 订单编号
     */
    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    private String orderNumber;

    /**
     * 客户ID
     */
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /**
     * 客户名称
     */
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    /**
     * 订单日期
     */
    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    /**
     * 交付日期
     */
    private LocalDateTime deliveryDate;

    /**
     * 订单状态
     */
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    /**
     * 小计金额
     */
    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    /**
     * 税额
     */
    @NotNull(message = "Tax is required")
    private BigDecimal tax;

    /**
     * 折扣金额
     */
    @NotNull(message = "Discount is required")
    private BigDecimal discount;

    /**
     * 总金额
     */
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    /**
     * 备注
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    /**
     * 订单项列表
     */
    private List<SalesOrderItemDTO> items;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
