package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Payment;
import com.inventory.mallservice.service.IPaymentService;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 支付Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/payments")
@Tag(name = "Mall Payment", description = "商城支付管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class PaymentController {

    private final IPaymentService paymentService;

    /**
     * 创建支付记录
     *
     * @param request 包含orderId、amount和paymentMethod的请求数据
     * @return 创建的支付记录信息
     */
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @Valid @RequestBody final Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String paymentMethod = (String) request.get("paymentMethod");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(orderId, amount, paymentMethod));
    }

    /**
     * 获取支付详情
     *
     * @param id 支付ID
     * @return 支付记录信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable final Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    /**
     * 根据支付号获取支付信息
     *
     * @param paymentNo 支付号
     * @return 支付记录信息
     */
    @GetMapping("/no/{paymentNo}")
    public ResponseEntity<Payment> getPaymentByNo(@PathVariable final String paymentNo) {
        return ResponseEntity.ok(paymentService.getPaymentByPaymentNo(paymentNo));
    }

    /**
     * 根据订单ID获取支付信息
     *
     * @param orderId 订单ID
     * @return 支付记录信息
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable final Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    /**
     * 分页获取支付列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 支付分页列表
     */
    @GetMapping
    public ResponseEntity<Page<Payment>> getPayments(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(paymentService.getPayments(pageable));
    }

    /**
     * 根据状态获取支付列表
     *
     * @param status 支付状态
     * @return 支付列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(
            @PathVariable final String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    /**
     * 处理支付成功
     *
     * @param request 包含paymentNo和transactionId的请求数据
     * @return 处理后的支付记录信息
     */
    @PostMapping("/success")
    public ResponseEntity<Payment> processPaymentSuccess(
            @Valid @RequestBody final Map<String, String> request) {
        String paymentNo = request.get("paymentNo");
        String transactionId = request.get("transactionId");
        return ResponseEntity.ok(paymentService.processPaymentSuccess(paymentNo, transactionId));
    }

    /**
     * 处理支付失败
     *
     * @param request 包含paymentNo和reason的请求数据
     * @return 处理后的支付记录信息
     */
    @PostMapping("/failed")
    public ResponseEntity<Payment> processPaymentFailed(
            @Valid @RequestBody final Map<String, String> request) {
        String paymentNo = request.get("paymentNo");
        String reason = request.get("reason");
        return ResponseEntity.ok(paymentService.processPaymentFailed(paymentNo, reason));
    }

    /**
     * 支付退款
     *
     * @param paymentId 支付ID
     * @return 退款后的支付记录信息
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable final Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    /**
     * 更新支付状态
     *
     * @param id 支付ID
     * @param request 包含status和transactionId的请求数据
     * @return 更新后的支付记录信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable final Long id,
            @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(
                id, request.get("status"), request.get("transactionId")));
    }
}
