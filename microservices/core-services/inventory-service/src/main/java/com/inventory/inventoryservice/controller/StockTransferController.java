package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.StockTransferOrderDTO;
import com.inventory.inventoryservice.service.IStockTransferService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-transfers")
@RequiredArgsConstructor
@Tag(name = "Stock Transfer", description = "Stock Transfer Order Management API")
@Validated
public class StockTransferController {

    private final IStockTransferService stockTransferService;

    /**
     * 获取所有库存调拨单
     *
     * @return 库存调拨单列表
     */
    @GetMapping
    @Operation(summary = "获取所有库存调拨单")
    public ResponseEntity<List<StockTransferOrderDTO>> getAllStockTransferOrders() {
        final List<StockTransferOrderDTO> orders = stockTransferService.getAllStockTransferOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取库存调拨单
     *
     * @param id 库存调拨单ID
     * @return 库存调拨单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取库存调拨单")
    public ResponseEntity<StockTransferOrderDTO> getStockTransferOrderById(@PathVariable Long id) {
        final StockTransferOrderDTO order = stockTransferService.getStockTransferOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据调拨单号获取库存调拨单
     *
     * @param transferNumber 调拨单号
     * @return 库存调拨单信息
     */
    @GetMapping("/number/{transferNumber}")
    @Operation(summary = "根据调拨单号获取库存调拨单")
    public ResponseEntity<StockTransferOrderDTO> getStockTransferOrderByTransferNumber(
            @PathVariable String transferNumber) {
        final StockTransferOrderDTO order =
                stockTransferService.getStockTransferOrderByTransferNumber(transferNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据源仓库ID获取库存调拨单
     *
     * @param warehouseId 源仓库ID
     * @return 库存调拨单列表
     */
    @GetMapping("/source-warehouse/{warehouseId}")
    @Operation(summary = "根据源仓库ID获取库存调拨单")
    public ResponseEntity<List<StockTransferOrderDTO>> getStockTransferOrdersBySourceWarehouse(
            @PathVariable Long warehouseId) {
        final List<StockTransferOrderDTO> orders =
                stockTransferService.getStockTransferOrdersBySourceWarehouse(warehouseId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据目标仓库ID获取库存调拨单
     *
     * @param warehouseId 目标仓库ID
     * @return 库存调拨单列表
     */
    @GetMapping("/target-warehouse/{warehouseId}")
    @Operation(summary = "根据目标仓库ID获取库存调拨单")
    public ResponseEntity<List<StockTransferOrderDTO>> getStockTransferOrdersByTargetWarehouse(
            @PathVariable Long warehouseId) {
        final List<StockTransferOrderDTO> orders =
                stockTransferService.getStockTransferOrdersByTargetWarehouse(warehouseId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据状态获取库存调拨单
     *
     * @param status 调拨单状态
     * @return 库存调拨单列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取库存调拨单")
    public ResponseEntity<List<StockTransferOrderDTO>> getStockTransferOrdersByStatus(@PathVariable String status) {
        final List<StockTransferOrderDTO> orders = stockTransferService.getStockTransferOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建库存调拨单
     *
     * @param stockTransferOrderDTO 库存调拨单数据
     * @return 创建的库存调拨单信息
     */
    @PostMapping
    @Operation(summary = "创建库存调拨单")
    public ResponseEntity<StockTransferOrderDTO> createStockTransferOrder(
            @Valid @RequestBody StockTransferOrderDTO stockTransferOrderDTO) {
        final StockTransferOrderDTO createdOrder = stockTransferService.createStockTransferOrder(stockTransferOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 更新库存调拨单
     *
     * @param id 库存调拨单ID
     * @param stockTransferOrderDTO 更新后的库存调拨单数据
     * @return 更新后的库存调拨单信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新库存调拨单")
    public ResponseEntity<StockTransferOrderDTO> updateStockTransferOrder(
            @PathVariable Long id,
            @Valid @RequestBody StockTransferOrderDTO stockTransferOrderDTO) {
        final StockTransferOrderDTO updatedOrder =
                stockTransferService.updateStockTransferOrder(id, stockTransferOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除库存调拨单
     *
     * @param id 库存调拨单ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除库存调拨单")
    public ResponseEntity<Void> deleteStockTransferOrder(@PathVariable Long id) {
        stockTransferService.deleteStockTransferOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新库存调拨单状态
     *
     * @param id 库存调拨单ID
     * @param status 新状态
     * @return 更新后的库存调拨单信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新库存调拨单状态")
    public ResponseEntity<StockTransferOrderDTO> updateStockTransferOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        final StockTransferOrderDTO updatedOrder = stockTransferService.updateStockTransferOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 获取仓库调拨总数量
     *
     * @param warehouseId 仓库ID
     * @return 调拨总数量
     */
    @GetMapping("/total/{warehouseId}")
    @Operation(summary = "获取仓库调拨总数量")
    public ResponseEntity<Integer> getTotalTransferByWarehouse(@PathVariable Long warehouseId) {
        final Integer total = stockTransferService.getTotalTransferByWarehouse(warehouseId);
        return ResponseEntity.ok(total);
    }
}
