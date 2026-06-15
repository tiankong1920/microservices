package com.inventory.reportservice.service.impl;

import com.inventory.reportservice.dto.ReportQueryDTO;
import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.entity.Report;
import com.inventory.reportservice.entity.ReportFormat;
import com.inventory.reportservice.entity.ReportStatus;
import com.inventory.reportservice.entity.ReportType;
import com.inventory.reportservice.repository.IReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.modelmapper.ModelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReportServiceImplTest {

    @Mock
    private IReportRepository reportRepository;

    @Mock
    private ModelMapper modelMapper;

    private ReportServiceImpl reportService;

    private Report testReport;
    private ReportRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportRepository, modelMapper);
        testReport = new Report();
        testReport.setId(1L);
        testReport.setReportName("Test Report");
        testReport.setReportType(ReportType.SALES_REPORT);
        testReport.setReportStatus(ReportStatus.COMPLETED);
        testReport.setReportData("{\"data\": \"test\"}");
        testReport.setGeneratedBy("admin");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setUpdatedAt(LocalDateTime.now());

        testRequest = ReportRequestDTO.builder()
                .reportName("Test Report")
                .reportType("SALES_REPORT")
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testGenerateReport() {
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1L);
            return report;
        });

        ReportResponseDTO result = reportService.generateReport(testRequest);

        assertNotNull(result);
        assertEquals("Test Report", result.getReportName());
        assertEquals(ReportStatus.GENERATING, result.getStatus());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testQueryReports() {
        when(reportRepository.findAll()).thenReturn(List.of(testReport));

        ReportQueryDTO query = ReportQueryDTO.builder().build();
        List<ReportResponseDTO> result = reportService.queryReports(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Report", result.get(0).getReportName());
    }

    @Test
    void testGetReportById() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        ReportResponseDTO result = reportService.getReportById(1L);

        assertNotNull(result);
        assertEquals("Test Report", result.getReportName());
        assertEquals(ReportType.SALES_REPORT, result.getReportType());
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reportService.getReportById(999L));
    }

    @Test
    void testGetReportContent() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        String content = reportService.getReportContent(1L);

        assertEquals("{\"data\": \"test\"}", content);
    }

    @Test
    void testGetReportContentNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reportService.getReportContent(999L));
    }

    @Test
    void testExportReport() {
        String url = reportService.exportReport(1L, ReportFormat.PDF);

        assertTrue(url.contains("1.pdf"));
        assertTrue(url.startsWith("/api/reports/download/"));
    }

    @Test
    void testGetReportStatistics() {
        Report completedReport = new Report();
        completedReport.setReportStatus(ReportStatus.COMPLETED);

        Report generatingReport = new Report();
        generatingReport.setReportStatus(ReportStatus.GENERATING);

        Report failedReport = new Report();
        failedReport.setReportStatus(ReportStatus.FAILED);

        when(reportRepository.findByReportType(ReportType.SALES_REPORT))
                .thenReturn(List.of(completedReport, generatingReport, failedReport));

        var stats = reportService.getReportStatistics("SALES_REPORT");

        assertEquals(3, stats.getTotalCount());
        assertEquals(1, stats.getCompletedCount());
        assertEquals(1, stats.getPendingCount());
        assertEquals(1, stats.getFailedCount());
    }

    @Test
    void testDeleteReport() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        reportService.deleteReport(1L);

        verify(reportRepository).delete(testReport);
    }

    @Test
    void testDeleteReportNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reportService.deleteReport(999L));
    }
}
