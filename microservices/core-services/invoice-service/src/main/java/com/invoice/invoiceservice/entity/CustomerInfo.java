package com.invoice.invoiceservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_customer_info", indexes = {
    @Index(name = "idx_customer_name", columnList = "customer_name"),
    @Index(name = "idx_customer_tax_no", columnList = "tax_number"),
    @Index(name = "idx_customer_status", columnList = "status"),
    @Index(name = "idx_customer_pinyin", columnList = "pinyin_initials"),
    @Index(name = "idx_customer_usage", columnList = "usage_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "tax_number", nullable = false, length = 100)
    private String taxNumber;

    @Column(name = "registered_address", length = 500)
    private String registeredAddress;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Lob
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "pinyin_initials", length = 50)
    private String pinyinInitials;

    @Column(name = "pinyin_full", length = 500)
    private String pinyinFull;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ENABLED;

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

    public enum CustomerStatus {
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
