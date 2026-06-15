package com.inventory.datasourceservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connection_status", indexes = {
    @Index(name = "idx_cs_datasource_id", columnList = "datasource_id"),
    @Index(name = "idx_cs_checked_at", columnList = "checked_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id", nullable = false)
    private Long datasourceId;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ConnectionStatusEnum status;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    public enum ConnectionStatusEnum {
        CONNECTED, DISCONNECTED, ERROR, TESTING
    }
}
