package com.inventory.reportservice.dto;

import com.inventory.reportservice.entity.ReportStatus;
import com.inventory.reportservice.entity.ReportType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDTO {
    private Long reportId;
    private String reportName;
    private ReportType reportType;
    private ReportStatus status;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private java.math.BigDecimal totalAmount;
    private Long totalQuantity;
    private ReportStatus reportStatus;
    private String generatedBy;
    private String reportData;
    private LocalDateTime generatedAt;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
