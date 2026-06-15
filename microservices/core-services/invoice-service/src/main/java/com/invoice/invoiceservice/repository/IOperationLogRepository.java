package com.invoice.invoiceservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.invoiceservice.entity.OperationLog;

@Repository
public interface IOperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            OperationLog.EntityType entityType, Long entityId);

    List<OperationLog> findByOperatorOrderByCreatedAtDesc(String operator);

    @Query("SELECT o FROM OperationLog o WHERE o.entityType = :entityType " +
           "AND o.entityId = :entityId AND o.operationType = :opType " +
           "ORDER BY o.createdAt DESC")
    List<OperationLog> findByEntityAndOperation(
            @Param("entityType") OperationLog.EntityType entityType,
            @Param("entityId") Long entityId,
            @Param("opType") OperationLog.OperationType opType);

    @Query("SELECT o FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY o.createdAt DESC")
    List<OperationLog> findByTimeRange(
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);
}
