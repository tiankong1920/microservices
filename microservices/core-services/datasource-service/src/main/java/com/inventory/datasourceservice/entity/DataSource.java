package com.inventory.datasourceservice.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "data_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("null")
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DataSourceType type;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "host", nullable = false, length = 255)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "database_name", length = 100)
    private String databaseName;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", length = 500)
    private String password;

    @Column(name = "encrypted", nullable = false)
    @Builder.Default
    private Boolean encrypted = false;

    @ElementCollection
    @CollectionTable(name = "data_source_properties", joinColumns = @JoinColumn(name = "data_source_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DataSourceStatus status = DataSourceStatus.INACTIVE;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_test_time")
    private LocalDateTime lastTestTime;

    @Column(name = "last_test_result", length = 20)
    private String lastTestResult;

    @Column(name = "last_test_duration_ms")
    private Long lastTestDurationMs;

    @Column(name = "last_test_error", columnDefinition = "TEXT")
    private String lastTestError;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum DataSourceType {
        MYSQL("MySQL"),
        POSTGRESQL("PostgreSQL"),
        ELASTICSEARCH("Elasticsearch"),
        KUDU("Kudu"),
        CUSTOM("Custom");

        private final String displayName;

        DataSourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DataSourceStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        ERROR("Error"),
        TESTING("Testing");

        private final String displayName;

        DataSourceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
