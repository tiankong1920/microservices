package com.inventory.productservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品分类DTO.
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
public class ProductCategoryDTO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductCategoryDTO> children;
}
