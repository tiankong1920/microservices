package com.inventory.reportservice.controller;

import com.inventory.reportservice.dto.ReportRequestDTO;
import com.inventory.reportservice.dto.ReportResponseDTO;
import com.inventory.reportservice.dto.ReportQueryDTO;
import com.inventory.reportservice.service.IReportService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

/**
 * 报表控制器 - 处理报表的生成、查询、下载和管理
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Report", description = "报表管理接口")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final IReportService reportService;

    /**
     * 生成报表
     *
     * @param request 报表请求数据
     * @return 创建的报表响应数据
     */
    @PostMapping
    public ResponseEntity<ReportResponseDTO> generateReport(@Valid @RequestBody ReportRequestDTO request) {
        try {
            ReportResponseDTO response = reportService.generateReport(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<ReportResponseDTO>build();
        }
    }

    /**
     * 查询报表列表
     *
     * @param reportName 报表名称（可选）
     * @param reportType 报表类型（可选）
     * @param status 报表状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 报表列表
     */
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> queryReports(
            @RequestParam(required = false) String reportName,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        ReportQueryDTO query = ReportQueryDTO.builder()
                .reportName(reportName)
                .reportType(reportType)
                .reportStatus(status)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();

        List<ReportResponseDTO> reports = reportService.queryReports(query);

        return ResponseEntity.ok(reports);
    }

    /**
     * 根据ID获取报表
     *
     * @param id 报表ID
     * @return 报表响应数据
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable Long id) {
        try {
            ReportResponseDTO response = reportService.getReportById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 获取报表内容
     *
     * @param id 报表ID
     * @return 报表内容
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<String> getReportContent(@PathVariable Long id) {
        try {
            String content = reportService.getReportContent(id);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("报表内容获取失败: " + e.getMessage());
        }
    }

    /**
     * 导出报表
     *
     * @param id 报表ID
     * @param format 导出格式（默认PDF）
     * @return 导出文件路径
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "PDF") String format) {

        try {
            String filePath = reportService.exportReport(id, null);
            return ResponseEntity.ok(filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<String>build();
        }
    }

    /**
     * 删除报表
     *
     * @param id 报表ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.noContent().<Void>build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build();
        }
    }

    /**
     * 获取报表统计
     *
     * @param reportType 报表类型
     * @return 报表统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<IReportService.ReportStatistics> getReportStatistics(
            @RequestParam String reportType) {

        try {
            IReportService.ReportStatistics statistics = reportService.getReportStatistics(reportType);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
