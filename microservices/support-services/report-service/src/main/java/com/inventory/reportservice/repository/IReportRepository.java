package com.inventory.reportservice.repository;

import com.inventory.reportservice.entity.Report;
import com.inventory.reportservice.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportType(ReportType reportType);
    List<Report> findByReportTypeAndReportDateBetween(
            ReportType reportType, LocalDateTime startDate, LocalDateTime endDate);
}
