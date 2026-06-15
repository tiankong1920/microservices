package com.inventory.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 产品SKU数据传输对象.
 *
 * <p>用于在服务层和表示层之间传输产品SKU信息。
 * 包含产品SKU的基本信息，如SKU编码、属性、价格等。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("null")
public class ProductSKUDTO {

    /** SKU唯一标识符. */
    private Long id;

    /** 关联的产品ID. */
    private Long productId;

    /** SKU编码. */
    private String skuCode;

    /** SKU属性. */
    private String attributes;

    /** SKU价格. */
    private BigDecimal price;

    /** 是否激活. */
    private Boolean isActive;

    /** 创建时间. */
    private LocalDateTime createdAt;

    /** 更新时间. */
    private LocalDateTime updatedAt;
}
