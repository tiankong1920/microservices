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
@Table(name = "connection_test_log", indexes = {
    @Index(name = "idx_ctl_datasource_id", columnList = "datasource_id"),
    @Index(name = "idx_ctl_tested_at", columnList = "tested_at"),
    @Index(name = "idx_ctl_result", columnList = "result")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id", nullable = false)
    private Long datasourceId;

    @Column(name = "test_type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private TestType testType;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private TestResult result;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "error_code", length = 32)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "tested_by", length = 64)
    private String testedBy;

    @Column(name = "tested_at", nullable = false)
    private LocalDateTime testedAt;

    public enum TestType {
        AUTO, MANUAL, BATCH, SCHEDULED
    }

    public enum TestResult {
        SUCCESS, FAILURE, TIMEOUT
    }
}
