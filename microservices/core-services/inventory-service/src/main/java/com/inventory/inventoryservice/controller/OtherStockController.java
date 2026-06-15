package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.OtherStockInOrderDTO;
import com.inventory.inventoryservice.dto.OtherStockOutOrderDTO;
import com.inventory.inventoryservice.service.IOtherStockService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/other-stock")
@RequiredArgsConstructor
@Tag(name = "Other Stock", description = "Other Stock Order Management API")
@Validated
public class OtherStockController {

    private final IOtherStockService otherStockService;

    /**
     * 获取所有其他入库单
     *
     * @return 其他入库单列表
     */
    @GetMapping("/in")
    @Operation(summary = "获取所有其他入库单")
    public ResponseEntity<List<OtherStockInOrderDTO>> getAllOtherStockInOrders() {
        final List<OtherStockInOrderDTO> orders = otherStockService.getAllOtherStockInOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取所有其他出库单
     *
     * @return 其他出库单列表
     */
    @GetMapping("/out")
    @Operation(summary = "获取所有其他出库单")
    public ResponseEntity<List<OtherStockOutOrderDTO>> getAllOtherStockOutOrders() {
        final List<OtherStockOutOrderDTO> orders = otherStockService.getAllOtherStockOutOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取其他入库单
     *
     * @param id 其他入库单ID
     * @return 其他入库单信息
     */
    @GetMapping("/in/{id}")
    @Operation(summary = "根据ID获取其他入库单")
    public ResponseEntity<OtherStockInOrderDTO> getOtherStockInOrderById(@PathVariable Long id) {
        final OtherStockInOrderDTO order = otherStockService.getOtherStockInOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据ID获取其他出库单
     *
     * @param id 其他出库单ID
     * @return 其他出库单信息
     */
    @GetMapping("/out/{id}")
    @Operation(summary = "根据ID获取其他出库单")
    public ResponseEntity<OtherStockOutOrderDTO> getOtherStockOutOrderById(@PathVariable Long id) {
        final OtherStockOutOrderDTO order = otherStockService.getOtherStockOutOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 创建其他入库单
     *
     * @param orderDTO 其他入库单数据
     * @return 创建的其他入库单信息
     */
    @PostMapping("/in")
    @Operation(summary = "创建其他入库单")
    public ResponseEntity<OtherStockInOrderDTO> createOtherStockInOrder(
            @Valid @RequestBody OtherStockInOrderDTO orderDTO) {
        final OtherStockInOrderDTO createdOrder = otherStockService.createOtherStockInOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 创建其他出库单
     *
     * @param orderDTO 其他出库单数据
     * @return 创建的其他出库单信息
     */
    @PostMapping("/out")
    @Operation(summary = "创建其他出库单")
    public ResponseEntity<OtherStockOutOrderDTO> createOtherStockOutOrder(
            @Valid @RequestBody OtherStockOutOrderDTO orderDTO) {
        final OtherStockOutOrderDTO createdOrder = otherStockService.createOtherStockOutOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * 删除其他入库单
     *
     * @param id 其他入库单ID
     * @return 无内容响应
     */
    @DeleteMapping("/in/{id}")
    @Operation(summary = "删除其他入库单")
    public ResponseEntity<Void> deleteOtherStockInOrder(@PathVariable Long id) {
        otherStockService.deleteOtherStockInOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除其他出库单
     *
     * @param id 其他出库单ID
     * @return 无内容响应
     */
    @DeleteMapping("/out/{id}")
    @Operation(summary = "删除其他出库单")
    public ResponseEntity<Void> deleteOtherStockOutOrder(@PathVariable Long id) {
        otherStockService.deleteOtherStockOutOrder(id);
        return ResponseEntity.noContent().build();
    }
}
