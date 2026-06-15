package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.StockTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IStockTransferItemRepository extends JpaRepository<StockTransferItem, Long> {

    List<StockTransferItem> findByTransferOrderId(Long transferOrderId);

    List<StockTransferItem> findByProductId(Long productId);

    @Transactional
    void deleteByTransferOrderId(Long transferOrderId);
}
