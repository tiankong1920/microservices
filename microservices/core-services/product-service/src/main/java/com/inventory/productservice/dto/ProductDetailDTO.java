package com.inventory.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品详情DTO.
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
public class ProductDetailDTO {

    private Long id;
    private String productCode;
    private String name;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private String status;
    private String description;
    private String mainImage;
    private String videoUrl;
    private BigDecimal weight;
    private String dimensions;
    private Long createdBy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductSKUDTO> skus;
    private List<ProductSpecDTO> specs;
    private List<ProductImageDTO> images;
}
