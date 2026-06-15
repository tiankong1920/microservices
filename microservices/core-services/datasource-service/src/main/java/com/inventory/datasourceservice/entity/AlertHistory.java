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

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_history", indexes = {
    @Index(name = "idx_ah_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_ah_datasource_id", columnList = "datasource_id"),
    @Index(name = "idx_ah_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "datasource_id", nullable = false)
    private Long datasourceId;

    @Column(name = "alert_config_id")
    private Long alertConfigId;

    @Column(name = "alert_level", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private AlertConfig.AlertLevel alertLevel;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AlertStatus status = AlertStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AlertStatus {
        PENDING, SENT, FAILED
    }
}
