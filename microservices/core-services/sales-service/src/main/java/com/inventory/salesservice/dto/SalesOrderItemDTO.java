package com.inventory.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售订单项数据传输对象
 *
 * <p>用于在服务层和表示层之间传输销售订单项信息。
 * 包含销售订单项的基本信息，如产品、数量、单价、折扣等。</p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>创建销售订单项</li>
 *   <li>更新销售订单项信息</li>
 *   <li>查询销售订单项信息</li>
 *   <li>在服务间传递销售订单项数据</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see SalesOrderDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemDTO {

    /**
     * 销售订单项唯一标识符
     */
    private Long id;
    
    /**
     * 销售订单ID
     */
    private Long salesOrderId;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 产品名称
     */
    private String productName;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 折扣金额
     */
    private BigDecimal discount;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
