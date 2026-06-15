package com.inventory.templateservice.entity;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template", indexes = {
    @Index(name = "idx_template_code", columnList = "template_code", unique = true),
    @Index(name = "idx_template_tenant", columnList = "tenant_id"),
    @Index(name = "idx_template_domain", columnList = "business_domain"),
    @Index(name = "idx_template_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false, unique = true, length = 64)
    private String templateCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "business_domain", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private BusinessDomain businessDomain;

    @Column(length = 64)
    private String category;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String version = "1.0.0";

    @Column(name = "parent_version_id")
    private Long parentVersionId;

    @Column(name = "allow_custom_fields")
    @Builder.Default
    private Boolean allowCustomFields = true;

    @Column(name = "allow_extension")
    @Builder.Default
    private Boolean allowExtension = false;

    @Column(name = "extension_point", length = 255)
    private String extensionPoint;

    @Column(name = "min_config_items")
    private Integer minConfigItems;

    @Column(name = "max_config_items")
    private Integer maxConfigItems;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_by", length = 64)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "deprecated_at")
    private LocalDateTime deprecatedAt;

    @Column(name = "change_log", columnDefinition = "TEXT")
    private String changeLog;

    @Column(name = "default_values", columnDefinition = "TEXT")
    private String defaultValues;

    @Column(name = "applicable_scenarios", columnDefinition = "TEXT")
    private String applicableScenarios;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> fields = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TemplateStatus.DRAFT;
        }
        if (version == null) {
            version = "1.0.0";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
