package com.inventory.reportservice.service.impl;

import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.dto.ReportQueryDTO;
import com.inventory.reportservice.entity.Report;
import com.inventory.reportservice.entity.ReportFormat;
import com.inventory.reportservice.entity.ReportStatus;
import com.inventory.reportservice.entity.ReportType;
import com.inventory.reportservice.repository.IReportRepository;
import com.inventory.reportservice.service.IReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"null", "unused"})
public class ReportServiceImpl implements IReportService {
    
    private final IReportRepository reportRepository;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional
    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        log.info("Generating report: {}", request.getReportName());
        
        Report report = new Report();
        report.setReportName(request.getReportName());
        report.setReportType(ReportType.valueOf(request.getReportType()));
        report.setStartDate(request.getStartDate());
        report.setEndDate(request.getEndDate());
        report.setReportStatus(ReportStatus.GENERATING);
        report.setGeneratedBy("system");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        Report savedReport = reportRepository.save(report);
        
        ReportResponseDTO response = new ReportResponseDTO();
        response.setReportId(savedReport.getId());
        response.setReportName(savedReport.getReportName());
        response.setReportType(savedReport.getReportType());
        response.setStatus(savedReport.getReportStatus());
        
        log.info("Report generated successfully, ID: {}", savedReport.getId());
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> queryReports(ReportQueryDTO query) {
        log.info("Querying reports");
        
        List<Report> reports = reportRepository.findAll();
        
        return reports.stream()
                .map(report -> {
                    ReportResponseDTO response = new ReportResponseDTO();
                    response.setReportId(report.getId());
                    response.setReportName(report.getReportName());
                    response.setReportType(report.getReportType());
                    response.setStatus(report.getReportStatus());
                    return response;
                })
                .toList();
    }
    
    @Override
    @Transactional
    public ReportResponseDTO getReportById(Long id) {
        log.info("Getting report by id: {}", id);
        
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        
        ReportResponseDTO response = new ReportResponseDTO();
        response.setReportId(report.getId());
        response.setReportName(report.getReportName());
        response.setReportType(report.getReportType());
        response.setStatus(report.getReportStatus());
        
        return response;
    }
    
    @Override
    @Transactional
    public String getReportContent(Long id) {
        log.info("Getting report content, ID: {}", id);
        
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        
        return report.getReportData();
    }
    
    @Override
    @Transactional
    public String exportReport(Long id, ReportFormat format) {
        log.info("Exporting report, ID: {}, format: {}", id, format);
        return "/api/reports/download/" + id + "." + format.name().toLowerCase();
    }
    
    @Override
    @Transactional
    public ReportStatistics getReportStatistics(String reportType) {
        log.info("Getting report statistics for type: {}", reportType);
        
        List<Report> reports = reportRepository.findByReportType(ReportType.valueOf(reportType));
        
        long totalCount = reports.size();
        long completedCount = reports.stream()
                .filter(r -> ReportStatus.COMPLETED.equals(r.getReportStatus()))
                .count();
        long pendingCount = reports.stream()
                .filter(r -> ReportStatus.GENERATING.equals(r.getReportStatus()))
                .count();
        long failedCount = reports.stream()
                .filter(r -> ReportStatus.FAILED.equals(r.getReportStatus()))
                .count();
        
        ReportStatistics statistics = new ReportStatistics();
        statistics.setTotalCount(totalCount);
        statistics.setCompletedCount(completedCount);
        statistics.setPendingCount(pendingCount);
        statistics.setFailedCount(failedCount);
        
        return statistics;
    }
    
    @Override
    @Transactional
    public void deleteReport(Long id) {
        log.info("Deleting report, ID: {}", id);
        
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        
        reportRepository.delete(report);
        
        log.info("Report deleted successfully, ID: {}", id);
    }
}
