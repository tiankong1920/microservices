package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.BudgetDTO;
import com.inventory.financeservice.entity.ApprovalWorkflow;
import com.inventory.financeservice.entity.Budget;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.repository.IApprovalWorkflowRepository;
import com.inventory.financeservice.repository.IBudgetRepository;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.service.IBudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class BudgetServiceImpl implements IBudgetService {

    private final IBudgetRepository budgetRepository;
    private final IApprovalWorkflowRepository approvalWorkflowRepository;
    private final IFinanceAuditLogRepository auditLogRepository;

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        log.info("Creating new budget: {}", budgetDTO.getBudgetName());

        Budget budget = budgetDTO.toEntity();
        if (budget.getBudgetCode() == null || budget.getBudgetCode().isEmpty()) {
            budget.setBudgetCode(generateBudgetCode());
        }
        budget.setUsedAmount(BigDecimal.ZERO);
        budget.setRemainingAmount(budget.getTotalAmount());
        budget.setStatus(Budget.BudgetStatus.DRAFT);

        Budget saved = budgetRepository.save(budget);

        createAuditLog(saved.getId(), "Budget", saved.getId(), "CREATE",
                null, saved.toString(), saved.getCreatedBy(), "Budget created");

        return BudgetDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO) {
        log.info("Updating budget: {}", id);

        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        String oldValue = existing.toString();

        existing.setBudgetName(budgetDTO.getBudgetName());
        if (budgetDTO.getBudgetType() != null) {
            existing.setBudgetType(Budget.BudgetType.valueOf(budgetDTO.getBudgetType()));
        }
        if (budgetDTO.getBudgetPeriod() != null) {
            existing.setBudgetPeriod(Budget.BudgetPeriod.valueOf(budgetDTO.getBudgetPeriod()));
        }
        existing.setFiscalYear(budgetDTO.getFiscalYear());
        existing.setPeriodStart(budgetDTO.getPeriodStart());
        existing.setPeriodEnd(budgetDTO.getPeriodEnd());
        existing.setTotalAmount(budgetDTO.getTotalAmount());
        existing.setDepartmentId(budgetDTO.getDepartmentId());
        existing.setDepartmentName(budgetDTO.getDepartmentName());
        existing.setProjectId(budgetDTO.getProjectId());
        existing.setProjectName(budgetDTO.getProjectName());
        existing.setExpenseCategory(budgetDTO.getExpenseCategory());
        existing.setWarningThreshold(budgetDTO.getWarningThreshold());
        existing.setDescription(budgetDTO.getDescription());

        if (existing.getStatus() == Budget.BudgetStatus.DRAFT) {
            BigDecimal remaining = existing.getTotalAmount().subtract(existing.getUsedAmount());
            existing.setRemainingAmount(remaining);
        }

        Budget updated = budgetRepository.save(existing);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "UPDATE",
                oldValue, updated.toString(), budgetDTO.getCreatedBy(), "Budget updated");

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    public Optional<BudgetDTO> getBudgetById(Long id) {
        return budgetRepository.findById(id).map(BudgetDTO::fromEntity);
    }

    @Override
    public Optional<BudgetDTO> getBudgetByCode(String budgetCode) {
        return budgetRepository.findByBudgetCode(budgetCode).map(BudgetDTO::fromEntity);
    }

    @Override
    public List<BudgetDTO> getAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByStatus(String status) {
        return budgetRepository.findByStatus(Budget.BudgetStatus.valueOf(status)).stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByDepartment(Long departmentId) {
        return budgetRepository.findByDepartmentId(departmentId).stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId).stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByFiscalYear(Integer fiscalYear) {
        return budgetRepository.findByFiscalYear(fiscalYear).stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetDTO approveBudget(Long id, String approverId) {
        log.info("Approving budget: {} by user: {}", id, approverId);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        if (budget.getStatus() != Budget.BudgetStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Budget is not pending approval");
        }

        budget.setStatus(Budget.BudgetStatus.APPROVED);

        ApprovalWorkflow workflow = createOrUpdateWorkflow(budget, approverId, ApprovalWorkflow.WorkflowStatus.APPROVED);
        workflow.setCompletedAt(LocalDateTime.now());
        approvalWorkflowRepository.save(workflow);

        Budget updated = budgetRepository.save(budget);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "APPROVE",
                null, updated.toString(), approverId, "Budget approved");

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public BudgetDTO rejectBudget(Long id, String rejectorId, String reason) {
        log.info("Rejecting budget: {} by user: {}", id, rejectorId);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        if (budget.getStatus() != Budget.BudgetStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Budget is not pending approval");
        }

        budget.setStatus(Budget.BudgetStatus.DRAFT);

        ApprovalWorkflow workflow = createOrUpdateWorkflow(budget, rejectorId, ApprovalWorkflow.WorkflowStatus.REJECTED);
        workflow.setRejectionReason(reason);
        workflow.setCompletedAt(LocalDateTime.now());
        approvalWorkflowRepository.save(workflow);

        Budget updated = budgetRepository.save(budget);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "REJECT",
                null, updated.toString(), rejectorId, "Budget rejected: " + reason);

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public BudgetDTO cancelBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        budget.setStatus(Budget.BudgetStatus.CANCELLED);
        Budget updated = budgetRepository.save(budget);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "CANCEL",
                null, updated.toString(), updated.getCreatedBy(), "Budget cancelled");

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public BudgetDTO updateBudgetExecution(Long id, BigDecimal usedAmount) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        BigDecimal oldUsed = budget.getUsedAmount();
        budget.setUsedAmount(usedAmount);
        budget.setRemainingAmount(budget.getTotalAmount().subtract(usedAmount));

        if (budget.getStatus() == Budget.BudgetStatus.APPROVED) {
            budget.setStatus(Budget.BudgetStatus.IN_PROGRESS);
        }

        Budget updated = budgetRepository.save(budget);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "UPDATE_EXECUTION",
                oldUsed.toString(), usedAmount.toString(), updated.getUpdatedBy(),
                "Budget execution updated");

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    public List<BudgetDTO> getBudgetsNearLimit() {
        return budgetRepository.findBudgetsNearLimit().stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getExhaustedBudgets() {
        return budgetRepository.findExhaustedBudgets().stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getBudgetUtilizationRate(Long departmentId, Integer fiscalYear) {
        BigDecimal totalBudget = budgetRepository.sumTotalBudgetByYear(fiscalYear);
        BigDecimal usedBudget = budgetRepository.sumUsedAmountByDepartmentAndYear(departmentId, fiscalYear);

        if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return usedBudget.multiply(new BigDecimal("100"))
                .divide(totalBudget, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public BudgetDTO submitForApproval(Long id, String requesterId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new RuntimeException("Only draft budgets can be submitted for approval");
        }

        budget.setStatus(Budget.BudgetStatus.PENDING_APPROVAL);
        Budget updated = budgetRepository.save(budget);

        ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                .workflowCode(generateWorkflowCode())
                .workflowName("Budget Approval - " + budget.getBudgetCode())
                .workflowType(ApprovalWorkflow.WorkflowType.BUDGET_APPROVAL)
                .entityType("Budget")
                .entityId(budget.getId())
                .entityCode(budget.getBudgetCode())
                .currentStep(ApprovalWorkflow.ApprovalStep.LEVEL_1)
                .status(ApprovalWorkflow.WorkflowStatus.PENDING)
                .requesterId(requesterId)
                .requesterName(requesterId)
                .submittedAt(LocalDateTime.now())
                .currentApproverId(getFirstLevelApprover())
                .build();

        approvalWorkflowRepository.save(workflow);

        createAuditLog(updated.getId(), "Budget", updated.getId(), "SUBMIT_APPROVAL",
                null, updated.toString(), requesterId, "Budget submitted for approval");

        return BudgetDTO.fromEntity(updated);
    }

    @Override
    public List<BudgetDTO> getPendingApprovalsForUser(String approverId) {
        return approvalWorkflowRepository.findPendingApprovalsForUser(approverId).stream()
                .map(w -> budgetRepository.findById(w.getEntityId()))
                .filter(Optional::isPresent)
                .map(opt -> BudgetDTO.fromEntity(opt.get()))
                .collect(Collectors.toList());
    }

    private String generateBudgetCode() {
        return "BUD" + LocalDateTime.now().format(CODE_FORMATTER);
    }

    private String generateWorkflowCode() {
        return "WF" + LocalDateTime.now().format(CODE_FORMATTER);
    }

    private String getFirstLevelApprover() {
        return "FINANCE_MANAGER";
    }

    private ApprovalWorkflow createOrUpdateWorkflow(Budget budget, String approverId, ApprovalWorkflow.WorkflowStatus status) {
        List<ApprovalWorkflow> workflows = approvalWorkflowRepository.findByEntityTypeAndEntityId("Budget", budget.getId());
        ApprovalWorkflow workflow = workflows.isEmpty() ? null : workflows.get(workflows.size() - 1);

        if (workflow == null) {
            workflow = ApprovalWorkflow.builder()
                    .workflowCode(generateWorkflowCode())
                    .build();
        }

        workflow.setCurrentApproverId(approverId);
        workflow.setStatus(status);

        return workflow;
    }

    private void createAuditLog(Long entityId, String entityType, Long refId, String operationType,
                                String oldValue, String newValue, String operatorId, String changeSummary) {
        FinanceAuditLog auditLog = FinanceAuditLog.builder()
                .entityType(entityType)
                .entityId(refId)
                .operationType(operationType)
                .operatorId(operatorId != null ? operatorId : "SYSTEM")
                .operationTime(LocalDateTime.now())
                .severity(FinanceAuditLog.Severity.INFO)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeSummary(changeSummary)
                .build();

        auditLogRepository.save(auditLog);
    }
}
