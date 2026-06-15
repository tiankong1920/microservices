package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.FinanceAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IFinanceAuditLogRepository extends JpaRepository<FinanceAuditLog, Long> {

    List<FinanceAuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<FinanceAuditLog> findByOperatorId(String operatorId);

    List<FinanceAuditLog> findBySeverity(FinanceAuditLog.Severity severity);

    @Query("SELECT a FROM FinanceAuditLog a WHERE a.operationTime >= :startTime AND a.operationTime <= :endTime")
    List<FinanceAuditLog> findByOperationTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM FinanceAuditLog a WHERE a.entityType = :entityType AND a.operationType = :operationType")
    Page<FinanceAuditLog> findByEntityTypeAndOperationType(@Param("entityType") String entityType, @Param("operationType") String operationType, Pageable pageable);

    @Query("SELECT a FROM FinanceAuditLog a WHERE a.operatorId = :operatorId AND a.operationTime >= :startTime ORDER BY a.operationTime DESC")
    List<FinanceAuditLog> findRecentByOperator(@Param("operatorId") String operatorId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT a FROM FinanceAuditLog a WHERE a.severity = 'SECURITY' AND a.operationTime >= :startTime")
    List<FinanceAuditLog> findSecurityLogs(@Param("startTime") LocalDateTime startTime);

    Page<FinanceAuditLog> findByEntityType(String entityType, Pageable pageable);
}
