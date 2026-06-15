package com.inventory.productservice.repository;

import com.inventory.productservice.entity.ProductAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品审核Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IProductAuditRepository extends JpaRepository<ProductAudit, Long> {

    List<ProductAudit> findByProductIdOrderByCreatedAtDesc(Long productId);
}
