package com.inventory.productservice.service;

import com.inventory.productservice.dto.ProductCategoryDTO;

import java.util.List;

/**
 * 商品分类服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface ProductCategoryService {

    ProductCategoryDTO getCategoryById(Long id);

    List<ProductCategoryDTO> getCategoryTree();

    List<ProductCategoryDTO> getSubCategories(Long parentId);

    ProductCategoryDTO createCategory(ProductCategoryDTO dto);

    ProductCategoryDTO updateCategory(Long id, ProductCategoryDTO dto);

    void deleteCategory(Long id);

    ProductCategoryDTO updateCategoryStatus(Long id, String status);

    void moveCategory(Long id, Long newParentId);

    void sortCategories(List<Long> categoryIds);
}
