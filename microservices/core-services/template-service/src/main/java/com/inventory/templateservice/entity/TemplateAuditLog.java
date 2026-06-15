package com.inventory.templateservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "template_audit_log", indexes = {
    @Index(name = "idx_audit_template", columnList = "template_id"),
    @Index(name = "idx_audit_time", columnList = "operation_time"),
    @Index(name = "idx_audit_tenant", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "template_code", length = 64)
    private String templateCode;

    @Column(nullable = false, length = 32)
    private String operation;

    @Column(name = "operation_detail", columnDefinition = "TEXT")
    private String operationDetail;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, length = 64)
    private String operator;

    @Column(name = "operator_ip", length = 64)
    private String operatorIp;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "operation_result", length = 16)
    private String operationResult;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
    }
}
