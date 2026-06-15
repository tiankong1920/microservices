package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IOrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByOrderStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    List<Order> findByOrderStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);

    @Query("SELECT o FROM Order o WHERE "
            + "(:status IS NULL OR o.orderStatus = :status) "
            + "AND (:startTime IS NULL OR o.createdAt >= :startTime) "
            + "AND (:endTime IS NULL OR o.createdAt <= :endTime) "
            + "AND (:minAmount IS NULL OR o.totalAmount >= :minAmount) "
            + "AND (:maxAmount IS NULL OR o.totalAmount <= :maxAmount) "
            + "ORDER BY o.createdAt DESC")
    Page<Order> searchOrders(@Param("status") String status,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime,
                              @Param("minAmount") BigDecimal minAmount,
                              @Param("maxAmount") BigDecimal maxAmount,
                              Pageable pageable);
}
