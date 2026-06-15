package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ISalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);

    List<SalesOrderItem> findByProductId(Long productId);

    @Transactional
    void deleteBySalesOrderId(Long salesOrderId);
}
