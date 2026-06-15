package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.RetailOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IRetailOrderItemRepository extends JpaRepository<RetailOrderItem, Long> {

    List<RetailOrderItem> findByRetailOrderId(Long retailOrderId);

    List<RetailOrderItem> findByProductId(Long productId);

    @Transactional
    void deleteByRetailOrderId(Long retailOrderId);
}
