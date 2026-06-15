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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "datasource_config", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private DatasourceType type;

    @Column(length = 32)
    private String version;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "database_name", length = 128)
    private String databaseName;

    @Column(length = 128)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(name = "extra_config", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String extraConfig;

    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DatasourceStatus status = DatasourceStatus.ACTIVE;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DatasourceType {
        MYSQL, POSTGRESQL, ELASTICSEARCH, KUDU
    }

    public enum DatasourceStatus {
        ACTIVE, INACTIVE, DELETED, ERROR
    }
}
