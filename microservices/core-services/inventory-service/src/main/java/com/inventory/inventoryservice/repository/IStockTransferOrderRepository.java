package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.StockTransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStockTransferOrderRepository extends JpaRepository<StockTransferOrder, Long> {

    Optional<StockTransferOrder> findByTransferNumber(String transferNumber);

    List<StockTransferOrder> findBySourceWarehouseId(Long sourceWarehouseId);

    List<StockTransferOrder> findByTargetWarehouseId(Long targetWarehouseId);

    List<StockTransferOrder> findByStatus(String status);
}
