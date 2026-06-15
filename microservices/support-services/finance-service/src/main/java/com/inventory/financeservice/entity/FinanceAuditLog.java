package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "finance_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_code", length = 100)
    private String entityCode;

    @Column(name = "operator_id", nullable = false)
    private String operatorId;

    @Column(name = "operator_name", length = 200)
    private String operatorName;

    @Column(name = "operator_role", length = 100)
    private String operatorRole;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private Severity severity = Severity.INFO;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    public enum Severity {
        TRACE, DEBUG, INFO, WARN, ERROR, SECURITY
    }

    @PrePersist
    protected void onCreate() {
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
    }
}
