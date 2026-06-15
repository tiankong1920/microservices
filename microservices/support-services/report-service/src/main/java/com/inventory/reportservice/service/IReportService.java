package com.inventory.reportservice.service;

import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.dto.ReportQueryDTO;
import com.inventory.reportservice.entity.ReportFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

public interface IReportService {
    
    ReportResponseDTO generateReport(ReportRequestDTO request);
    
    List<ReportResponseDTO> queryReports(ReportQueryDTO query);
    
    ReportResponseDTO getReportById(Long id);
    
    void deleteReport(Long id);
    
    String getReportContent(Long id);
    
    String exportReport(Long id, ReportFormat format);
    
    ReportStatistics getReportStatistics(String reportType);
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ReportStatistics {
        private long totalCount;
        private long completedCount;
        private long pendingCount;
        private long failedCount;
        private long expiredCount;
    }
}
