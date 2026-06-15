package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.BatchDTO;
import com.inventory.inventoryservice.dto.InventoryDTO;
import com.inventory.inventoryservice.dto.WarehouseDTO;
import com.inventory.inventoryservice.service.IBatchService;
import com.inventory.inventoryservice.service.IInventoryService;
import com.inventory.inventoryservice.service.IWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存控制器 - 管理库存操作
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Inventory Management API")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(InventoryController.class);

    private final IInventoryService inventoryService;
    private final IWarehouseService warehouseService;
    private final IBatchService batchService;

    /**
     * 获取所有库存记录
     *
     * @return 所有库存记录列表
     */
    @GetMapping
    @Operation(summary = "获取所有库存记录",
            description = "检索系统中所有库存记录的完整列表")
    public ResponseEntity<List<InventoryDTO>> getAllInventory() {
        LOG.info("Getting all inventory records");
        final List<InventoryDTO> inventoryList = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventoryList);
    }

    /**
     * 根据ID获取库存记录
     *
     * @param id 库存记录ID
     * @return 库存记录信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取库存记录",
            description = "通过唯一标识符检索单个库存记录")
    public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable Long id) {
        LOG.info("Getting inventory by ID: {}", id);
        final InventoryDTO inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * 根据产品ID获取库存记录
     *
     * @param productId 产品ID
     * @return 产品的库存记录列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "根据产品ID获取库存记录",
            description = "检索特定产品的所有库存记录")
    public ResponseEntity<List<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        LOG.info("Getting inventory by product ID: {}", productId);
        final List<InventoryDTO> inventoryList = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventoryList);
    }

    /**
     * 根据仓库ID获取库存记录
     *
     * @param warehouseId 仓库ID
     * @return 仓库的库存记录列表
     */
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "根据仓库ID获取库存记录",
            description = "检索特定仓库的所有库存记录")
    public ResponseEntity<List<InventoryDTO>> getInventoryByWarehouseId(@PathVariable Long warehouseId) {
        LOG.info("Getting inventory by warehouse ID: {}", warehouseId);
        final List<InventoryDTO> inventoryList = inventoryService.getInventoryByWarehouseId(warehouseId);
        return ResponseEntity.ok(inventoryList);
    }

    /**
     * 创建库存记录
     *
     * @param inventoryDTO 库存数据
     * @return 创建的库存记录信息
     */
    @PostMapping
    @Operation(summary = "创建库存记录",
            description = "使用提供的详细信息在系统中创建新的库存记录")
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        LOG.info("Creating new inventory: {}", inventoryDTO);
        final InventoryDTO createdInventory = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
    }

    /**
     * 更新库存记录
     *
     * @param id 库存记录ID
     * @param inventoryDTO 更新后的库存数据
     * @return 更新后的库存记录信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新库存记录",
            description = "使用提供的详细信息更新现有库存记录")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDTO inventoryDTO) {
        LOG.info("Updating inventory with ID: {}", id);
        final InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.ok(updatedInventory);
    }

    /**
     * 删除库存记录
     *
     * @param id 库存记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除库存记录",
            description = "通过唯一标识符从系统中删除库存记录")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        LOG.info("Deleting inventory with ID: {}", id);
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 预留库存
     *
     * @param productId 产品ID
     * @param warehouseId 仓库ID
     * @param quantity 预留数量
     * @return 预留后剩余可用数量
     */
    @PostMapping("/reserve")
    @Operation(summary = "预留库存",
            description = "为仓库中的产品预留指定数量的库存")
    public ResponseEntity<Integer> reserveInventory(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        LOG.info("Reserving {} units of product: {} in warehouse: {}", quantity, productId, warehouseId);
        final Integer availableQuantity = inventoryService.reserveInventory(productId, warehouseId, quantity);
        return ResponseEntity.ok(availableQuantity);
    }

    /**
     * 释放预留库存
     *
     * @param productId 产品ID
     * @param warehouseId 仓库ID
     * @param quantity 释放数量
     * @return 释放后剩余可用数量
     */
    @PostMapping("/release")
    @Operation(summary = "释放预留库存",
            description = "释放仓库中产品先前预留的库存数量")
    public ResponseEntity<Integer> releaseInventory(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        LOG.info("Releasing {} units of product: {} in warehouse: {}", quantity, productId, warehouseId);
        final Integer availableQuantity = inventoryService.releaseInventory(productId, warehouseId, quantity);
        return ResponseEntity.ok(availableQuantity);
    }

    /**
     * 调整库存数量
     *
     * @param productId 产品ID
     * @param warehouseId 仓库ID
     * @param quantity 调整数量（正数增加，负数减少）
     * @return 调整后更新的可用数量
     */
    @PostMapping("/adjust")
    @Operation(summary = "调整库存数量",
            description = "按指定数量调整仓库中产品的库存数量，正值增加，负值减少")
    public ResponseEntity<Integer> adjustInventory(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        LOG.info("Adjusting inventory by {} units for product: {} in warehouse: {}", quantity, productId, warehouseId);
        final Integer availableQuantity = inventoryService.adjustInventory(productId, warehouseId, quantity);
        return ResponseEntity.ok(availableQuantity);
    }

    /**
     * 获取所有仓库
     *
     * @return 所有仓库列表
     */
    @GetMapping("/warehouses")
    @Operation(summary = "获取所有仓库",
            description = "检索系统中所有仓库的完整列表")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        LOG.info("Getting all warehouses");
        final List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    /**
     * 获取所有批次
     *
     * @return 所有批次列表
     */
    @GetMapping("/batches")
    @Operation(summary = "获取所有批次",
            description = "检索系统中所有批次的完整列表")
    public ResponseEntity<List<BatchDTO>> getAllBatches() {
        LOG.info("Getting all batches");
        final List<BatchDTO> batches = batchService.getAllBatches();
        return ResponseEntity.ok(batches);
    }

    /**
 * Retrieves all batches that are expiring before a specified date.
     *
 * @param beforeDate the date before which batches are considered expiring
 * @return a list of expiring batches as BatchDTO objects
     */
    @GetMapping("/batches/expiring")
    @Operation(summary = "获取即将过期批次",
            description = "检索在指定日期之前即将过期的所有批次")
    public ResponseEntity<List<BatchDTO>> getExpiringBatches(@RequestParam LocalDate beforeDate) {
        LOG.info("Getting expiring batches before date: {}", beforeDate);
        final List<BatchDTO> batches = batchService.getBatchesExpiringBefore(beforeDate);
        return ResponseEntity.ok(batches);
    }

}
