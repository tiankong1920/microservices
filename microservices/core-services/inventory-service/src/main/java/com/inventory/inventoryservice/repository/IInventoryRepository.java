package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByBatchId(Long batchId);

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByStatus(String status);
}
