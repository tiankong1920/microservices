package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;

    @Column(name = "summary_data", columnDefinition = "TEXT")
    private String summaryData;

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReportType {
        BALANCE_SHEET, INCOME_STATEMENT, CASH_FLOW_STATEMENT,
        CUSTOM_ANALYSIS, TAX_REPORT, AUDIT_REPORT
    }

    public enum PeriodType {
        MONTHLY, QUARTERLY, YEARLY
    }

    public enum ReportStatus {
        DRAFT, GENERATED, REVIEWING, APPROVED, PUBLISHED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
