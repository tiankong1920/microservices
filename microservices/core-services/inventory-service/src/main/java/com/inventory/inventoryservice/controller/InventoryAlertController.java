package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.InventoryAlertDTO;
import com.inventory.inventoryservice.dto.InventoryAlertThresholdDTO;
import com.inventory.inventoryservice.entity.InventoryAlertLog;
import com.inventory.inventoryservice.service.InventoryAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/alerts")
@RequiredArgsConstructor
@Tag(name = "Inventory Alerts", description = "Inventory Alert Management API")
@Validated
public class InventoryAlertController {

    private final InventoryAlertService alertService;

    /**
     * 创建或更新告警阈值
     *
     * @param dto 告警阈值数据
     * @return 创建或更新的告警阈值信息
     */
    @PostMapping("/thresholds")
    @Operation(summary = "创建或更新告警阈值")
    public ResponseEntity<InventoryAlertThresholdDTO> createOrUpdateThreshold(
            @Valid @RequestBody final InventoryAlertThresholdDTO dto) {
        return ResponseEntity.ok(alertService.createOrUpdateThreshold(dto));
    }

    /**
     * 获取所有告警阈值
     *
     * @return 告警阈值列表
     */
    @GetMapping("/thresholds")
    @Operation(summary = "获取所有告警阈值")
    public ResponseEntity<List<InventoryAlertThresholdDTO>> getAllThresholds() {
        return ResponseEntity.ok(alertService.getAllThresholds());
    }

    /**
     * 检查库存并发送告警
     *
     * @param inventoryId 库存ID
     * @return 告警信息（如有）
     */
    @PostMapping("/check/{inventoryId}")
    @Operation(summary = "检查库存并发送告警")
    public ResponseEntity<InventoryAlertDTO> checkAndAlert(@PathVariable final Long inventoryId) {
        InventoryAlertDTO alert = alertService.checkAndAlert(inventoryId);
        if (alert == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alert);
    }

    /**
     * 检查所有库存并发送告警
     *
     * @return 所有告警列表
     */
    @PostMapping("/check-all")
    @Operation(summary = "检查所有库存并发送告警")
    public ResponseEntity<List<InventoryAlertDTO>> checkAllAlerts() {
        return ResponseEntity.ok(alertService.checkAndAlertAll());
    }

    /**
     * 获取未确认的告警
     *
     * @return 未确认的告警列表
     */
    @GetMapping("/unacknowledged")
    @Operation(summary = "获取未确认的告警")
    public ResponseEntity<List<InventoryAlertLog>> getUnacknowledgedAlerts() {
        return ResponseEntity.ok(alertService.getUnacknowledgedAlerts());
    }

    /**
     * 确认告警
     *
     * @param alertId 告警ID
     * @param acknowledgedBy 确认人
     * @return 无内容响应
     */
    @PatchMapping("/{alertId}/acknowledge")
    @Operation(summary = "确认告警")
    public ResponseEntity<Void> acknowledgeAlert(
            @PathVariable final Long alertId,
            @RequestParam(defaultValue = "system") final String acknowledgedBy) {
        alertService.acknowledgeAlert(alertId, acknowledgedBy);
        return ResponseEntity.ok().build();
    }
}
