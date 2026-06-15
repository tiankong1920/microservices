package com.inventory.productservice.repository;

import com.inventory.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByName(String name);
    
    Optional<Product> findBySkuCode(String skuCode);
}
