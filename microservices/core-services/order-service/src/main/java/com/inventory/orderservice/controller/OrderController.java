package com.inventory.orderservice.controller;

import com.inventory.orderservice.dto.OrderDTO;
import com.inventory.orderservice.dto.OrderItemDTO;
import com.inventory.orderservice.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order Management API")
@Validated
public class OrderController {

    private final IOrderService orderService;

    /**
     * 获取所有订单
     *
     * @return 所有订单列表
     */
    @GetMapping
    @Operation(summary = "获取所有订单", description = "获取系统中所有订单的完整列表")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("Request received to get all orders");
        final List<OrderDTO> orders = orderService.getAllOrders();
        log.info("Retrieved {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取订单
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取订单", description = "根据唯一标识符获取单个订单")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        log.info("Request received to get order with id: {}", id);
        final OrderDTO order = orderService.getOrderById(id);
        log.info("Retrieved order: {}", order);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据订单号获取订单
     *
     * @param orderNumber 订单号
     * @return 订单信息
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "根据订单号获取订单", description = "根据唯一订单号获取订单")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Request received to get order with number: {}", orderNumber);
        final OrderDTO order = orderService.findOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据客户名称查找订单
     *
     * @param customerName 客户名称
     * @return 订单列表
     */
    @GetMapping("/customer/{customerName}")
    @Operation(summary = "根据客户名称查找订单", description = "根据客户名称搜索订单（部分匹配）")
    public ResponseEntity<List<OrderDTO>> findOrdersByCustomer(@PathVariable String customerName) {
        log.info("Request received to find orders for customer: {}", customerName);
        final List<OrderDTO> orders = orderService.findOrdersByCustomerName(customerName);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据状态查找订单
     *
     * @param status 订单状态
     * @return 订单列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查找订单", description = "根据状态搜索订单")
    public ResponseEntity<List<OrderDTO>> findOrdersByStatus(@PathVariable String status) {
        log.info("Request received to find orders with status: {}", status);
        final List<OrderDTO> orders = orderService.findOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取有效状态转换
     *
     * @param id 订单ID
     * @return 有效状态转换列表
     */
    @GetMapping("/{id}/transitions")
    @Operation(summary = "获取有效状态转换", description = "返回订单的有效状态转换列表")
    public ResponseEntity<List<String>> getValidTransitions(@PathVariable Long id) {
        log.info("Request received to get valid transitions for order: {}", id);
        final List<String> transitions = orderService.getValidStatusTransitions(id);
        return ResponseEntity.ok(transitions);
    }

    /**
     * 创建订单
     *
     * @param orderDTO 订单数据
     * @return 创建的订单信息
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "使用提供的信息在系统中创建新订单")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.info("Request received to create order");
        final OrderDTO createdOrder = orderService.createOrder(orderDTO);
        log.info("Created order with id: {}", createdOrder.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 更新订单
     *
     * @param id 订单ID
     * @param orderDTO 更新后的订单数据
     * @return 更新后的订单信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新订单", description = "使用提供的信息更新现有订单")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id,
                                                  @Valid @RequestBody OrderDTO orderDTO) {
        log.info("Request received to update order with id: {}", id);
        final OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
        log.info("Updated order: {}", updatedOrder);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param status 新状态
     * @return 更新后的订单信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新现有订单的状态")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id,
                                                      @RequestParam String status) {
        log.info("Request received to update order {} status to: {}", id, status);
        final OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @return 取消后的订单信息
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消现有订单并恢复库存")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        log.info("Request received to cancel order: {}", id);
        final OrderDTO cancelledOrder = orderService.cancelOrder(id);
        log.info("Order cancelled: {}", id);
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * 向订单添加商品
     *
     * @param id 订单ID
     * @param itemDTO 订单项数据
     * @return 更新后的订单信息
     */
    @PostMapping("/{id}/items")
    @Operation(summary = "向订单添加商品", description = "向现有订单添加新商品")
    public ResponseEntity<OrderDTO> addItemToOrder(@PathVariable Long id,
                                                    @Valid @RequestBody OrderItemDTO itemDTO) {
        log.info("Request received to add item to order: {}", id);
        final OrderDTO updatedOrder = orderService.addItemToOrder(id, itemDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 从订单移除商品
     *
     * @param orderId 订单ID
     * @param itemId 订单项ID
     * @return 更新后的订单信息
     */
    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "从订单移除商品", description = "从订单中移除商品并恢复库存")
    public ResponseEntity<OrderDTO> removeItemFromOrder(@PathVariable Long orderId,
                                                        @PathVariable Long itemId) {
        log.info("Request received to remove item {} from order: {}", itemId, orderId);
        final OrderDTO updatedOrder = orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除订单
     *
     * @param id 订单ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "根据唯一标识符从系统中删除订单")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("Request received to delete order with id: {}", id);
        orderService.deleteOrder(id);
        log.info("Deleted order with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
