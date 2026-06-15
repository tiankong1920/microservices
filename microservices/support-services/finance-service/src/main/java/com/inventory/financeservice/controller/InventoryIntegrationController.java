package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.InventorySyncDTO;
import com.inventory.financeservice.service.IInventoryIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/inventory")
@Tag(name = "Inventory Integration", description = "进销存系统集成接口")
@RequiredArgsConstructor
@Validated
public class InventoryIntegrationController {
    private final IInventoryIntegrationService inventoryIntegrationService;

    /**
     * 同步销售数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 无内容响应
     */
    @PostMapping("/sync/sales")
    @Operation(summary = "同步销售数据")
    public ResponseEntity<Void> syncSalesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        inventoryIntegrationService.syncSalesData(startDate, endDate);
        return ResponseEntity.ok().build();
    }

    /**
     * 同步采购数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 无内容响应
     */
    @PostMapping("/sync/purchase")
    @Operation(summary = "同步采购数据")
    public ResponseEntity<Void> syncPurchaseData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        inventoryIntegrationService.syncPurchaseData(startDate, endDate);
        return ResponseEntity.ok().build();
    }

    /**
     * 同步库存调整数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 无内容响应
     */
    @PostMapping("/sync/adjustment")
    @Operation(summary = "同步库存调整数据")
    public ResponseEntity<Void> syncInventoryAdjustment(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        inventoryIntegrationService.syncInventoryAdjustment(startDate, endDate);
        return ResponseEntity.ok().build();
    }

    /**
     * 同步所有进销存数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果
     */
    @PostMapping("/sync/all")
    @Operation(summary = "同步所有进销存数据")
    public ResponseEntity<Map<String, Object>> synchronizeAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        IInventoryIntegrationService.SyncResult result = inventoryIntegrationService.synchronizeAll(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "salesRecordsSynced", result.getSalesRecordsSynced(),
                "purchaseRecordsSynced", result.getPurchaseRecordsSynced(),
                "adjustmentsSynced", result.getAdjustmentsSynced(),
                "receivablesCreated", result.getReceivablesCreated(),
                "payablesCreated", result.getPayablesCreated(),
                "errors", result.getErrors(),
                "success", result.isSuccess()
        ));
    }

    /**
     * 从销售订单创建应收账款
     *
     * @param salesOrderId 销售订单ID
     * @return 无内容响应
     */
    @PostMapping("/receivable/from-sales")
    @Operation(summary = "从销售订单创建应收账款")
    public ResponseEntity<Void> createAccountsReceivableFromSales(
            @RequestParam String salesOrderId) {
        inventoryIntegrationService.createAccountsReceivableFromSales(salesOrderId);
        return ResponseEntity.ok().build();
    }

    /**
     * 从采购订单创建应付账款
     *
     * @param purchaseOrderId 采购订单ID
     * @return 无内容响应
     */
    @PostMapping("/payable/from-purchase")
    @Operation(summary = "从采购订单创建应付账款")
    public ResponseEntity<Void> createAccountsPayableFromPurchase(
            @RequestParam String purchaseOrderId) {
        inventoryIntegrationService.createAccountsPayableFromPurchase(purchaseOrderId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取同步历史记录
     *
     * @param syncType 同步类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步历史列表
     */
    @GetMapping("/sync-history")
    @Operation(summary = "获取同步历史记录")
    public ResponseEntity<List<InventorySyncDTO>> getSyncHistory(
            @RequestParam(required = false) String syncType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<InventorySyncDTO> history = inventoryIntegrationService.getSyncHistory(syncType, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * 处理库存预警
     *
     * @param alertType 预警类型
     * @param productId 产品ID
     * @param quantity 数量
     * @param threshold 阈值
     * @return 无内容响应
     */
    @PostMapping("/alert")
    @Operation(summary = "处理库存预警")
    public ResponseEntity<Void> processInventoryAlert(
            @RequestParam String alertType,
            @RequestParam String productId,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal threshold) {
        inventoryIntegrationService.processInventoryAlert(alertType, productId, quantity, threshold);
        return ResponseEntity.ok().build();
    }
}
