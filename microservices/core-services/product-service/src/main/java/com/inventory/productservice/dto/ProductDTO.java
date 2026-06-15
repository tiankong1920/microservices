package com.inventory.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品数据传输对象（DTO）.
 *
 * <p>用于在应用层之间传输产品数据。
 * 该类是Product实体类的数据传输对象，用于服务层、控制器层和前端之间的数据交换。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("null")
public class ProductDTO {

    /** 产品ID. */
    private Long id;

    /** 产品名称. */
    private String name;

    /** 产品描述. */
    private String description;

    /** 产品SKU代码. */
    private String sku;

    /** 产品价格. */
    private BigDecimal price;

    /** 库存数量. */
    private Integer stockQuantity;

    /** 产品状态. */
    private String status;

    /** 是否激活. */
    private Boolean isActive;

    /** 创建时间. */
    private LocalDateTime createdAt;

    /** 更新时间. */
    private LocalDateTime updatedAt;

    /** 产品SKU列表. */
    private List<ProductSKUDTO> skus;
}
