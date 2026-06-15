package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.RefundAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退款审核Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IRefundAuditRepository extends JpaRepository<RefundAudit, Long> {

    List<RefundAudit> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
