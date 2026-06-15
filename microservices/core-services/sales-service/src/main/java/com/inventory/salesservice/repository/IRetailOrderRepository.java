package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.RetailOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRetailOrderRepository extends JpaRepository<RetailOrder, Long> {

    Optional<RetailOrder> findByRetailNumber(String retailNumber);

    List<RetailOrder> findByCustomerId(Long customerId);

    List<RetailOrder> findByWarehouseId(Long warehouseId);

    List<RetailOrder> findByPaymentStatus(String paymentStatus);

    List<RetailOrder> findByRetailDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
