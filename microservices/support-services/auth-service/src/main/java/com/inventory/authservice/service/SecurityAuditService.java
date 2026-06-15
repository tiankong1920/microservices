package com.inventory.authservice.service;

import com.inventory.authservice.entity.SecurityAuditLog;
import com.inventory.authservice.repository.ISecurityAuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
@SuppressWarnings("null")
public class SecurityAuditService {

    private static final int AUDIT_LOG_RETENTION_DAYS = 180;

    private final ISecurityAuditLogRepository auditLogRepository;

    public SecurityAuditService(ISecurityAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(AUDIT_LOG_RETENTION_DAYS);
        long countToDelete = auditLogRepository.countByTimestampBefore(cutoffDate);

        if (countToDelete > 0) {
            log.info("Starting cleanup of audit logs older than {} (retention: {} days)", cutoffDate, AUDIT_LOG_RETENTION_DAYS);
            auditLogRepository.deleteByTimestampBefore(cutoffDate);
            log.info("Completed cleanup of {} old audit log entries", countToDelete);
        } else {
            log.debug("No audit logs older than {} to delete", cutoffDate);
        }
    }

    @Async
    public void logLoginSuccess(String username, String ipAddress, String userAgent) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("LOGIN_SUCCESS")
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .description("User logged in successfully")
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logLoginFailure(String username, String ipAddress, String reason) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("LOGIN_FAILURE")
            .username(username)
            .ipAddress(ipAddress)
            .description("Login failed: " + reason)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logLogout(String username, String ipAddress, String description) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("LOGOUT")
            .username(username)
            .ipAddress(ipAddress)
            .description(description)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logPasswordChange(String username, String ipAddress) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("PASSWORD_CHANGE")
            .username(username)
            .ipAddress(ipAddress)
            .description("Password changed successfully")
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logAccountLockout(String username, String ipAddress, String reason) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("ACCOUNT_LOCKOUT")
            .username(username)
            .ipAddress(ipAddress)
            .description("Account locked: " + reason)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logAccountUnlock(String username, String unlockedBy, String ipAddress) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("ACCOUNT_UNLOCK")
            .username(username)
            .ipAddress(ipAddress)
            .description("Account unlocked by: " + unlockedBy)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logPermissionChange(String username, String changedBy, String description) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType("PERMISSION_CHANGE")
            .username(username)
            .description(description)
            .additionalInfo("Changed by: " + changedBy)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logMfaEvent(String username, String eventType, String ipAddress, String description) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType(eventType)
            .username(username)
            .ipAddress(ipAddress)
            .description(description)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Async
    public void logSecurityEvent(String eventType, String username, String ipAddress, String description) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .eventType(eventType)
            .username(username)
            .ipAddress(ipAddress)
            .description(description)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getAuditLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getAuditLogsByEventType(String eventType, Pageable pageable) {
        return auditLogRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getRecentLoginAttempts(String username, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findByUsernameAndEventTypeAndTimestampAfterOrderByTimestampDesc(
            username, "LOGIN_FAILURE", since);
    }

    @Transactional(readOnly = true)
    public long countFailedLogins(String username, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countByUsernameAndEventTypeAndTimestampAfter(
            username, "LOGIN_FAILURE", since);
    }
}
