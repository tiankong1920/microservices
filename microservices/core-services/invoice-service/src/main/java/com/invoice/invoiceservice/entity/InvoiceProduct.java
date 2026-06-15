package com.invoice.invoiceservice.entity;

import java.math.BigDecimal;

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
@Table(name = "invoice_product", indexes = {
    @Index(name = "idx_product_name", columnList = "product_name"),
    @Index(name = "idx_product_spec", columnList = "specification"),
    @Index(name = "idx_product_usage", columnList = "usage_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "specification", length = 200)
    private String specification;

    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "product_category", length = 100)
    private String productCategory;

    @Column(name = "tax_category_code", length = 50)
    private String taxCategoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ENABLED;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "last_used_at")
    private java.time.LocalDateTime lastUsedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    public enum ProductStatus {
        ENABLED, DISABLED
    }

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
