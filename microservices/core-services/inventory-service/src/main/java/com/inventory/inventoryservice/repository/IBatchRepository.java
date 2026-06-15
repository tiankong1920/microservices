package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByBatchCode(String batchCode);

    boolean existsByBatchCode(String batchCode);

    List<Batch> findByProductId(Long productId);

    List<Batch> findByWarehouseId(Long warehouseId);

    List<Batch> findBySupplierId(Long supplierId);

    List<Batch> findByStatus(String status);

    List<Batch> findByExpiryDateBefore(LocalDate date);

    List<Batch> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
