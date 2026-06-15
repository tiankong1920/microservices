package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IIncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByIncomeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Income> findByIncomeType(String incomeType);
    List<Income> findByIncomeStatus(String incomeStatus);
    Optional<Income> findByIncomeNumber(String incomeNumber);
    List<Income> findBySettlementAccountId(Long settlementAccountId);
    List<Income> findByStatus(String status);
    List<Income> findByApprovalStatus(String approvalStatus);

    @Query("SELECT COALESCE(SUM(i.incomeAmount), 0) FROM Income i " +
           "WHERE i.incomeDate BETWEEN :startDate AND :endDate " +
           "AND i.incomeStatus = 'COMPLETED'")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Income i WHERE " +
           "(LOWER(i.incomeNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.counterpartyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Income> searchByKeyword(@Param("keyword") String keyword);
}
