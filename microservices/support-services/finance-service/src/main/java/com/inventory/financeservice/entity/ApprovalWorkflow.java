package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_code", nullable = false, unique = true, length = 50)
    private String workflowCode;

    @Column(name = "workflow_name", nullable = false, length = 200)
    private String workflowName;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, length = 30)
    private WorkflowType workflowType;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_code", length = 100)
    private String entityCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private ApprovalStep currentStep = ApprovalStep.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Column(name = "requester_id", nullable = false)
    private String requesterId;

    @Column(name = "requester_name", length = 200)
    private String requesterName;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "current_approver_id")
    private String currentApproverId;

    @Column(name = "current_approver_name", length = 200)
    private String currentApproverName;

    @Column(name = "approval_history", columnDefinition = "TEXT")
    private String approvalHistory;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum WorkflowType {
        INCOME_APPROVAL, EXPENSE_APPROVAL, BUDGET_APPROVAL,
        PAYMENT_APPROVAL, ADJUSTMENT_APPROVAL, REPORT_APPROVAL
    }

    public enum ApprovalStep {
        PENDING, LEVEL_1, LEVEL_2, LEVEL_3, FINAL, APPROVED, REJECTED
    }

    public enum WorkflowStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELLED, EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
