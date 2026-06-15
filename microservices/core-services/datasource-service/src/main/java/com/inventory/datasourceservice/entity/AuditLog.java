package com.inventory.datasourceservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_al_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_al_user_id", columnList = "user_id"),
    @Index(name = "idx_al_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(length = 128)
    private String username;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Operation operation;

    @Column(name = "resource_type", nullable = false, length = 32)
    private String resourceType;

    @Column(name = "resource_id", length = 64)
    private String resourceId;

    @Column(name = "old_value", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValue;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Operation {
        CREATE, UPDATE, DELETE, VIEW, TEST, EXPORT, IMPORT, LOGIN, LOGOUT
    }
}
