package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.SalesReturnOrderDTO;
import com.inventory.salesservice.service.ISalesReturnService;
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
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 销售退货控制器 - 管理销售退货订单的增删改查
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/sales-returns")
@RequiredArgsConstructor
@Tag(name = "Sales Return", description = "Sales Return Order Management API")
@Validated
public class SalesReturnController {

    private final ISalesReturnService salesReturnService;

    /**
     * 获取所有销售退货订单
     *
     * @return 销售退货订单列表
     */
    @GetMapping
    @Operation(summary = "获取所有销售退货订单")
    public ResponseEntity<List<SalesReturnOrderDTO>> getAllSalesReturnOrders() {
        final List<SalesReturnOrderDTO> orders = salesReturnService.getAllSalesReturnOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取销售退货订单
     *
     * @param id 退货订单ID
     * @return 销售退货订单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取销售退货订单")
    public ResponseEntity<SalesReturnOrderDTO> getSalesReturnOrderById(@PathVariable Long id) {
        final SalesReturnOrderDTO order = salesReturnService.getSalesReturnOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据退货单号获取销售退货订单
     *
     * @param returnNumber 退货单号
     * @return 销售退货订单信息
     */
    @GetMapping("/number/{returnNumber}")
    @Operation(summary = "根据退货单号获取销售退货订单")
    public ResponseEntity<SalesReturnOrderDTO> getSalesReturnOrderByReturnNumber(@PathVariable String returnNumber) {
        final SalesReturnOrderDTO order = salesReturnService.getSalesReturnOrderByReturnNumber(returnNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据客户ID获取销售退货订单
     *
     * @param customerId 客户ID
     * @return 销售退货订单列表
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "根据客户ID获取销售退货订单")
    public ResponseEntity<List<SalesReturnOrderDTO>> getSalesReturnOrdersByCustomerId(@PathVariable Long customerId) {
        final List<SalesReturnOrderDTO> orders = salesReturnService.getSalesReturnOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据仓库ID获取销售退货订单
     *
     * @param warehouseId 仓库ID
     * @return 销售退货订单列表
     */
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "根据仓库ID获取销售退货订单")
    public ResponseEntity<List<SalesReturnOrderDTO>> getSalesReturnOrdersByWarehouseId(@PathVariable Long warehouseId) {
        final List<SalesReturnOrderDTO> orders = salesReturnService.getSalesReturnOrdersByWarehouseId(warehouseId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据状态获取销售退货订单
     *
     * @param status 退货订单状态
     * @return 销售退货订单列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取销售退货订单")
    public ResponseEntity<List<SalesReturnOrderDTO>> getSalesReturnOrdersByStatus(@PathVariable String status) {
        final List<SalesReturnOrderDTO> orders = salesReturnService.getSalesReturnOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据原始订单ID获取销售退货订单
     *
     * @param originalOrderId 原始订单ID
     * @return 销售退货订单列表
     */
    @GetMapping("/original-order/{originalOrderId}")
    @Operation(summary = "根据原始订单ID获取销售退货订单")
    public ResponseEntity<List<SalesReturnOrderDTO>> getSalesReturnOrdersByOriginalOrderId(@PathVariable Long originalOrderId) {
        final List<SalesReturnOrderDTO> orders = salesReturnService.getSalesReturnOrdersByOriginalOrderId(originalOrderId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建销售退货订单
     *
     * @param salesReturnOrderDTO 退货订单数据
     * @return 创建的退货订单
     */
    @PostMapping
    @Operation(summary = "创建销售退货订单")
    public ResponseEntity<SalesReturnOrderDTO> createSalesReturnOrder(
            @RequestBody SalesReturnOrderDTO salesReturnOrderDTO) {
        final SalesReturnOrderDTO createdOrder = salesReturnService.createSalesReturnOrder(salesReturnOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 更新销售退货订单
     *
     * @param id 退货订单ID
     * @param salesReturnOrderDTO 更新后的订单数据
     * @return 更新后的退货订单
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新销售退货订单")
    public ResponseEntity<SalesReturnOrderDTO> updateSalesReturnOrder(
            @PathVariable Long id,
            @RequestBody SalesReturnOrderDTO salesReturnOrderDTO) {
        final SalesReturnOrderDTO updatedOrder = salesReturnService.updateSalesReturnOrder(id, salesReturnOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除销售退货订单
     *
     * @param id 退货订单ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除销售退货订单")
    public ResponseEntity<Void> deleteSalesReturnOrder(@PathVariable Long id) {
        salesReturnService.deleteSalesReturnOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新销售退货订单状态
     *
     * @param id 退货订单ID
     * @param status 新状态
     * @return 更新后的退货订单
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新销售退货订单状态")
    public ResponseEntity<SalesReturnOrderDTO> updateSalesReturnOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        final SalesReturnOrderDTO updatedOrder = salesReturnService.updateSalesReturnOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 获取客户退货总金额
     *
     * @param customerId 客户ID
     * @return 退货总金额
     */
    @GetMapping("/total/{customerId}")
    @Operation(summary = "获取客户退货总金额")
    public ResponseEntity<Double> getTotalReturnByCustomerId(@PathVariable Long customerId) {
        final Double total = salesReturnService.getTotalReturnByCustomerId(customerId);
        return ResponseEntity.ok(total);
    }
}
