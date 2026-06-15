package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {

    Optional<ApprovalWorkflow> findByWorkflowCode(String workflowCode);

    List<ApprovalWorkflow> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<ApprovalWorkflow> findByStatus(ApprovalWorkflow.WorkflowStatus status);

    List<ApprovalWorkflow> findByCurrentApproverId(String approverId);

    List<ApprovalWorkflow> findByRequesterId(String requesterId);

    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.status = 'PENDING' AND w.currentApproverId = :approverId")
    List<ApprovalWorkflow> findPendingApprovalsForUser(@Param("approverId") String approverId);

    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.workflowType = :type AND w.status = :status")
    List<ApprovalWorkflow> findByWorkflowTypeAndStatus(@Param("type") ApprovalWorkflow.WorkflowType workflowType, @Param("status") ApprovalWorkflow.WorkflowStatus status);

    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.submittedAt >= :startDate AND w.submittedAt <= :endDate")
    List<ApprovalWorkflow> findBySubmittedDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(w) FROM ApprovalWorkflow w WHERE w.currentApproverId = :approverId AND w.status = 'PENDING'")
    Long countPendingApprovals(@Param("approverId") String approverId);
}
