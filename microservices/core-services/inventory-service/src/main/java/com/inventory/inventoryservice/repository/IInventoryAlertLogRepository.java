package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.InventoryAlertLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IInventoryAlertLogRepository extends JpaRepository<InventoryAlertLog, Long> {

    List<InventoryAlertLog> findByProductId(Long productId);

    List<InventoryAlertLog> findByAlertType(String alertType);

    List<InventoryAlertLog> findByAcknowledged(boolean acknowledged);

    Page<InventoryAlertLog> findByAcknowledged(boolean acknowledged, Pageable pageable);

    List<InventoryAlertLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    List<InventoryAlertLog> findByAlertTypeAndAcknowledged(String alertType, boolean acknowledged);
}
