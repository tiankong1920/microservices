package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.RefundApplication;
import com.inventory.mallservice.service.IRefundService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * 退款Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/refunds")
@Tag(name = "Refund", description = "退款管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class RefundController {

    private final IRefundService refundService;

    /**
     * 创建退款申请
     *
     * @param request 包含orderId、userId、refundType、refundAmount和reason的请求数据
     * @return 创建的退款申请信息
     */
    @PostMapping
    public ResponseEntity<RefundApplication> createRefund(
            @Valid @RequestBody final Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String refundType = (String) request.get("refundType");
        BigDecimal refundAmount = new BigDecimal(request.get("refundAmount").toString());
        String reason = (String) request.get("reason");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.createRefundApplication(
                        orderId, userId, refundType, refundAmount, reason));
    }

    /**
     * 审批通过退款申请
     *
     * @param id 退款申请ID
     * @param request 包含auditorId和可选notes的请求数据
     * @return 审批后的退款申请信息
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<RefundApplication> approveRefund(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, Object> request) {
        Long auditorId = Long.valueOf(request.get("auditorId").toString());
        String notes = (String) request.getOrDefault("notes", "");
        return ResponseEntity.ok(refundService.approveRefund(id, auditorId, notes));
    }

    /**
     * 拒绝退款申请
     *
     * @param id 退款申请ID
     * @param request 包含auditorId和可选notes的请求数据
     * @return 拒绝后的退款申请信息
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<RefundApplication> rejectRefund(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, Object> request) {
        Long auditorId = Long.valueOf(request.get("auditorId").toString());
        String notes = (String) request.getOrDefault("notes", "");
        return ResponseEntity.ok(refundService.rejectRefund(id, auditorId, notes));
    }

    /**
     * 处理退款
     *
     * @param id 退款申请ID
     * @return 处理后的退款申请信息
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<RefundApplication> processRefund(@PathVariable final Long id) {
        return ResponseEntity.ok(refundService.processRefund(id));
    }

    /**
     * 获取退款申请详情
     *
     * @param id 退款申请ID
     * @return 退款申请信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefundApplication> getRefund(@PathVariable final Long id) {
        return ResponseEntity.ok(refundService.getRefundById(id));
    }

    /**
     * 根据状态获取退款申请列表
     *
     * @param status 退款状态
     * @param page 页码
     * @param size 每页大小
     * @return 退款申请分页列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RefundApplication>> getRefundsByStatus(
            @PathVariable final String status,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(refundService.getRefundsByStatus(status, pageable));
    }

    /**
     * 获取用户的退款申请列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 用户的退款申请分页列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<RefundApplication>> getUserRefunds(
            @PathVariable final Long userId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(refundService.getRefundsByUserId(userId, pageable));
    }

    /**
     * 获取订单的退款申请列表
     *
     * @param orderId 订单ID
     * @return 退款申请列表
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<RefundApplication>> getOrderRefunds(
            @PathVariable final Long orderId) {
        return ResponseEntity.ok(refundService.getRefundsByOrderId(orderId));
    }
}
