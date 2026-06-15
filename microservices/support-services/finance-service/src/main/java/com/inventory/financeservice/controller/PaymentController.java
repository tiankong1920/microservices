package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.PaymentDTO;
import com.inventory.financeservice.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "付款管理接口")
@Validated
public class PaymentController {

    private final IPaymentService paymentService;

    /**
     * 获取所有付款记录
     *
     * @return 付款记录列表
     */
    @GetMapping
    @Operation(summary = "获取所有付款记录")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        final List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * 根据ID获取付款记录
     *
     * @param id 付款记录ID
     * @return 付款记录信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取付款记录")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        final PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    /**
     * 根据付款编号获取付款记录
     *
     * @param paymentNumber 付款编号
     * @return 付款记录信息
     */
    @GetMapping("/number/{paymentNumber}")
    @Operation(summary = "根据付款编号获取付款记录")
    public ResponseEntity<PaymentDTO> getPaymentByPaymentNumber(@PathVariable String paymentNumber) {
        final PaymentDTO payment = paymentService.getPaymentByPaymentNumber(paymentNumber);
        return ResponseEntity.ok(payment);
    }

    /**
     * 根据供应商获取付款记录
     *
     * @param supplierId 供应商ID
     * @return 付款记录列表
     */
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "根据供应商获取付款记录")
    public ResponseEntity<List<PaymentDTO>> getPaymentsBySupplierId(@PathVariable Long supplierId) {
        final List<PaymentDTO> payments = paymentService.getPaymentsBySupplierId(supplierId);
        return ResponseEntity.ok(payments);
    }

    /**
     * 根据日期范围获取付款记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 付款记录列表
     */
    @GetMapping("/date-range")
    @Operation(summary = "根据日期范围获取付款记录")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        final List<PaymentDTO> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    /**
     * 根据付款方式获取付款记录
     *
     * @param paymentMethod 付款方式
     * @return 付款记录列表
     */
    @GetMapping("/payment-method/{paymentMethod}")
    @Operation(summary = "根据付款方式获取付款记录")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPaymentMethod(@PathVariable String paymentMethod) {
        final List<PaymentDTO> payments = paymentService.getPaymentsByPaymentMethod(paymentMethod);
        return ResponseEntity.ok(payments);
    }

    /**
     * 根据付款状态获取付款记录
     *
     * @param paymentStatus 付款状态
     * @return 付款记录列表
     */
    @GetMapping("/status/{paymentStatus}")
    @Operation(summary = "根据付款状态获取付款记录")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPaymentStatus(@PathVariable String paymentStatus) {
        final List<PaymentDTO> payments = paymentService.getPaymentsByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(payments);
    }

    /**
     * 创建付款记录
     *
     * @param paymentDTO 付款记录数据
     * @return 创建的付款记录信息
     */
    @PostMapping
    @Operation(summary = "创建付款记录")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        final PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    /**
     * 更新付款记录
     *
     * @param id 付款记录ID
     * @param paymentDTO 更新后的付款数据
     * @return 更新后的付款记录信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新付款记录")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        final PaymentDTO updatedPayment = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * 删除付款记录
     *
     * @param id 付款记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除付款记录")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新付款状态
     *
     * @param id 付款记录ID
     * @param paymentStatus 付款状态
     * @return 更新后的付款记录信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新付款状态")
    public ResponseEntity<PaymentDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus) {
        final PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * 获取供应商付款总额
     *
     * @param supplierId 供应商ID
     * @return 付款总额
     */
    @GetMapping("/total/{supplierId}")
    @Operation(summary = "获取供应商付款总额")
    public ResponseEntity<BigDecimal> getTotalPaymentBySupplierId(@PathVariable Long supplierId) {
        final BigDecimal total = paymentService.getTotalPaymentBySupplierId(supplierId);
        return ResponseEntity.ok(total);
    }
}
