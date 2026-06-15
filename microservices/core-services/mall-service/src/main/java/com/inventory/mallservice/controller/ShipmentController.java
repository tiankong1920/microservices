package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Shipment;
import com.inventory.mallservice.service.IShipmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 物流Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/shipments")
@Tag(name = "Shipment", description = "发货管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class ShipmentController {

    private final IShipmentService shipmentService;

    /**
     * 创建发货记录
     *
     * @param request 包含orderId、logisticsCompany和trackingNumber的请求数据
     * @return 创建的发货记录信息
     */
    @PostMapping
    public ResponseEntity<Shipment> createShipment(
            @Valid @RequestBody final Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String logisticsCompany = (String) request.get("logisticsCompany");
        String trackingNumber = (String) request.get("trackingNumber");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipmentService.createShipment(orderId, logisticsCompany, trackingNumber));
    }

    /**
     * 获取发货详情
     *
     * @param id 发货ID
     * @return 发货记录信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipment(@PathVariable final Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    /**
     * 根据订单ID获取发货信息
     *
     * @param orderId 订单ID
     * @return 发货记录信息
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable final Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }

    /**
     * 分页获取发货列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 发货分页列表
     */
    @GetMapping
    public ResponseEntity<Page<Shipment>> getShipments(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(shipmentService.getShipments(pageable));
    }

    /**
     * 根据状态获取发货列表
     *
     * @param status 发货状态
     * @return 发货列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shipment>> getShipmentsByStatus(
            @PathVariable final String status) {
        return ResponseEntity.ok(shipmentService.getShipmentsByStatus(status));
    }

    /**
     * 更新物流单号
     *
     * @param id 发货ID
     * @param request 包含trackingNumber的请求数据
     * @return 更新后的发货记录信息
     */
    @PutMapping("/{id}/tracking")
    public ResponseEntity<Shipment> updateTrackingNumber(
            @PathVariable final Long id,
            @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(
                shipmentService.updateTrackingNumber(id, request.get("trackingNumber")));
    }

    /**
     * 更新发货状态
     *
     * @param id 发货ID
     * @param request 包含status的请求数据
     * @return 更新后的发货记录信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable final Long id,
            @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(
                shipmentService.updateShipmentStatus(id, request.get("status")));
    }

    /**
     * 同步物流信息
     *
     * @param id 发货ID
     * @return 同步结果
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, String>> syncLogisticsInfo(
            @PathVariable final Long id) {
        shipmentService.syncLogisticsInfo(id);
        return ResponseEntity.ok(Map.of("message", "物流信息同步已触发"));
    }
}
