package com.inventory.templateservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "validation_rule", indexes = {
    @Index(name = "idx_rule_code", columnList = "rule_code", unique = true),
    @Index(name = "idx_rule_type", columnList = "rule_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 64)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 32)
    private String ruleType;

    @Column(name = "regex_pattern", length = 500)
    private String regexPattern;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "applicable_field_types", columnDefinition = "TEXT")
    private String applicableFieldTypes;

    @Column(name = "built_in")
    @Builder.Default
    private Boolean builtIn = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
