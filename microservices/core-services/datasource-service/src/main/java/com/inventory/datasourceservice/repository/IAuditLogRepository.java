package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.AuditLog;
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
public interface IAuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    List<AuditLog> findByResourceId(String resourceId);

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId " +
           "AND (:operation IS NULL OR al.operation = :operation) " +
           "AND (:resourceType IS NULL OR al.resourceType = :resourceType) " +
           "AND (:userId IS NULL OR al.userId = :userId) " +
           "AND (:startTime IS NULL OR al.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR al.createdAt <= :endTime)")
    Page<AuditLog> search(@Param("tenantId") String tenantId,
                          @Param("operation") AuditLog.Operation operation,
                          @Param("resourceType") String resourceType,
                          @Param("userId") String userId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          Pageable pageable);

    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime before);
}
