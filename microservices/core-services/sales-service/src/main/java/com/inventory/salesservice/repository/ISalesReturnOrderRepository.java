package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.SalesReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISalesReturnOrderRepository extends JpaRepository<SalesReturnOrder, Long> {

    Optional<SalesReturnOrder> findByReturnNumber(String returnNumber);

    List<SalesReturnOrder> findByCustomerId(Long customerId);

    List<SalesReturnOrder> findByWarehouseId(Long warehouseId);

    List<SalesReturnOrder> findByStatus(String status);

    List<SalesReturnOrder> findByOriginalOrderId(Long originalOrderId);
}
