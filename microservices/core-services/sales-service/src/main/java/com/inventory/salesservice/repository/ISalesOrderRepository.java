package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ISalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    List<SalesOrder> findByCustomerId(Long customerId);
    List<SalesOrder> findByStatus(String status);
    @Query("SELECT o FROM SalesOrder o JOIN FETCH o.items")
    List<SalesOrder> findAllWithItems();
    List<SalesOrder> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
