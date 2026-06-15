package com.inventory.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品图片DTO.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductImageDTO {

    private Long id;
    private Long productId;
    private String imageUrl;
    private Integer sortOrder;
}
