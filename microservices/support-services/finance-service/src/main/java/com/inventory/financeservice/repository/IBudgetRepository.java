package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByBudgetCode(String budgetCode);

    List<Budget> findByStatus(Budget.BudgetStatus status);

    List<Budget> findByFiscalYear(Integer fiscalYear);

    List<Budget> findByDepartmentId(Long departmentId);

    List<Budget> findByProjectId(Long projectId);

    List<Budget> findByBudgetType(Budget.BudgetType budgetType);

    @Query("SELECT b FROM Budget b WHERE b.fiscalYear = :year AND b.budgetPeriod = :period")
    List<Budget> findByFiscalYearAndPeriod(@Param("year") Integer year, @Param("period") Budget.BudgetPeriod period);

    @Query("SELECT b FROM Budget b WHERE b.status = :status AND b.periodStart <= :date AND b.periodEnd >= :date")
    List<Budget> findActiveBudgetsByDate(@Param("status") Budget.BudgetStatus status, @Param("date") LocalDateTime date);

    @Query("SELECT b FROM Budget b WHERE b.remainingAmount < (b.totalAmount * b.warningThreshold / 100)")
    List<Budget> findBudgetsNearLimit();

    @Query("SELECT b FROM Budget b WHERE b.remainingAmount <= 0")
    List<Budget> findExhaustedBudgets();

    @Query("SELECT SUM(b.usedAmount) FROM Budget b WHERE b.departmentId = :deptId AND b.fiscalYear = :year")
    BigDecimal sumUsedAmountByDepartmentAndYear(@Param("deptId") Long departmentId, @Param("year") Integer year);

    @Query("SELECT SUM(b.totalAmount) FROM Budget b WHERE b.fiscalYear = :year")
    BigDecimal sumTotalBudgetByYear(@Param("year") Integer year);

    @Query("SELECT b FROM Budget b WHERE b.expenseCategory = :category AND b.fiscalYear = :year")
    List<Budget> findByExpenseCategoryAndYear(@Param("category") String category, @Param("year") Integer year);
}
