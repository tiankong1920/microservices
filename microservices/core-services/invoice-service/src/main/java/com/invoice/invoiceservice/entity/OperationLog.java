package com.invoice.invoiceservice.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_operation_log", indexes = {
    @Index(name = "idx_oplog_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_oplog_user", columnList = "operator"),
    @Index(name = "idx_oplog_time", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 30)
    private OperationType operationType;

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    public enum EntityType {
        CUSTOMER_INFO, INVOICE_PRODUCT, UNIT_INFO
    }

    public enum OperationType {
        CREATE, UPDATE, DELETE, ENABLE, DISABLE, SEARCH, AUTO_FILL
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
