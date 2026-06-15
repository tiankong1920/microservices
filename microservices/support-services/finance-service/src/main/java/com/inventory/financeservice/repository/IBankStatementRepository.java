package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBankStatementRepository extends JpaRepository<BankStatement, Long> {

    Optional<BankStatement> findByStatementNumber(String statementNumber);

    List<BankStatement> findByAccountNumber(String accountNumber);

    List<BankStatement> findByReconciliationStatus(BankStatement.ReconciliationStatus status);

    List<BankStatement> findByImportBatchId(String importBatchId);

    @Query("SELECT b FROM BankStatement b WHERE b.accountNumber = :accountNumber AND b.transactionDate >= :startDate AND b.transactionDate <= :endDate")
    List<BankStatement> findByAccountAndDateRange(@Param("accountNumber") String accountNumber, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM BankStatement b WHERE b.referenceNumber = :reference AND b.reconciliationStatus = 'UNRECONCILED'")
    List<BankStatement> findByReferenceAndUnreconciled(@Param("reference") String reference);

    @Query("SELECT SUM(b.transactionAmount) FROM BankStatement b WHERE b.accountNumber = :accountNumber AND b.transactionDate >= :startDate AND b.transactionDate <= :endDate AND b.transactionType = :type")
    BigDecimal sumAmountByAccountAndTypeAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") String transactionType);

    @Query("SELECT b FROM BankStatement b WHERE b.reconciliationStatus = 'UNRECONCILED' AND b.transactionDate >= :startDate")
    List<BankStatement> findUnreconciledStatements(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(b) FROM BankStatement b WHERE b.importBatchId = :batchId")
    Long countByImportBatch(@Param("batchId") String batchId);
}
