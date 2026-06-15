package com.inventory.reportservice.controller;

import com.inventory.reportservice.dto.ReportQueryDTO;
import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.service.IReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReportControllerTest {

    @Mock
    private IReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private ReportResponseDTO testReport;

    @BeforeEach
    void setUp() {
        testReport = new ReportResponseDTO();
        testReport.setReportId(1L);
        testReport.setReportName("Inventory Report");
    }

    @Test
    void testGenerateReportSuccess() {
        when(reportService.generateReport(any(ReportRequestDTO.class))).thenReturn(testReport);

        ResponseEntity<ReportResponseDTO> response = reportController.generateReport(new ReportRequestDTO());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGenerateReportError() {
        when(reportService.generateReport(any(ReportRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<ReportResponseDTO> response = reportController.generateReport(new ReportRequestDTO());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testQueryReports() {
        when(reportService.queryReports(any(ReportQueryDTO.class))).thenReturn(List.of(testReport));

        ResponseEntity<List<ReportResponseDTO>> response = reportController.queryReports(
                null, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetReportById() {
        when(reportService.getReportById(1L)).thenReturn(testReport);

        ResponseEntity<ReportResponseDTO> response = reportController.getReportById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportService.getReportById(1L)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<ReportResponseDTO> response = reportController.getReportById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetReportContent() {
        when(reportService.getReportContent(1L)).thenReturn("report-content");

        ResponseEntity<String> response = reportController.getReportContent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("report-content", response.getBody());
    }

    @Test
    void testExportReport() {
        when(reportService.exportReport(1L, null)).thenReturn("/tmp/report.pdf");

        ResponseEntity<String> response = reportController.exportReport(1L, "PDF");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("/tmp/report.pdf", response.getBody());
    }

    @Test
    void testDeleteReport() {
        doNothing().when(reportService).deleteReport(1L);

        ResponseEntity<Void> response = reportController.deleteReport(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reportService).deleteReport(1L);
    }

    @Test
    void testDeleteReportNotFound() {
        doThrow(new RuntimeException("not found")).when(reportService).deleteReport(1L);

        ResponseEntity<Void> response = reportController.deleteReport(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetReportStatistics() {
        IReportService.ReportStatistics stats = new IReportService.ReportStatistics();
        when(reportService.getReportStatistics("INVENTORY")).thenReturn(stats);

        ResponseEntity<IReportService.ReportStatistics> response =
                reportController.getReportStatistics("INVENTORY");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
