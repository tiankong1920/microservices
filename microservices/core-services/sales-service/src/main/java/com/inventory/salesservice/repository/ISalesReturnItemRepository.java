package com.inventory.salesservice.repository;

import com.inventory.salesservice.entity.SalesReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ISalesReturnItemRepository extends JpaRepository<SalesReturnItem, Long> {

    List<SalesReturnItem> findByReturnOrderId(Long returnOrderId);

    List<SalesReturnItem> findByProductId(Long productId);

    @Transactional
    void deleteByReturnOrderId(Long returnOrderId);
}
