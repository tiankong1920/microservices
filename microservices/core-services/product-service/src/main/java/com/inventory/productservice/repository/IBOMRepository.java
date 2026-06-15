package com.inventory.productservice.repository;

import com.inventory.productservice.entity.BOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IBOMRepository extends JpaRepository<BOM, Long> {
    Optional<BOM> findByBomCode(String bomCode);
    Optional<BOM> findByProductId(Long productId);
}
