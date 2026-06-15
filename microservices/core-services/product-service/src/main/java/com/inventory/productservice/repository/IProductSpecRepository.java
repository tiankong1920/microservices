package com.inventory.productservice.repository;

import com.inventory.productservice.entity.ProductSpec;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品规格Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    List<ProductSpec> findByProductId(Long productId);
}
