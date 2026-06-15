package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.BudgetDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IBudgetService {

    BudgetDTO createBudget(BudgetDTO budgetDTO);

    BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO);

    Optional<BudgetDTO> getBudgetById(Long id);

    Optional<BudgetDTO> getBudgetByCode(String budgetCode);

    List<BudgetDTO> getAllBudgets();

    List<BudgetDTO> getBudgetsByStatus(String status);

    List<BudgetDTO> getBudgetsByDepartment(Long departmentId);

    List<BudgetDTO> getBudgetsByProject(Long projectId);

    List<BudgetDTO> getBudgetsByFiscalYear(Integer fiscalYear);

    BudgetDTO approveBudget(Long id, String approverId);

    BudgetDTO rejectBudget(Long id, String rejectorId, String reason);

    BudgetDTO cancelBudget(Long id);

    BudgetDTO updateBudgetExecution(Long id, BigDecimal usedAmount);

    List<BudgetDTO> getBudgetsNearLimit();

    List<BudgetDTO> getExhaustedBudgets();

    BigDecimal getBudgetUtilizationRate(Long departmentId, Integer fiscalYear);

    BudgetDTO submitForApproval(Long id, String requesterId);

    List<BudgetDTO> getPendingApprovalsForUser(String approverId);
}
