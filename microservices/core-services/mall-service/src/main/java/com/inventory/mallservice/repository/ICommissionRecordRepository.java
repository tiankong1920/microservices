package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.CommissionRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 佣金记录Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface ICommissionRecordRepository extends JpaRepository<CommissionRecord, Long> {

    List<CommissionRecord> findByDistributorIdAndStatus(Long distributorId, String status);

    List<CommissionRecord> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
}
