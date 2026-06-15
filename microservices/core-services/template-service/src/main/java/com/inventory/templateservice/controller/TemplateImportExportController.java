package com.inventory.templateservice.controller;

import com.inventory.common.template.dto.TemplateImportResultDTO;
import com.inventory.common.template.service.ITemplateImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 模板导入导出控制器 - 管理模板的导入导出
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/templates/import-export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "模板导入导出", description = "模板的导入导出接口")
@Validated
public class TemplateImportExportController {

    private final ITemplateImportExportService importExportService;

    @PostMapping("/import/json")
    @Operation(summary = "导入JSON文件", description = "从JSON文件批量导入模板")
    public ResponseEntity<TemplateImportResultDTO> importFromJson(
            @Parameter(description = "JSON文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "增量导入") @RequestParam(defaultValue = "false") boolean incremental) throws IOException {
        log.info("Importing templates from JSON file, incremental: {}", incremental);
        TemplateImportResultDTO result = importExportService.importFromJson(file.getInputStream(), incremental);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/excel")
    @Operation(summary = "导入Excel文件", description = "从Excel文件批量导入模板")
    public ResponseEntity<TemplateImportResultDTO> importFromExcel(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "增量导入") @RequestParam(defaultValue = "false") boolean incremental) throws IOException {
        log.info("Importing templates from Excel file, incremental: {}", incremental);
        TemplateImportResultDTO result = importExportService.importFromExcel(file.getInputStream(), incremental);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证导入文件", description = "验证导入文件的格式和内容")
    public ResponseEntity<TemplateImportResultDTO> validateImportFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型") @RequestParam String fileType) throws IOException {
        log.info("Validating import file, type: {}", fileType);
        TemplateImportResultDTO result = importExportService.validateImportFile(file.getInputStream(), fileType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/json")
    @Operation(summary = "导出为JSON", description = "将指定模板导出为JSON文件")
    public void exportToJson(
            @Parameter(description = "模板ID列表") @RequestParam List<Long> templateIds,
            HttpServletResponse response) throws IOException {
        log.info("Exporting {} templates to JSON", templateIds.size());
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"templates_export.json\"");
        
        importExportService.exportToJson(templateIds, response.getOutputStream());
    }

    @GetMapping("/export/json/domain/{domain}")
    @Operation(summary = "按领域导出JSON", description = "将指定领域的模板导出为JSON")
    public void exportToJsonByDomain(
            @Parameter(description = "业务领域") @PathVariable String domain,
            HttpServletResponse response) throws IOException {
        log.info("Exporting templates for domain: {} to JSON", domain);
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"templates_" + domain + "_export.json\"");
        
        importExportService.exportToJson(domain, response.getOutputStream());
    }

    @GetMapping("/export/excel")
    @Operation(summary = "导出为Excel", description = "将指定模板导出为Excel文件")
    public void exportToExcel(
            @Parameter(description = "模板ID列表") @RequestParam List<Long> templateIds,
            HttpServletResponse response) throws IOException {
        log.info("Exporting {} templates to Excel", templateIds.size());
        
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"templates_export.xlsx\"");
        
        importExportService.exportToExcel(templateIds, response.getOutputStream());
    }

    @GetMapping("/export/excel/domain/{domain}")
    @Operation(summary = "按领域导出Excel", description = "将指定领域的模板导出为Excel")
    public void exportToExcelByDomain(
            @Parameter(description = "业务领域") @PathVariable String domain,
            HttpServletResponse response) throws IOException {
        log.info("Exporting templates for domain: {} to Excel", domain);
        
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"templates_" + domain + "_export.xlsx\"");
        
        importExportService.exportToExcel(domain, response.getOutputStream());
    }

    @GetMapping("/export/{id}/json")
    @Operation(summary = "导出单个模板JSON", description = "将单个模板导出为JSON文件")
    public ResponseEntity<byte[]> exportSingleTemplateJson(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Exporting template {} to JSON", id);
        
        byte[] content = importExportService.exportTemplateAsJson(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"template_" + id + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
    }

    @GetMapping("/export/{id}/excel")
    @Operation(summary = "导出单个模板Excel", description = "将单个模板导出为Excel文件")
    public ResponseEntity<byte[]> exportSingleTemplateExcel(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Exporting template {} to Excel", id);
        
        byte[] content = importExportService.exportTemplateAsExcel(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"template_" + id + ".xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/template/{fileType}")
    @Operation(summary = "获取导入模板", description = "生成导入模板示例文件")
    public ResponseEntity<String> generateImportTemplate(
            @Parameter(description = "文件类型") @PathVariable String fileType) {
        log.info("Generating import template for type: {}", fileType);
        String template = importExportService.generateImportTemplate(fileType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"import_template." + fileType + "\"")
                .body(template);
    }
}
