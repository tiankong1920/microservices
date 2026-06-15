package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.RefundApplication;
import com.inventory.mallservice.exception.RefundException;
import com.inventory.mallservice.repository.IRefundApplicationRepository;
import com.inventory.mallservice.service.IRefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RefundServiceImpl implements IRefundService {

    private final IRefundApplicationRepository refundRepository;

    @Override
    @Transactional
    public RefundApplication createRefundApplication(final Long orderId, final Long userId,
                                                       final String refundType,
                                                       final BigDecimal refundAmount,
                                                       final String reason) {
        log.info("Creating refund application for order {} by user {}", orderId, userId);
        RefundApplication application = new RefundApplication();
        application.setOrderId(orderId);
        application.setUserId(userId);
        application.setRefundType(refundType);
        application.setRefundAmount(refundAmount);
        application.setReason(reason);
        application.setStatus("PENDING");
        return refundRepository.save(application);
    }

    @Override
    @Transactional
    public RefundApplication approveRefund(final Long applicationId, final Long auditorId,
                                             final String notes) {
        log.info("Approving refund application {} by auditor {}", applicationId, auditorId);
        RefundApplication application = refundRepository.findById(applicationId)
                .orElseThrow(() -> RefundException.notFound(applicationId));
        if (!"PENDING".equals(application.getStatus())) {
            throw RefundException.notInPendingStatus();
        }
        application.setStatus("APPROVED");
        return refundRepository.save(application);
    }

    @Override
    @Transactional
    public RefundApplication rejectRefund(final Long applicationId, final Long auditorId,
                                            final String notes) {
        log.info("Rejecting refund application {} by auditor {}", applicationId, auditorId);
        RefundApplication application = refundRepository.findById(applicationId)
                .orElseThrow(() -> RefundException.notFound(applicationId));
        if (!"PENDING".equals(application.getStatus())) {
            throw RefundException.notInPendingStatus();
        }
        application.setStatus("REJECTED");
        return refundRepository.save(application);
    }

    @Override
    @Transactional
    public RefundApplication processRefund(final Long applicationId) {
        log.info("Processing refund for application {}", applicationId);
        RefundApplication application = refundRepository.findById(applicationId)
                .orElseThrow(() -> RefundException.notFound(applicationId));
        if (!"APPROVED".equals(application.getStatus())) {
            throw RefundException.notApproved();
        }
        application.setStatus("PROCESSING");
        refundRepository.save(application);

        try {
            application.setStatus("COMPLETED");
            log.info("Refund processed successfully for application {}", applicationId);
        } catch (Exception e) {
            application.setStatus("FAILED");
            log.error("Refund processing failed for application {}: {}", applicationId, e.getMessage());
        }
        return refundRepository.save(application);
    }

    @Override
    public RefundApplication getRefundById(final Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("RefundApplication", id));
    }

    @Override
    public Page<RefundApplication> getRefundsByStatus(final String status, final Pageable pageable) {
        return refundRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<RefundApplication> getRefundsByUserId(final Long userId, final Pageable pageable) {
        return refundRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public List<RefundApplication> getRefundsByOrderId(final Long orderId) {
        return refundRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public void retryFailedRefunds() {
        log.info("Retrying failed refunds");
        List<RefundApplication> failedRefunds = refundRepository.findByStatus("FAILED");
        for (RefundApplication refund : failedRefunds) {
            try {
                processRefund(refund.getId());
            } catch (Exception e) {
                log.warn("Retry failed for refund {}: {}", refund.getId(), e.getMessage());
            }
        }
    }
}
