package com.inventory.templateservice.entity;

import com.inventory.common.template.FieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_field", indexes = {
    @Index(name = "idx_custom_field_template", columnList = "template_id"),
    @Index(name = "idx_custom_field_code", columnList = "field_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "field_code", nullable = false, length = 64)
    private String fieldCode;

    @Column(name = "field_name", nullable = false, length = 64)
    private String fieldName;

    @Column(name = "field_label", nullable = false, length = 255)
    private String fieldLabel;

    @Column(name = "field_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Column(length = 500)
    private String description;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(nullable = false)
    @Builder.Default
    private Boolean required = false;

    @Column(name = "validation_regex", length = 500)
    private String validationRegex;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(columnDefinition = "TEXT")
    private String options;

    @Column(name = "role_permissions", columnDefinition = "TEXT")
    private String rolePermissions;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
