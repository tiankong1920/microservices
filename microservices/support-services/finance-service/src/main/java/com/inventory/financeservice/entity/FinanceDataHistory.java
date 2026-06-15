package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "finance_data_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDataHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_code", length = 100)
    private String entityCode;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_label", length = 200)
    private String fieldLabel;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_by_name", length = 200)
    private String changedByName;

    @Column(name = "change_time", nullable = false)
    private LocalDateTime changeTime;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    public enum ChangeType {
        CREATE, UPDATE, DELETE, APPROVE, REJECT, REVERT
    }

    @PrePersist
    protected void onCreate() {
        if (changeTime == null) {
            changeTime = LocalDateTime.now();
        }
    }
}
