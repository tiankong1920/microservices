package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IOrderService {

    Order createOrder(Long userId, List<Long> cartItemIds, Long couponId, String notes);

    Order createDirectOrder(Long userId, Long skuId, Integer quantity, Long couponId, String notes);

    Order getOrderById(Long id);

    Order getOrderByOrderNo(String orderNo);

    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);

    Page<Order> getOrdersByStatus(String status, Pageable pageable);

    Page<Order> searchOrders(String status, LocalDateTime startTime, LocalDateTime endTime,
                             BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    Order updateOrderStatus(Long id, String newStatus, String notes);

    Order cancelOrder(Long id, String reason);

    Order confirmReceipt(Long id);

    List<Order> getExpiredUnpaidOrders();
}
