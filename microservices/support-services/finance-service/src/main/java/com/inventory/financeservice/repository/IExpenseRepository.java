package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByExpenseNumber(String expenseNumber);

    List<Expense> findByExpenseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Expense> findByExpenseType(String expenseType);

    List<Expense> findBySettlementAccountId(Long settlementAccountId);

    List<Expense> findByStatus(String status);

    List<Expense> findByApprovalStatus(String approvalStatus);

    @Query("SELECT COALESCE(SUM(e.expenseAmount), 0) FROM Expense e " +
           "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.expenseStatus = 'COMPLETED'")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Expense e WHERE " +
           "(LOWER(e.expenseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.counterpartyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Expense> searchByKeyword(@Param("keyword") String keyword);
}
