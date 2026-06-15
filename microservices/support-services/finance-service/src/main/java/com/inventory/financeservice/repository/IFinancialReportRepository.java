package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFinancialReportRepository extends JpaRepository<FinancialReport, Long> {

    Optional<FinancialReport> findByReportCode(String reportCode);

    List<FinancialReport> findByReportType(FinancialReport.ReportType reportType);

    List<FinancialReport> findByStatus(FinancialReport.ReportStatus status);

    List<FinancialReport> findByFiscalYear(Integer fiscalYear);

    @Query("SELECT r FROM FinancialReport r WHERE r.periodStart >= :startDate AND r.periodEnd <= :endDate")
    List<FinancialReport> findByPeriodRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM FinancialReport r WHERE r.reportType = :type AND r.fiscalYear = :year AND r.periodType = :periodType")
    List<FinancialReport> findByTypeAndYearAndPeriod(
            @Param("type") FinancialReport.ReportType reportType,
            @Param("year") Integer fiscalYear,
            @Param("periodType") FinancialReport.PeriodType periodType);

    @Query("SELECT r FROM FinancialReport r WHERE r.generatedBy = :userId AND r.status = :status")
    List<FinancialReport> findByGeneratedByAndStatus(@Param("userId") String userId, @Param("status") FinancialReport.ReportStatus status);

    List<FinancialReport> findByApprovedBy(String approvedBy);
}
