package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.OrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单状态历史Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
