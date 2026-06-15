package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.entity.Expense;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.entity.Income;
import com.inventory.financeservice.repository.IExpenseRepository;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.repository.IIncomeRepository;
import com.inventory.financeservice.repository.ISettlementAccountRepository;
import com.inventory.financeservice.service.IIncomeExpenseService;
import com.inventory.financeservice.service.IIncomeService;
import com.inventory.financeservice.service.ISettlementAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class IncomeExpenseServiceImpl implements IIncomeExpenseService, IIncomeService {

    private static final String LOG_INCOME_CREATING = "Creating income record: {}";
    private static final String LOG_INCOME_UPDATED = "Income updated with id: {}";
    private static final String LOG_INCOME_NOT_FOUND = "Income not found with id: {}";
    private static final String LOG_EXPENSE_CREATING = "Creating expense record: {}";
    private static final String LOG_EXPENSE_UPDATED = "Expense updated with id: {}";
    private static final String LOG_EXPENSE_NOT_FOUND = "Expense not found with id: {}";

    private final IIncomeRepository incomeRepository;
    private final IExpenseRepository expenseRepository;
    private final ISettlementAccountRepository accountRepository;
    private final IFinanceAuditLogRepository auditLogRepository;
    private final ISettlementAccountService settlementAccountService;

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public IncomeDTO createIncome(IncomeDTO incomeDTO) {
        log.info(LOG_INCOME_CREATING, incomeDTO.getIncomeAmount());

        Income income = incomeDTO.toEntity();
        if (income.getIncomeNumber() == null || income.getIncomeNumber().isEmpty()) {
            income.setIncomeNumber(generateDocumentNumber("INC"));
        }
        income.setStatus(IncomeDTO.STATUS_PENDING);
        income.setApprovalStatus(IncomeDTO.STATUS_PENDING);

        Income saved = incomeRepository.save(income);

        createAuditLog(saved.getId(), "Income", saved.getId(), "CREATE",
                null, saved.toString(), saved.getCreatedBy(), "Income record created");

        return IncomeDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO) {
        Income existing = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));

        String oldValue = existing.toString();

        existing.setIncomeDate(incomeDTO.getIncomeDate());
        existing.setIncomeAmount(incomeDTO.getIncomeAmount());
        existing.setIncomeType(incomeDTO.getIncomeType());
        existing.setIncomeCategory(incomeDTO.getIncomeCategory());
        existing.setSettlementAccountId(incomeDTO.getSettlementAccountId());
        existing.setSettlementAccountName(incomeDTO.getSettlementAccountName());
        existing.setCounterpartyName(incomeDTO.getCounterpartyName());
        existing.setProjectId(incomeDTO.getProjectId());
        existing.setProjectName(incomeDTO.getProjectName());
        existing.setDepartmentId(incomeDTO.getDepartmentId());
        existing.setDepartmentName(incomeDTO.getDepartmentName());
        existing.setInvoiceNumber(incomeDTO.getInvoiceNumber());
        existing.setDescription(incomeDTO.getDescription());
        existing.setNotes(incomeDTO.getNotes());

        Income updated = incomeRepository.save(existing);

        createAuditLog(updated.getId(), "Income", updated.getId(), "UPDATE",
                oldValue, updated.toString(), incomeDTO.getUpdatedBy(), "Income record updated");

        return IncomeDTO.fromEntity(updated);
    }

    @Override
    public Optional<IncomeDTO> getIncomeById(Long id) {
        return incomeRepository.findById(id).map(IncomeDTO::fromEntity);
    }

    @Override
    public Optional<IncomeDTO> getIncomeByNumber(String incomeNumber) {
        return incomeRepository.findByIncomeNumber(incomeNumber).map(IncomeDTO::fromEntity);
    }

    @Override
    public List<IncomeDTO> getAllIncomes() {
        return incomeRepository.findAll().stream().map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<IncomeDTO> getIncomesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return incomeRepository.findByIncomeDateBetween(startDate, endDate).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<IncomeDTO> getIncomesByType(String incomeType) {
        return incomeRepository.findByIncomeType(incomeType).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<IncomeDTO> getIncomesByStatus(String status) {
        return incomeRepository.findByStatus(status).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<IncomeDTO> getIncomesBySettlementAccount(Long accountId) {
        return incomeRepository.findBySettlementAccountId(accountId).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<IncomeDTO> searchIncomes(String keyword) {
        return incomeRepository.searchByKeyword(keyword).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncomeDTO approveIncome(Long id, String approverId) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));

        income.setStatus(IncomeDTO.STATUS_APPROVED);
        income.setApprovalStatus(IncomeDTO.STATUS_APPROVED);
        income.setApproverId(approverId);
        income.setApprovedAt(LocalDateTime.now());

        Income updated = incomeRepository.save(income);

        if (income.getSettlementAccountId() != null) {
            settlementAccountService.updateBalance(income.getSettlementAccountId(),
                    income.getIncomeAmount(), "DEPOSIT");
        }

        createAuditLog(updated.getId(), "Income", updated.getId(), "APPROVE",
                null, updated.toString(), approverId, "Income approved");

        return IncomeDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public IncomeDTO rejectIncome(Long id, String rejectorId, String reason) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));

        income.setStatus(IncomeDTO.STATUS_REJECTED);
        income.setApprovalStatus(IncomeDTO.STATUS_REJECTED);
        income.setApproverId(rejectorId);
        income.setRejectionReason(reason);

        Income updated = incomeRepository.save(income);

        createAuditLog(updated.getId(), "Income", updated.getId(), "REJECT",
                null, updated.toString(), rejectorId, "Income rejected: " + reason);

        return IncomeDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public IncomeDTO cancelIncome(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));

        income.setStatus(IncomeDTO.STATUS_CANCELLED);

        Income updated = incomeRepository.save(income);

        createAuditLog(updated.getId(), "Income", updated.getId(), "CANCEL",
                null, updated.toString(), updated.getCreatedBy(), "Income cancelled");

        return IncomeDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        log.info("Creating expense record: {}", expenseDTO.getExpenseAmount());

        Expense expense = expenseDTO.toEntity();
        if (expense.getExpenseNumber() == null || expense.getExpenseNumber().isEmpty()) {
            expense.setExpenseNumber(generateDocumentNumber("EXP"));
        }
        expense.setStatus(ExpenseDTO.STATUS_PENDING);
        expense.setApprovalStatus(ExpenseDTO.STATUS_PENDING);

        Expense saved = expenseRepository.save(expense);

        createAuditLog(saved.getId(), "Expense", saved.getId(), "CREATE",
                null, saved.toString(), saved.getCreatedBy(), "Expense record created");

        return ExpenseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        String oldValue = existing.toString();

        existing.setExpenseDate(expenseDTO.getExpenseDate());
        existing.setExpenseAmount(expenseDTO.getExpenseAmount());
        existing.setExpenseType(expenseDTO.getExpenseType());
        existing.setExpenseCategory(expenseDTO.getExpenseCategory());
        existing.setSettlementAccountId(expenseDTO.getSettlementAccountId());
        existing.setSettlementAccountName(expenseDTO.getSettlementAccountName());
        existing.setCounterpartyName(expenseDTO.getCounterpartyName());
        existing.setProjectId(expenseDTO.getProjectId());
        existing.setProjectName(expenseDTO.getProjectName());
        existing.setDepartmentId(expenseDTO.getDepartmentId());
        existing.setDepartmentName(expenseDTO.getDepartmentName());
        existing.setInvoiceNumber(expenseDTO.getInvoiceNumber());
        existing.setNotes(expenseDTO.getNotes());

        Expense updated = expenseRepository.save(existing);

        createAuditLog(updated.getId(), "Expense", updated.getId(), "UPDATE",
                oldValue, updated.toString(), expenseDTO.getUpdatedBy(), "Expense record updated");

        return ExpenseDTO.fromEntity(updated);
    }

    @Override
    public Optional<ExpenseDTO> getExpenseById(Long id) {
        return expenseRepository.findById(id).map(ExpenseDTO::fromEntity);
    }

    @Override
    public Optional<ExpenseDTO> getExpenseByNumber(String expenseNumber) {
        return expenseRepository.findByExpenseNumber(expenseNumber).map(ExpenseDTO::fromEntity);
    }

    @Override
    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll().stream().map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByType(String expenseType) {
        return expenseRepository.findByExpenseType(expenseType).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByStatus(String status) {
        return expenseRepository.findByStatus(status).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesBySettlementAccount(Long accountId) {
        return expenseRepository.findBySettlementAccountId(accountId).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> searchExpenses(String keyword) {
        return expenseRepository.searchByKeyword(keyword).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExpenseDTO approveExpense(Long id, String approverId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        expense.setStatus(ExpenseDTO.STATUS_APPROVED);
        expense.setApprovalStatus(ExpenseDTO.STATUS_APPROVED);
        expense.setApproverId(approverId);
        expense.setApprovedAt(LocalDateTime.now());

        Expense updated = expenseRepository.save(expense);

        if (expense.getSettlementAccountId() != null) {
            settlementAccountService.updateBalance(expense.getSettlementAccountId(),
                    expense.getExpenseAmount(), "WITHDRAW");
        }

        createAuditLog(updated.getId(), "Expense", updated.getId(), "APPROVE",
                null, updated.toString(), approverId, "Expense approved");

        return ExpenseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public ExpenseDTO rejectExpense(Long id, String rejectorId, String reason) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        expense.setStatus(ExpenseDTO.STATUS_REJECTED);
        expense.setApprovalStatus(ExpenseDTO.STATUS_REJECTED);
        expense.setApproverId(rejectorId);
        expense.setRejectionReason(reason);

        Expense updated = expenseRepository.save(expense);

        createAuditLog(updated.getId(), "Expense", updated.getId(), "REJECT",
                null, updated.toString(), rejectorId, "Expense rejected: " + reason);

        return ExpenseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public ExpenseDTO cancelExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        expense.setStatus(ExpenseDTO.STATUS_CANCELLED);

        Expense updated = expenseRepository.save(expense);

        createAuditLog(updated.getId(), "Expense", updated.getId(), "CANCEL",
                null, updated.toString(), updated.getCreatedBy(), "Expense cancelled");

        return ExpenseDTO.fromEntity(updated);
    }

    @Override
    public BigDecimal getTotalIncomeByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return incomeRepository.sumAmountByDateRange(startDate, endDate);
    }

    @Override
    public BigDecimal getTotalExpenseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.sumAmountByDateRange(startDate, endDate);
    }

    @Override
    public BigDecimal getNetProfitByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalIncome = getTotalIncomeByDateRange(startDate, endDate);
        BigDecimal totalExpense = getTotalExpenseByDateRange(startDate, endDate);
        return totalIncome.subtract(totalExpense);
    }

    @Override
    public List<IncomeDTO> getPendingApprovalIncomes() {
        return incomeRepository.findByStatus(IncomeDTO.STATUS_PENDING).stream()
                .map(IncomeDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getPendingApprovalExpenses() {
        return expenseRepository.findByStatus(ExpenseDTO.STATUS_PENDING).stream()
                .map(ExpenseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<IncomeDTO> findIncomeById(Long id) {
        return getIncomeById(id);
    }

    @Override
    public Optional<IncomeDTO> findIncomeByNumber(String incomeNumber) {
        return getIncomeByNumber(incomeNumber);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id) {
        log.info("Deleting income: {}", id);
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));
        incomeRepository.delete(income);
        createAuditLog(id, "Income", id, "DELETE",
                income.toString(), null, "SYSTEM", "Income record deleted");
    }

    @Override
    public byte[] exportIncomesToExcel() {
        List<Income> incomes = incomeRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("Income Number,Date,Amount,Type,Category,Account,Counterparty,Status\n");
        for (Income inc : incomes) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    inc.getIncomeNumber(),
                    inc.getIncomeDate(),
                    inc.getIncomeAmount(),
                    inc.getIncomeType(),
                    inc.getIncomeCategory(),
                    inc.getSettlementAccountName(),
                    inc.getCounterpartyName(),
                    inc.getStatus()));
        }
        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportIncomesToCsv() {
        return exportIncomesToExcel();
    }

    @Override
    public byte[] exportIncomesToExcel(LocalDateTime startDate, LocalDateTime endDate) {
        List<Income> incomes = incomeRepository.findByIncomeDateBetween(startDate, endDate);
        StringBuilder csv = new StringBuilder();
        csv.append("Income Number,Date,Amount,Type,Category,Account,Counterparty,Status\n");
        for (Income inc : incomes) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    inc.getIncomeNumber(),
                    inc.getIncomeDate(),
                    inc.getIncomeAmount(),
                    inc.getIncomeType(),
                    inc.getIncomeCategory(),
                    inc.getSettlementAccountName(),
                    inc.getCounterpartyName(),
                    inc.getStatus()));
        }
        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportExpensesToExcel(LocalDateTime startDate, LocalDateTime endDate) {
        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        StringBuilder csv = new StringBuilder();
        csv.append("Expense Number,Date,Amount,Type,Category,Account,Counterparty,Status\n");
        for (Expense exp : expenses) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    exp.getExpenseNumber(),
                    exp.getExpenseDate(),
                    exp.getExpenseAmount(),
                    exp.getExpenseType(),
                    exp.getExpenseCategory(),
                    exp.getSettlementAccountName(),
                    exp.getCounterpartyName(),
                    exp.getStatus()));
        }
        return csv.toString().getBytes();
    }

    private String generateDocumentNumber(String prefix) {
        return prefix + LocalDateTime.now().format(CODE_FORMATTER) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
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
