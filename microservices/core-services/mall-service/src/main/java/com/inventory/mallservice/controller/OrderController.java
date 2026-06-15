package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Order;
import com.inventory.mallservice.service.IOrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 订单Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/orders")
@Tag(name = "Mall Order", description = "商城订单管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 从购物车创建订单
     *
     * @param request 包含userId、cartItemIds、couponId和notes的请求数据
     * @return 创建的订单信息
     */
    @PostMapping("/from-cart")
    public ResponseEntity<Order> createOrderFromCart(
            @Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Long> cartItemIds = (List<Long>) request.get("cartItemIds");
        Long couponId = request.get("couponId") != null
                ? Long.valueOf(request.get("couponId").toString()) : null;
        String notes = (String) request.getOrDefault("notes", "");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(userId, cartItemIds, couponId, notes));
    }

    /**
     * 直接创建订单
     *
     * @param request 包含userId、skuId、quantity、couponId和notes的请求数据
     * @return 创建的订单信息
     */
    @PostMapping("/direct")
    public ResponseEntity<Order> createDirectOrder(
            @Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long skuId = Long.valueOf(request.get("skuId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        Long couponId = request.get("couponId") != null
                ? Long.valueOf(request.get("couponId").toString()) : null;
        String notes = (String) request.getOrDefault("notes", "");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createDirectOrder(userId, skuId, quantity, couponId, notes));
    }

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable final Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * 根据订单号获取订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/no/{orderNo}")
    public ResponseEntity<Order> getOrderByNo(@PathVariable final String orderNo) {
        return ResponseEntity.ok(orderService.getOrderByOrderNo(orderNo));
    }

    /**
     * 获取用户的订单列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 用户的订单分页列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Order>> getUserOrders(
            @PathVariable final Long userId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }

    /**
     * 根据状态获取订单列表
     *
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable final String status,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    /**
     * 搜索订单
     *
     * @param status 订单状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param minAmount 最小金额（可选）
     * @param maxAmount 最大金额（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Order>> searchOrders(
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final LocalDateTime endTime,
            @RequestParam(required = false) final BigDecimal minAmount,
            @RequestParam(required = false) final BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.searchOrders(
                status, startTime, endTime, minAmount, maxAmount, pageable));
    }

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param request 包含status和notes的请求数据
     * @return 更新后的订单信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(
                id, request.get("status"), request.get("notes")));
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param request 包含reason的请求数据
     * @return 取消后的订单信息
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(orderService.cancelOrder(id, request.get("reason")));
    }

    /**
     * 确认收货
     *
     * @param id 订单ID
     * @return 确认收货后的订单信息
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmReceipt(@PathVariable final Long id) {
        return ResponseEntity.ok(orderService.confirmReceipt(id));
    }
}
