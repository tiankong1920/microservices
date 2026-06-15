package com.inventory.templateservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "template_version", indexes = {
    @Index(name = "idx_version_template", columnList = "template_id"),
    @Index(name = "idx_version_number", columnList = "version_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "version_number", nullable = false, length = 32)
    private String versionNumber;

    @Column(name = "major_version", nullable = false)
    @Builder.Default
    private Integer majorVersion = 1;

    @Column(name = "minor_version", nullable = false)
    @Builder.Default
    private Integer minorVersion = 0;

    @Column(name = "patch_version", nullable = false)
    @Builder.Default
    private Integer patchVersion = 0;

    @Column(name = "change_description", columnDefinition = "TEXT")
    private String changeDescription;

    @Column(name = "change_type", length = 16)
    private String changeType;

    @Column(name = "changed_fields", columnDefinition = "TEXT")
    private String changedFields;

    @Column(columnDefinition = "TEXT")
    private String snapshot;

    @Column(name = "changed_by", length = 64)
    private String changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "is_rollback")
    @Builder.Default
    private Boolean isRollback = false;

    @Column(name = "rollback_from_version_id")
    private Long rollbackFromVersionId;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
