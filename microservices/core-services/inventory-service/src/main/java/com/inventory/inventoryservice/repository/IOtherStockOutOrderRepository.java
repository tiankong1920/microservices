package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.OtherStockOutOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOtherStockOutOrderRepository extends JpaRepository<OtherStockOutOrder, Long> {
    Optional<OtherStockOutOrder> findByOrderNumber(String orderNumber);
    List<OtherStockOutOrder> findByWarehouseId(Long warehouseId);
    List<OtherStockOutOrder> findByOrderType(String orderType);
    List<OtherStockOutOrder> findByStatus(String status);
}
