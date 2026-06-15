package com.inventory.reportservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportQueryDTO {
    private String reportName;
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportStatus;
    private String generatedBy;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
