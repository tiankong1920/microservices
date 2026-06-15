package com.inventory.reportservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long id;
    private String reportName;
    private String reportType;
    private LocalDateTime reportDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalAmount;
    private Long totalQuantity;
    private String reportStatus;
    private String generatedBy;
    private String reportData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
