package com.inventory.productservice.repository;

import com.inventory.productservice.entity.ProductSKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductSKURepository extends JpaRepository<ProductSKU, Long> {
    Optional<ProductSKU> findBySkuCode(String skuCode);
    List<ProductSKU> findAllByProductId(Long productId);
    List<ProductSKU> findAllByProductIdAndIsActiveTrue(Long productId);
}
