package com.inventory.authservice.repository;

import com.inventory.authservice.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ISecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    Page<SecurityAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<SecurityAuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    Page<SecurityAuditLog> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    List<SecurityAuditLog> findByUsernameAndEventTypeAndTimestampAfterOrderByTimestampDesc(
        String username, String eventType, LocalDateTime timestamp);

    long countByUsernameAndEventTypeAndTimestampAfter(String username, String eventType, LocalDateTime timestamp);

    void deleteByTimestampBefore(LocalDateTime cutoffDate);

    long countByTimestampBefore(LocalDateTime cutoffDate);
}
