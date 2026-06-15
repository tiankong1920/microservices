package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.Order;
import com.inventory.mallservice.entity.OrderItem;
import com.inventory.mallservice.entity.ShoppingCart;
import com.inventory.mallservice.exception.OrderException;
import com.inventory.mallservice.repository.IOrderItemRepository;
import com.inventory.mallservice.repository.IOrderRepository;
import com.inventory.mallservice.repository.IShoppingCartRepository;
import com.inventory.mallservice.service.IOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IShoppingCartRepository shoppingCartRepository;

    @Override
    @Transactional
    public Order createOrder(final Long userId, final List<Long> cartItemIds,
                              final Long couponId, final String notes) {
        log.info("Creating order for user: {} from cart items: {}", userId, cartItemIds);

        List<ShoppingCart> cartItems = shoppingCartRepository.findAllById(cartItemIds);
        if (cartItems.isEmpty()) {
            throw OrderException.noCartItems();
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (ShoppingCart cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.setSkuId(cartItem.getSkuId());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(BigDecimal.ZERO);
            item.setSubtotal(BigDecimal.ZERO);
            item.setProductName("");
            item.setSkuAttributes("{}");
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("PENDING");
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setCouponId(couponId);
        order.setNotes(notes);
        order.setCustomerName("");
        order.setCustomerPhone("");
        order.setCustomerAddress("");

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        shoppingCartRepository.deleteAllById(cartItemIds);

        log.info("Order created successfully with orderNo: {}", savedOrder.getOrderNo());
        return savedOrder;
    }

    @Override
    @Transactional
    public Order createDirectOrder(final Long userId, final Long skuId, final Integer quantity,
                                    final Long couponId, final String notes) {
        log.info("Creating direct order for user: {}, sku: {}, quantity: {}", userId, skuId, quantity);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setActualAmount(BigDecimal.ZERO);
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("PENDING");
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setCouponId(couponId);
        order.setNotes(notes);
        order.setCustomerName("");
        order.setCustomerPhone("");
        order.setCustomerAddress("");

        Order savedOrder = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(savedOrder);
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        item.setUnitPrice(BigDecimal.ZERO);
        item.setSubtotal(BigDecimal.ZERO);
        item.setProductName("");
        item.setSkuAttributes("{}");
        orderItemRepository.save(item);

        log.info("Direct order created successfully with orderNo: {}", savedOrder.getOrderNo());
        return savedOrder;
    }

    @Override
    public Order getOrderById(final Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Order", id));
    }

    @Override
    public Order getOrderByOrderNo(final String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Order", "orderNo", orderNo));
    }

    @Override
    public Page<Order> getOrdersByUserId(final Long userId, final Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(final String status, final Pageable pageable) {
        return orderRepository.findByOrderStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<Order> searchOrders(final String status, final LocalDateTime startTime,
                                     final LocalDateTime endTime, final BigDecimal minAmount,
                                     final BigDecimal maxAmount, final Pageable pageable) {
        return orderRepository.searchOrders(status, startTime, endTime, minAmount, maxAmount, pageable);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(final Long id, final String newStatus, final String notes) {
        log.info("Updating order {} status to {}", id, newStatus);
        Order order = getOrderById(id);
        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        if ("PAID".equals(newStatus)) {
            order.setPaymentStatus("PAID");
            order.setPaymentTime(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(final Long id, final String reason) {
        log.info("Cancelling order: {}, reason: {}", id, reason);
        Order order = getOrderById(id);
        if (!"PENDING".equals(order.getOrderStatus()) && !"PAID".equals(order.getOrderStatus())) {
            throw OrderException.cannotCancel(order.getOrderStatus());
        }
        order.setOrderStatus("CANCELLED");
        order.setNotes(reason);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order confirmReceipt(final Long id) {
        log.info("Confirming receipt for order: {}", id);
        Order order = getOrderById(id);
        if (!"SHIPPED".equals(order.getOrderStatus())) {
            throw OrderException.mustBeShipped();
        }
        order.setOrderStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getExpiredUnpaidOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
        return orderRepository.findByOrderStatusAndCreatedAtBefore("PENDING", expireTime);
    }

    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }
}
