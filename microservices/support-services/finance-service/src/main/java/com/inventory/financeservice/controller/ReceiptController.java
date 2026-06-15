package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.ReceiptDTO;
import com.inventory.financeservice.service.IReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/finance/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipt", description = "收据管理接口")
@Validated
public class ReceiptController {

    private final IReceiptService receiptService;

    /**
     * 获取所有收款记录
     *
     * @return 收款记录列表
     */
    @GetMapping
    @Operation(summary = "获取所有收款记录")
    public ResponseEntity<List<ReceiptDTO>> getAllReceipts() {
        final List<ReceiptDTO> receipts = receiptService.getAllReceipts();
        return ResponseEntity.ok(receipts);
    }

    /**
     * 根据ID获取收款记录
     *
     * @param id 收款记录ID
     * @return 收款记录信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取收款记录")
    public ResponseEntity<ReceiptDTO> getReceiptById(@PathVariable Long id) {
        final ReceiptDTO receipt = receiptService.getReceiptById(id);
        return ResponseEntity.ok(receipt);
    }

    /**
     * 根据收据编号获取收款记录
     *
     * @param receiptNumber 收据编号
     * @return 收款记录信息
     */
    @GetMapping("/number/{receiptNumber}")
    @Operation(summary = "根据收据编号获取收款记录")
    public ResponseEntity<ReceiptDTO> getReceiptByReceiptNumber(@PathVariable String receiptNumber) {
        final ReceiptDTO receipt = receiptService.getReceiptByReceiptNumber(receiptNumber);
        return ResponseEntity.ok(receipt);
    }

    /**
     * 根据客户获取收款记录
     *
     * @param customerId 客户ID
     * @return 收款记录列表
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "根据客户获取收款记录")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByCustomerId(@PathVariable Long customerId) {
        final List<ReceiptDTO> receipts = receiptService.getReceiptsByCustomerId(customerId);
        return ResponseEntity.ok(receipts);
    }

    /**
     * 根据日期范围获取收款记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收款记录列表
     */
    @GetMapping("/date-range")
    @Operation(summary = "根据日期范围获取收款记录")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<ReceiptDTO> receipts = receiptService.getReceiptsByDateRange(startDate, endDate);
        return ResponseEntity.ok(receipts);
    }

    /**
     * 根据支付方式获取收款记录
     *
     * @param paymentMethod 支付方式
     * @return 收款记录列表
     */
    @GetMapping("/payment-method/{paymentMethod}")
    @Operation(summary = "根据支付方式获取收款记录")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByPaymentMethod(@PathVariable String paymentMethod) {
        final List<ReceiptDTO> receipts = receiptService.getReceiptsByPaymentMethod(paymentMethod);
        return ResponseEntity.ok(receipts);
    }

    /**
     * 根据收款状态获取收款记录
     *
     * @param receiptStatus 收款状态
     * @return 收款记录列表
     */
    @GetMapping("/status/{receiptStatus}")
    @Operation(summary = "根据收款状态获取收款记录")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByReceiptStatus(@PathVariable String receiptStatus) {
        final List<ReceiptDTO> receipts = receiptService.getReceiptsByReceiptStatus(receiptStatus);
        return ResponseEntity.ok(receipts);
    }

    /**
     * 创建收款记录
     *
     * @param receiptDTO 收款记录数据
     * @return 创建的收款记录信息
     */
    @PostMapping
    @Operation(summary = "创建收款记录")
    public ResponseEntity<ReceiptDTO> createReceipt(@Valid @RequestBody ReceiptDTO receiptDTO) {
        final ReceiptDTO createdReceipt = receiptService.createReceipt(receiptDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt);
    }

    /**
     * 更新收款记录
     *
     * @param id 收款记录ID
     * @param receiptDTO 更新后的收款数据
     * @return 更新后的收款记录信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新收款记录")
    public ResponseEntity<ReceiptDTO> updateReceipt(
            @PathVariable Long id,
            @Valid @RequestBody ReceiptDTO receiptDTO) {
        final ReceiptDTO updatedReceipt = receiptService.updateReceipt(id, receiptDTO);
        return ResponseEntity.ok(updatedReceipt);
    }

    /**
     * 删除收款记录
     *
     * @param id 收款记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收款记录")
    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        receiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新收款状态
     *
     * @param id 收款记录ID
     * @param receiptStatus 收款状态
     * @return 更新后的收款记录信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新收款状态")
    public ResponseEntity<ReceiptDTO> updateReceiptStatus(
            @PathVariable Long id,
            @RequestParam String receiptStatus) {
        final ReceiptDTO updatedReceipt = receiptService.updateReceiptStatus(id, receiptStatus);
        return ResponseEntity.ok(updatedReceipt);
    }

    /**
     * 获取客户收款总额
     *
     * @param customerId 客户ID
     * @return 收款总额
     */
    @GetMapping("/total/{customerId}")
    @Operation(summary = "获取客户收款总额")
    public ResponseEntity<java.math.BigDecimal> getTotalReceiptByCustomerId(@PathVariable Long customerId) {
        final java.math.BigDecimal total = receiptService.getTotalReceiptByCustomerId(customerId);
        return ResponseEntity.ok(total);
    }
}
