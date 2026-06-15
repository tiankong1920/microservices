package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.InventoryAlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInventoryAlertThresholdRepository extends JpaRepository<InventoryAlertThreshold, Long> {

    List<InventoryAlertThreshold> findByProductId(Long productId);

    List<InventoryAlertThreshold> findByWarehouseId(Long warehouseId);

    Optional<InventoryAlertThreshold> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<InventoryAlertThreshold> findByEnabled(boolean enabled);

    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
