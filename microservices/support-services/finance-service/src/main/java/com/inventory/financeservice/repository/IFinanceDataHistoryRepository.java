package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.FinanceDataHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IFinanceDataHistoryRepository extends JpaRepository<FinanceDataHistory, Long> {

    List<FinanceDataHistory> findByEntityTypeAndEntityIdOrderByChangeTimeDesc(String entityType, Long entityId);

    Page<FinanceDataHistory> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    List<FinanceDataHistory> findByChangedBy(String changedBy);

    @Query("SELECT h FROM FinanceDataHistory h WHERE h.entityType = :entityType AND h.entityId = :entityId AND h.changeTime >= :startTime ORDER BY h.changeTime DESC")
    List<FinanceDataHistory> findRecentChanges(@Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT h FROM FinanceDataHistory h WHERE h.fieldName = :fieldName AND h.entityType = :entityType AND h.entityId = :entityId ORDER BY h.changeTime DESC")
    List<FinanceDataHistory> findFieldHistory(@Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("fieldName") String fieldName);

    @Query("SELECT h FROM FinanceDataHistory h WHERE h.changeType = 'UPDATE' AND h.changeTime >= :startTime ORDER BY h.changeTime DESC")
    Page<FinanceDataHistory> findRecentUpdates(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    @Query("SELECT h FROM FinanceDataHistory h WHERE h.entityCode = :entityCode ORDER BY h.changeTime DESC")
    List<FinanceDataHistory> findByEntityCode(@Param("entityCode") String entityCode);
}
