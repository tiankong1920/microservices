package com.inventory.salesservice.controller;

import com.inventory.salesservice.dto.RetailOrderDTO;
import com.inventory.salesservice.service.IRetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/retail")
@RequiredArgsConstructor
@Tag(name = "Retail", description = "Retail Order Management API")
@Validated
public class RetailController {

    private final IRetailService retailService;

    /**
     * 获取所有零售订单
     *
     * @return 零售订单列表
     */
    @GetMapping
    @Operation(summary = "获取所有零售订单")
    public ResponseEntity<List<RetailOrderDTO>> getAllRetailOrders() {
        final List<RetailOrderDTO> orders = retailService.getAllRetailOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取零售订单
     *
     * @param id 零售订单ID
     * @return 零售订单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取零售订单")
    public ResponseEntity<RetailOrderDTO> getRetailOrderById(@PathVariable Long id) {
        final RetailOrderDTO order = retailService.getRetailOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据零售单号获取零售订单
     *
     * @param retailNumber 零售单号
     * @return 零售订单信息
     */
    @GetMapping("/number/{retailNumber}")
    @Operation(summary = "根据零售单号获取零售订单")
    public ResponseEntity<RetailOrderDTO> getRetailOrderByRetailNumber(@PathVariable String retailNumber) {
        final RetailOrderDTO order = retailService.getRetailOrderByRetailNumber(retailNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据客户ID获取零售订单
     *
     * @param customerId 客户ID
     * @return 零售订单列表
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "根据客户ID获取零售订单")
    public ResponseEntity<List<RetailOrderDTO>> getRetailOrdersByCustomerId(@PathVariable Long customerId) {
        final List<RetailOrderDTO> orders = retailService.getRetailOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据仓库ID获取零售订单
     *
     * @param warehouseId 仓库ID
     * @return 零售订单列表
     */
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "根据仓库ID获取零售订单")
    public ResponseEntity<List<RetailOrderDTO>> getRetailOrdersByWarehouseId(@PathVariable Long warehouseId) {
        final List<RetailOrderDTO> orders = retailService.getRetailOrdersByWarehouseId(warehouseId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据支付状态获取零售订单
     *
     * @param paymentStatus 支付状态
     * @return 零售订单列表
     */
    @GetMapping("/payment-status/{paymentStatus}")
    @Operation(summary = "根据支付状态获取零售订单")
    public ResponseEntity<List<RetailOrderDTO>> getRetailOrdersByPaymentStatus(@PathVariable String paymentStatus) {
        final List<RetailOrderDTO> orders = retailService.getRetailOrdersByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据日期范围获取零售订单
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 零售订单列表
     */
    @GetMapping("/date-range")
    @Operation(summary = "根据日期范围获取零售订单")
    public ResponseEntity<List<RetailOrderDTO>> getRetailOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<RetailOrderDTO> orders = retailService.getRetailOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建零售订单
     *
     * @param retailOrderDTO 零售订单数据
     * @return 创建的零售订单信息
     */
    @PostMapping
    @Operation(summary = "创建零售订单")
    public ResponseEntity<RetailOrderDTO> createRetailOrder(
            @Valid @RequestBody RetailOrderDTO retailOrderDTO) {
        final RetailOrderDTO createdOrder = retailService.createRetailOrder(retailOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 更新零售订单
     *
     * @param id 零售订单ID
     * @param retailOrderDTO 更新后的零售订单数据
     * @return 更新后的零售订单信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新零售订单")
    public ResponseEntity<RetailOrderDTO> updateRetailOrder(
            @PathVariable Long id,
            @Valid @RequestBody RetailOrderDTO retailOrderDTO) {
        final RetailOrderDTO updatedOrder = retailService.updateRetailOrder(id, retailOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除零售订单
     *
     * @param id 零售订单ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除零售订单")
    public ResponseEntity<Void> deleteRetailOrder(@PathVariable Long id) {
        retailService.deleteRetailOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新零售订单支付状态
     *
     * @param id 零售订单ID
     * @param paymentStatus 支付状态
     * @return 更新后的零售订单信息
     */
    @PatchMapping("/{id}/payment-status")
    @Operation(summary = "更新零售订单支付状态")
    public ResponseEntity<RetailOrderDTO> updateRetailOrderPaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus) {
        final RetailOrderDTO updatedOrder = retailService.updateRetailOrderPaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 获取客户零售总金额
     *
     * @param customerId 客户ID
     * @return 零售总金额
     */
    @GetMapping("/total/{customerId}")
    @Operation(summary = "获取客户零售总金额")
    public ResponseEntity<Double> getTotalRetailByCustomerId(@PathVariable Long customerId) {
        final Double total = retailService.getTotalRetailByCustomerId(customerId);
        return ResponseEntity.ok(total);
    }
}
