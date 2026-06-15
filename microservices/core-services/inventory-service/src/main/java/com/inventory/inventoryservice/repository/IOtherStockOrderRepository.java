package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.OtherStockInOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOtherStockOrderRepository extends JpaRepository<OtherStockInOrder, Long> {
    Optional<OtherStockInOrder> findByOrderNumber(String orderNumber);
    List<OtherStockInOrder> findByWarehouseId(Long warehouseId);
    List<OtherStockInOrder> findByOrderType(String orderType);
    List<OtherStockInOrder> findByStatus(String status);
}
