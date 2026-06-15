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
@Table(name = "alert_config", indexes = {
    @Index(name = "idx_ac_tenant_id", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "datasource_ids", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String datasourceIds;

    @Column(name = "alert_level", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @Column(name = "alert_channels", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String alertChannels;

    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String receivers;

    @Column(name = "notify_frequency", length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotifyFrequency notifyFrequency = NotifyFrequency.IMMEDIATE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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

    public enum AlertLevel {
        CRITICAL, WARNING, INFO
    }

    public enum NotifyFrequency {
        IMMEDIATE, HOURLY, DAILY
    }
}
