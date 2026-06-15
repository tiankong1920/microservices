package com.inventory.templateservice.repository;

import com.inventory.templateservice.entity.TemplateAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITemplateAuditLogRepository extends JpaRepository<TemplateAuditLog, Long> {

    List<TemplateAuditLog> findByTemplateIdOrderByOperationTimeDesc(Long templateId);

    Page<TemplateAuditLog> findByTemplateIdOrderByOperationTimeDesc(Long templateId, Pageable pageable);

    Page<TemplateAuditLog> findByTenantIdOrderByOperationTimeDesc(String tenantId, Pageable pageable);

    List<TemplateAuditLog> findByOperatorOrderByOperationTimeDesc(String operator);

    @Query("SELECT a FROM TemplateAuditLog a WHERE a.templateId = :templateId " +
           "AND a.operationTime BETWEEN :startTime AND :endTime ORDER BY a.operationTime DESC")
    List<TemplateAuditLog> findByTemplateIdAndTimeRange(
            @Param("templateId") Long templateId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM TemplateAuditLog a WHERE a.tenantId = :tenantId " +
           "AND a.operationTime BETWEEN :startTime AND :endTime ORDER BY a.operationTime DESC")
    Page<TemplateAuditLog> findByTenantIdAndTimeRange(
            @Param("tenantId") String tenantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT a.operation, COUNT(a) FROM TemplateAuditLog a WHERE a.templateId = :templateId " +
           "GROUP BY a.operation")
    List<Object[]> countByOperation(@Param("templateId") Long templateId);

    @Query("SELECT a FROM TemplateAuditLog a WHERE a.tenantId = :tenantId " +
           "AND (:operation IS NULL OR a.operation = :operation) " +
           "AND (:operator IS NULL OR a.operator = :operator) " +
           "ORDER BY a.operationTime DESC")
    Page<TemplateAuditLog> search(
            @Param("tenantId") String tenantId,
            @Param("operation") String operation,
            @Param("operator") String operator,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM TemplateAuditLog a WHERE a.templateId = :templateId")
    long countByTemplateId(@Param("templateId") Long templateId);
}
