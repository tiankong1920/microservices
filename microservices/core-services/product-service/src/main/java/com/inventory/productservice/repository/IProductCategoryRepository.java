package com.inventory.productservice.repository;

import java.util.List;

import com.inventory.productservice.entity.ProductCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品分类Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByParentIdIsNullOrderBySortOrderAsc();

    List<ProductCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<ProductCategory> findByStatusOrderBySortOrderAsc(String status);

    List<ProductCategory> findByLevelOrderBySortOrderAsc(Integer level);
}
