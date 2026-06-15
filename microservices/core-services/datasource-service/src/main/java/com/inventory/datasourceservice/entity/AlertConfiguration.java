package com.inventory.datasourceservice.entity;

import java.time.LocalDateTime;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alert_configurations", indexes = {
    @Index(name = "idx_alert_tenant", columnList = "tenant_id"),
    @Index(name = "idx_alert_datasource", columnList = "data_source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("null")
public class AlertConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "data_source_id")
    private Long dataSourceId;

    @Column(name = "alert_name", nullable = false, length = 100)
    private String alertName;

    @Column(name = "alert_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "alert_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "notification_channels", length = 500)
    private String notificationChannels;

    @Column(name = "recipients", length = 1000)
    private String recipients;

    @Column(name = "notification_frequency", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationFrequency notificationFrequency = NotificationFrequency.IMMEDIATE;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public enum AlertType {
        CONNECTION_FAILED,
        CONNECTION_TIMEOUT,
        HIGH_LATENCY,
        CONNECTION_RESTORED,
        TEST_FAILED,
        CONFIG_CHANGED
    }

    public enum AlertLevel {
        CRITICAL,
        WARNING,
        INFO
    }

    public enum NotificationFrequency {
        IMMEDIATE,
        HOURLY_SUMMARY,
        DAILY_SUMMARY
    }
}
