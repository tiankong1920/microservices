package com.inventory.productservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品规格DTO.
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
public class ProductSpecDTO {

    private Long id;
    private Long productId;
    private String specName;
    private List<String> values;
}
