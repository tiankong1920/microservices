package com.inventory.productservice.repository;

import com.inventory.productservice.entity.ProductImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品图片Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    @Transactional
    void deleteByProductId(Long productId);
}
