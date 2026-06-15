package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.AlertHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    Page<AlertHistory> findByTenantId(String tenantId, Pageable pageable);

    List<AlertHistory> findByDatasourceId(Long datasourceId);

    @Query("SELECT ah FROM AlertHistory ah WHERE ah.tenantId = :tenantId " +
           "AND (:datasourceId IS NULL OR ah.datasourceId = :datasourceId) " +
           "AND (:status IS NULL OR ah.status = :status) " +
           "AND (:startTime IS NULL OR ah.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR ah.createdAt <= :endTime)")
    Page<AlertHistory> search(@Param("tenantId") String tenantId,
                               @Param("datasourceId") Long datasourceId,
                               @Param("status") AlertHistory.AlertStatus status,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime,
                               Pageable pageable);

    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime before);
}
