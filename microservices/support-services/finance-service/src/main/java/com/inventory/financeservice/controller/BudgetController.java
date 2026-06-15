package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.BudgetDTO;
import com.inventory.financeservice.exception.ResourceNotFoundException;
import com.inventory.financeservice.service.IBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/budgets")
@Tag(name = "Budget", description = "预算管理接口")
@RequiredArgsConstructor
@Validated
public class BudgetController {
    private final IBudgetService budgetService;

    /**
     * 创建预算记录
     *
     * @param budgetDTO 预算数据
     * @return 创建的预算信息
     */
    @PostMapping
    @Operation(summary = "创建预算记录")
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO createdBudget = budgetService.createBudget(budgetDTO);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取预算记录
     *
     * @param id 预算ID
     * @return 预算信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取预算记录")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable Long id) {
        BudgetDTO budgetDTO = budgetService.getBudgetById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        return ResponseEntity.ok(budgetDTO);
    }

    /**
     * 获取所有预算记录
     *
     * @return 预算列表
     */
    @GetMapping
    @Operation(summary = "获取所有预算记录")
    public ResponseEntity<List<BudgetDTO>> getAllBudgets() {
        List<BudgetDTO> budgetDTOs = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 更新预算记录
     *
     * @param id 预算ID
     * @param budgetDTO 更新后的预算数据
     * @return 更新后的预算信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新预算记录")
    public ResponseEntity<BudgetDTO> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO updatedBudget = budgetService.updateBudget(id, budgetDTO);
        return ResponseEntity.ok(updatedBudget);
    }

    /**
     * 删除预算记录
     *
     * @param id 预算ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算记录")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.cancelBudget(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据状态获取预算记录
     *
     * @param status 预算状态
     * @return 预算列表
     */
    @GetMapping("/by-status")
    @Operation(summary = "根据状态获取预算记录")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByStatus(@RequestParam String status) {
        List<BudgetDTO> budgetDTOs = budgetService.getBudgetsByStatus(status);
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 根据部门获取预算记录
     *
     * @param departmentId 部门ID
     * @return 预算列表
     */
    @GetMapping("/by-department")
    @Operation(summary = "根据部门获取预算记录")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByDepartment(@RequestParam Long departmentId) {
        List<BudgetDTO> budgetDTOs = budgetService.getBudgetsByDepartment(departmentId);
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 根据项目获取预算记录
     *
     * @param projectId 项目ID
     * @return 预算列表
     */
    @GetMapping("/by-project")
    @Operation(summary = "根据项目获取预算记录")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByProject(@RequestParam Long projectId) {
        List<BudgetDTO> budgetDTOs = budgetService.getBudgetsByProject(projectId);
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 根据财年获取预算记录
     *
     * @param fiscalYear 财年
     * @return 预算列表
     */
    @GetMapping("/by-fiscal-year")
    @Operation(summary = "根据财年获取预算记录")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByFiscalYear(@RequestParam Integer fiscalYear) {
        List<BudgetDTO> budgetDTOs = budgetService.getBudgetsByFiscalYear(fiscalYear);
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 获取接近限额的预算记录
     *
     * @return 预算列表
     */
    @GetMapping("/near-limit")
    @Operation(summary = "获取接近限额的预算记录")
    public ResponseEntity<List<BudgetDTO>> getBudgetsNearLimit() {
        List<BudgetDTO> budgetDTOs = budgetService.getBudgetsNearLimit();
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 获取已耗尽的预算记录
     *
     * @return 预算列表
     */
    @GetMapping("/exhausted")
    @Operation(summary = "获取已耗尽的预算记录")
    public ResponseEntity<List<BudgetDTO>> getExhaustedBudgets() {
        List<BudgetDTO> budgetDTOs = budgetService.getExhaustedBudgets();
        return ResponseEntity.ok(budgetDTOs);
    }

    /**
     * 获取预算利用率
     *
     * @param departmentId 部门ID
     * @param fiscalYear 财年
     * @return 预算利用率
     */
    @GetMapping("/utilization-rate")
    @Operation(summary = "获取预算利用率")
    public ResponseEntity<BigDecimal> getBudgetUtilizationRate(
            @RequestParam Long departmentId,
            @RequestParam Integer fiscalYear) {
        BigDecimal rate = budgetService.getBudgetUtilizationRate(departmentId, fiscalYear);
        return ResponseEntity.ok(rate);
    }

    /**
     * 审批通过预算
     *
     * @param id 预算ID
     * @param approverId 审批人ID
     * @return 审批后的预算信息
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过预算")
    public ResponseEntity<BudgetDTO> approveBudget(
            @PathVariable Long id,
            @RequestParam String approverId) {
        BudgetDTO approvedBudget = budgetService.approveBudget(id, approverId);
        return ResponseEntity.ok(approvedBudget);
    }

    /**
     * 驳回预算
     *
     * @param id 预算ID
     * @param rejectorId 驳回人ID
     * @param reason 驳回原因
     * @return 驳回后的预算信息
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回预算")
    public ResponseEntity<BudgetDTO> rejectBudget(
            @PathVariable Long id,
            @RequestParam String rejectorId,
            @RequestParam String reason) {
        BudgetDTO rejectedBudget = budgetService.rejectBudget(id, rejectorId, reason);
        return ResponseEntity.ok(rejectedBudget);
    }

    /**
     * 提交预算审批
     *
     * @param id 预算ID
     * @param requesterId 申请人ID
     * @return 提交后的预算信息
     */
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交预算审批")
    public ResponseEntity<BudgetDTO> submitForApproval(
            @PathVariable Long id,
            @RequestParam String requesterId) {
        BudgetDTO submittedBudget = budgetService.submitForApproval(id, requesterId);
        return ResponseEntity.ok(submittedBudget);
    }

    /**
     * 更新预算执行
     *
     * @param id 预算ID
     * @param usedAmount 已使用金额
     * @return 更新后的预算信息
     */
    @PutMapping("/{id}/execution")
    @Operation(summary = "更新预算执行")
    public ResponseEntity<BudgetDTO> updateBudgetExecution(
            @PathVariable Long id,
            @RequestParam BigDecimal usedAmount) {
        BudgetDTO updatedBudget = budgetService.updateBudgetExecution(id, usedAmount);
        return ResponseEntity.ok(updatedBudget);
    }
}
