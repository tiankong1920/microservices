package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.RefundApplication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IRefundService {

    RefundApplication createRefundApplication(Long orderId, Long userId, String refundType,
                                               BigDecimal refundAmount, String reason);

    RefundApplication approveRefund(Long applicationId, Long auditorId, String notes);

    RefundApplication rejectRefund(Long applicationId, Long auditorId, String notes);

    RefundApplication processRefund(Long applicationId);

    RefundApplication getRefundById(Long id);

    Page<RefundApplication> getRefundsByStatus(String status, Pageable pageable);

    Page<RefundApplication> getRefundsByUserId(Long userId, Pageable pageable);

    List<RefundApplication> getRefundsByOrderId(Long orderId);

    void retryFailedRefunds();
}
