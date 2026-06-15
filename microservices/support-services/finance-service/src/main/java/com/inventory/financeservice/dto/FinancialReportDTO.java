package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.FinancialReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportDTO {

    private Long id;
    private String reportCode;
    private String reportType;
    private String reportName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String periodType;
    private Integer fiscalYear;
    private String reportContent;
    private String summaryData;
    private String generatedBy;
    private String approvedBy;
    private String status;
    private LocalDateTime approvedAt;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FinancialReportDTO fromEntity(FinancialReport report) {
        if (report == null) return null;

        return FinancialReportDTO.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .reportType(report.getReportType() != null ? report.getReportType().name() : null)
                .reportName(report.getReportName())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .periodType(report.getPeriodType() != null ? report.getPeriodType().name() : null)
                .fiscalYear(report.getFiscalYear())
                .reportContent(report.getReportContent())
                .summaryData(report.getSummaryData())
                .generatedBy(report.getGeneratedBy())
                .approvedBy(report.getApprovedBy())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .approvedAt(report.getApprovedAt())
                .remarks(report.getRemarks())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    public FinancialReport toEntity() {
        return FinancialReport.builder()
                .id(this.id)
                .reportCode(this.reportCode)
                .reportType(this.reportType != null ? FinancialReport.ReportType.valueOf(this.reportType) : null)
                .reportName(this.reportName)
                .periodStart(this.periodStart)
                .periodEnd(this.periodEnd)
                .periodType(this.periodType != null ? FinancialReport.PeriodType.valueOf(this.periodType) : null)
                .fiscalYear(this.fiscalYear)
                .reportContent(this.reportContent)
                .summaryData(this.summaryData)
                .generatedBy(this.generatedBy)
                .approvedBy(this.approvedBy)
                .status(this.status != null ? FinancialReport.ReportStatus.valueOf(this.status) : FinancialReport.ReportStatus.DRAFT)
                .approvedAt(this.approvedAt)
                .remarks(this.remarks)
                .build();
    }
}
