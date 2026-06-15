package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.SalesOrderDTO;
import com.inventory.salesservice.service.ISalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Order", description = "Sales Order Management API")
@Validated
public class SalesOrderController {

    private final ISalesOrderService salesOrderService;

    /**
     * 获取所有销售订单
     *
     * @return 销售订单列表
     */
    @GetMapping
    @Operation(summary = "获取所有销售订单")
    public ResponseEntity<List<SalesOrderDTO>> getAllSalesOrders() {
        final List<SalesOrderDTO> orders = salesOrderService.getAllSalesOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取销售订单
     *
     * @param id 销售订单ID
     * @return 销售订单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取销售订单")
    public ResponseEntity<SalesOrderDTO> getSalesOrderById(@PathVariable Long id) {
        final SalesOrderDTO order = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据订单号获取销售订单
     *
     * @param orderNumber 订单号
     * @return 销售订单信息
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "根据订单号获取销售订单")
    public ResponseEntity<SalesOrderDTO> getSalesOrderByOrderNumber(@PathVariable String orderNumber) {
        final SalesOrderDTO order = salesOrderService.getSalesOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据客户ID获取销售订单
     *
     * @param customerId 客户ID
     * @return 销售订单列表
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "根据客户ID获取销售订单")
    public ResponseEntity<List<SalesOrderDTO>> getSalesOrdersByCustomerId(@PathVariable Long customerId) {
        final List<SalesOrderDTO> orders = salesOrderService.getSalesOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据状态获取销售订单
     *
     * @param status 订单状态
     * @return 销售订单列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取销售订单")
    public ResponseEntity<List<SalesOrderDTO>> getSalesOrdersByStatus(@PathVariable String status) {
        final List<SalesOrderDTO> orders = salesOrderService.getSalesOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据日期范围获取销售订单
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售订单列表
     */
    @GetMapping("/date-range")
    @Operation(summary = "根据日期范围获取销售订单")
    public ResponseEntity<List<SalesOrderDTO>> getSalesOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<SalesOrderDTO> orders = salesOrderService.getSalesOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建销售订单
     *
     * @param salesOrderDTO 销售订单数据
     * @return 创建的销售订单信息
     */
    @PostMapping
    @Operation(summary = "创建销售订单")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderDTO salesOrderDTO) {
        final SalesOrderDTO createdOrder = salesOrderService.createSalesOrder(salesOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 更新销售订单
     *
     * @param id 销售订单ID
     * @param salesOrderDTO 更新后的销售订单数据
     * @return 更新后的销售订单信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新销售订单")
    public ResponseEntity<SalesOrderDTO> updateSalesOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderDTO salesOrderDTO) {
        final SalesOrderDTO updatedOrder = salesOrderService.updateSalesOrder(id, salesOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除销售订单
     *
     * @param id 销售订单ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除销售订单")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新销售订单状态
     *
     * @param id 销售订单ID
     * @param status 新状态
     * @return 更新后的销售订单信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新销售订单状态")
    public ResponseEntity<SalesOrderDTO> updateSalesOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        final SalesOrderDTO updatedOrder = salesOrderService.updateSalesOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 获取客户销售总金额
     *
     * @param customerId 客户ID
     * @return 销售总金额
     */
    @GetMapping("/total/{customerId}")
    @Operation(summary = "获取客户销售总金额")
    public ResponseEntity<Double> getTotalSalesByCustomerId(@PathVariable Long customerId) {
        final Double total = salesOrderService.getTotalSalesByCustomerId(customerId);
        return ResponseEntity.ok(total);
    }
}
