package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.TaxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITaxRecordRepository extends JpaRepository<TaxRecord, Long> {

    Optional<TaxRecord> findByTaxNumber(String taxNumber);

    List<TaxRecord> findByTaxType(TaxRecord.TaxType taxType);

    List<TaxRecord> findByStatus(TaxRecord.TaxStatus status);

    List<TaxRecord> findByTaxTypeAndStatus(TaxRecord.TaxType taxType, TaxRecord.TaxStatus status);

    @Query("SELECT t FROM TaxRecord t WHERE t.taxablePeriodStart >= :startDate AND t.taxablePeriodEnd <= :endDate")
    List<TaxRecord> findByTaxablePeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TaxRecord t WHERE t.dueDate < :date AND t.status != 'PAID'")
    List<TaxRecord> findOverdueTaxes(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(t.taxAmount) FROM TaxRecord t WHERE t.taxType = :type AND t.status = :status")
    BigDecimal sumTaxAmountByTypeAndStatus(@Param("type") TaxRecord.TaxType type, @Param("status") TaxRecord.TaxStatus status);

    @Query("SELECT SUM(t.inputTaxAmount) FROM TaxRecord t WHERE t.taxablePeriodStart >= :startDate AND t.taxablePeriodEnd <= :endDate")
    BigDecimal sumInputTaxByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.outputTaxAmount) FROM TaxRecord t WHERE t.taxablePeriodStart >= :startDate AND t.taxablePeriodEnd <= :endDate")
    BigDecimal sumOutputTaxByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TaxRecord t WHERE t.status = 'PENDING' ORDER BY t.dueDate ASC")
    List<TaxRecord> findPendingTaxesOrderByDueDate();
}
