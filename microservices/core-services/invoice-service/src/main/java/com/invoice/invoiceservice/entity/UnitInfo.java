package com.invoice.invoiceservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "invoice_unit_info", indexes = {
    @Index(name = "idx_unit_name", columnList = "unit_name"),
    @Index(name = "idx_unit_usage", columnList = "usage_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_name", nullable = false, length = 50, unique = true)
    private String unitName;

    @Column(name = "aliases", length = 500)
    private String aliases;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
