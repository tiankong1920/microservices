package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.RefundApplication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退款Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IRefundApplicationRepository extends JpaRepository<RefundApplication, Long> {

    Page<RefundApplication> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<RefundApplication> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<RefundApplication> findByOrderId(Long orderId);

    List<RefundApplication> findByStatus(String status);
}
