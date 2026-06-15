package com.inventory.templateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.FieldType;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.constant.TemplateConstants;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateFieldDTO;
import com.inventory.common.template.dto.TemplateImportErrorDTO;
import com.inventory.common.template.dto.TemplateImportResultDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.common.template.service.ITemplateImportExportService;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.repository.ITemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateImportExportService implements ITemplateImportExportService {

    private final ITemplateRepository templateRepository;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TemplateImportResultDTO importFromJson(InputStream jsonStream) {
        return importFromJson(jsonStream, false);
    }

    @Override
    @Transactional
    public TemplateImportResultDTO importFromJson(InputStream jsonStream, boolean incremental) {
        log.info("Importing templates from JSON, incremental: {}", incremental);

        List<TemplateImportErrorDTO> errors = new ArrayList<>();
        List<String> importedCodes = new ArrayList<>();

        try {
            List<TemplateDTO> templates = objectMapper.readValue(jsonStream,
                    new TypeReference<List<TemplateDTO>>() {});

            int total = templates.size();
            int success = 0;
            int failed = 0;
            int skipped = 0;

            for (int i = 0; i < templates.size(); i++) {
                TemplateDTO dto = templates.get(i);
                try {
                    validateTemplateDTO(dto);

                    if (incremental && templateRepository.existsByTemplateCode(dto.getTemplateCode())) {
                        Template existing = templateRepository.findByTemplateCode(dto.getTemplateCode())
                                .orElseThrow(() -> TemplateException.notFoundByCode(dto.getTemplateCode()));
                        templateService.updateTemplate(existing.getId(), dto);
                    } else {
                        templateService.createTemplate(dto);
                    }

                    importedCodes.add(dto.getTemplateCode());
                    success++;
                } catch (TemplateException e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .templateCode(dto.getTemplateCode())
                            .errorType("TEMPLATE_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("Please check template configuration")
                            .build());
                    failed++;
                } catch (IllegalArgumentException e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .templateCode(dto.getTemplateCode())
                            .errorType("VALIDATION_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("Please check the template data format")
                            .build());
                    failed++;
                } catch (Exception e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .templateCode(dto.getTemplateCode())
                            .errorType("IMPORT_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("An unexpected error occurred, please check the data format")
                            .build());
                    failed++;
                }
            }

            return TemplateImportResultDTO.builder()
                    .success(failed == 0)
                    .totalCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .skippedCount(skipped)
                    .errors(errors)
                    .importedTemplateCodes(importedCodes)
                    .summary(Map.of("importType", "JSON", "incremental", incremental))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON input", e);
            throw TemplateException.importFailed("Invalid JSON format: " + e.getMessage());
        } catch (IOException e) {
            log.error("Failed to read input stream", e);
            throw TemplateException.importFailed("Failed to read input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to import JSON", e);
            throw TemplateException.importFailed(e.getMessage());
        }
    }

    @Override
    @Transactional
    public TemplateImportResultDTO importFromExcel(InputStream excelStream) {
        return importFromExcel(excelStream, false);
    }

    @Override
    @Transactional
    public TemplateImportResultDTO importFromExcel(InputStream excelStream, boolean incremental) {
        log.info("Importing templates from Excel, incremental: {}", incremental);

        List<TemplateImportErrorDTO> errors = new ArrayList<>();
        List<String> importedCodes = new ArrayList<>();
        int total = 0;
        int success = 0;
        int failed = 0;
        int skipped = 0;

        try (Workbook workbook = new XSSFWorkbook(excelStream)) {
            Sheet templateSheet = workbook.getSheet("Templates");
            if (templateSheet == null) {
                throw TemplateException.importFailed("Sheet 'Templates' not found");
            }

            Map<String, Integer> headerIndex = new LinkedHashMap<>();
            Row headerRow = templateSheet.getRow(0);
            for (Cell cell : headerRow) {
                headerIndex.put(cell.getStringCellValue(), cell.getColumnIndex());
            }

            for (int i = 1; i <= templateSheet.getLastRowNum(); i++) {
                Row row = templateSheet.getRow(i);
                if (row == null) continue;

                total++;
                TemplateDTO dto = null;
                String templateCode = "ROW_" + (i + 1);
                try {
                    dto = parseTemplateFromRow(row, headerIndex);
                    final String resolvedTemplateCode = dto.getTemplateCode();
                    templateCode = resolvedTemplateCode;
                    validateTemplateDTO(dto);

                    if (incremental && templateRepository.existsByTemplateCode(resolvedTemplateCode)) {
                        Template existing = templateRepository.findByTemplateCode(resolvedTemplateCode)
                                .orElseThrow(() -> TemplateException.notFoundByCode(resolvedTemplateCode));
                        templateService.updateTemplate(existing.getId(), dto);
                    } else {
                        templateService.createTemplate(dto);
                    }

                    importedCodes.add(resolvedTemplateCode);
                    success++;
                } catch (TemplateException e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .templateCode(templateCode)
                            .errorType("TEMPLATE_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("Please check template configuration")
                            .build());
                    failed++;
                } catch (IllegalArgumentException e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .templateCode(templateCode)
                            .errorType("VALIDATION_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("Please check the row data")
                            .build());
                    failed++;
                } catch (Exception e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .rowIndex(i + 1)
                            .errorType("IMPORT_ERROR")
                            .errorMessage(e.getMessage())
                            .suggestion("An unexpected error occurred during row processing")
                            .build());
                    failed++;
                }
            }

            return TemplateImportResultDTO.builder()
                    .success(failed == 0)
                    .totalCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .skippedCount(skipped)
                    .errors(errors)
                    .importedTemplateCodes(importedCodes)
                    .summary(Map.of("importType", "Excel", "incremental", incremental))
                    .build();

        } catch (TemplateException e) {
            log.error("Template validation failed during Excel import", e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            throw TemplateException.importFailed("Failed to read Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to import Excel", e);
            throw TemplateException.importFailed(e.getMessage());
        }
    }

    @Override
    public void exportToJson(List<Long> templateIds, OutputStream outputStream) {
        log.info("Exporting {} templates to JSON", templateIds.size());

        try {
            List<TemplateDTO> templates = templateIds.stream()
                    .map(id -> templateService.getTemplateById(id))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, templates);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize templates to JSON", e);
            throw TemplateException.exportFailed("Failed to serialize data: " + e.getMessage());
        } catch (IOException e) {
            log.error("Failed to write JSON to output stream", e);
            throw TemplateException.exportFailed("Failed to write output: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to export JSON", e);
            throw TemplateException.exportFailed(e.getMessage());
        }
    }

    @Override
    public void exportToJson(String domain, OutputStream outputStream) {
        log.info("Exporting templates for domain: {}", domain);

        List<TemplateDTO> templates = templateService.getTemplatesByDomain(domain);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, templates);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize templates to JSON", e);
            throw TemplateException.exportFailed("Failed to serialize data: " + e.getMessage());
        } catch (IOException e) {
            log.error("Failed to write JSON to output stream", e);
            throw TemplateException.exportFailed("Failed to write output: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to export JSON", e);
            throw TemplateException.exportFailed(e.getMessage());
        }
    }

    @Override
    public void exportToExcel(List<Long> templateIds, OutputStream outputStream) {
        log.info("Exporting {} templates to Excel", templateIds.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet templateSheet = workbook.createSheet("Templates");
            Sheet fieldSheet = workbook.createSheet("Fields");

            createTemplateHeaderRow(templateSheet);
            createFieldHeaderRow(fieldSheet);

            int templateRowNum = 1;
            int fieldRowNum = 1;

            for (Long templateId : templateIds) {
                TemplateDTO dto = templateService.getTemplateById(templateId).orElse(null);
                if (dto == null) continue;

                Row row = templateSheet.createRow(templateRowNum++);
                populateTemplateRow(row, dto);

                if (dto.getFields() != null) {
                    for (TemplateFieldDTO field : dto.getFields()) {
                        Row fieldRow = fieldSheet.createRow(fieldRowNum++);
                        populateFieldRow(fieldRow, dto.getTemplateCode(), field);
                    }
                }
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Failed to write Excel to output stream", e);
            throw TemplateException.exportFailed("Failed to write Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to export Excel", e);
            throw TemplateException.exportFailed(e.getMessage());
        }
    }

    @Override
    public void exportToExcel(String domain, OutputStream outputStream) {
        List<TemplateDTO> templates = templateService.getTemplatesByDomain(domain);
        List<Long> templateIds = templates.stream()
                .map(TemplateDTO::getId)
                .collect(Collectors.toList());
        exportToExcel(templateIds, outputStream);
    }

    @Override
    public byte[] exportTemplateAsJson(Long templateId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportToJson(List.of(templateId), outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportTemplateAsExcel(Long templateId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportToExcel(List.of(templateId), outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public TemplateImportResultDTO validateImportFile(InputStream inputStream, String fileType) {
        List<TemplateImportErrorDTO> errors = new ArrayList<>();

        try {
            if (TemplateConstants.IMPORT_FORMAT_JSON.equalsIgnoreCase(fileType)) {
                List<TemplateDTO> templates = objectMapper.readValue(inputStream,
                        new TypeReference<List<TemplateDTO>>() {});

                for (int i = 0; i < templates.size(); i++) {
                    try {
                        validateTemplateDTO(templates.get(i));
                    } catch (IllegalArgumentException e) {
                        errors.add(TemplateImportErrorDTO.builder()
                                .rowIndex(i + 1)
                                .templateCode(templates.get(i).getTemplateCode())
                                .errorType("VALIDATION_ERROR")
                                .errorMessage(e.getMessage())
                                .build());
                    } catch (Exception e) {
                        errors.add(TemplateImportErrorDTO.builder()
                                .rowIndex(i + 1)
                                .templateCode(templates.get(i).getTemplateCode())
                                .errorType("VALIDATION_ERROR")
                                .errorMessage("Unexpected validation error: " + e.getMessage())
                                .build());
                    }
                }
            } else if (TemplateConstants.IMPORT_FORMAT_EXCEL.equalsIgnoreCase(fileType)) {
                try (Workbook workbook = new XSSFWorkbook(inputStream)) {
                    Sheet sheet = workbook.getSheet("Templates");
                    if (sheet == null) {
                        errors.add(TemplateImportErrorDTO.builder()
                                .errorType("FILE_ERROR")
                                .errorMessage("Sheet 'Templates' not found")
                                .build());
                    }
                } catch (IOException e) {
                    errors.add(TemplateImportErrorDTO.builder()
                            .errorType("FILE_ERROR")
                            .errorMessage("Failed to read Excel file: " + e.getMessage())
                            .build());
                }
            } else {
                errors.add(TemplateImportErrorDTO.builder()
                        .errorType("FILE_TYPE_ERROR")
                        .errorMessage("Unsupported file type: " + fileType)
                        .build());
            }
        } catch (JsonProcessingException e) {
            errors.add(TemplateImportErrorDTO.builder()
                    .errorType("FILE_ERROR")
                    .errorMessage("Invalid JSON format: " + e.getMessage())
                    .build());
        } catch (IOException e) {
            errors.add(TemplateImportErrorDTO.builder()
                    .errorType("FILE_ERROR")
                    .errorMessage("Failed to read input stream: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            errors.add(TemplateImportErrorDTO.builder()
                    .errorType("FILE_ERROR")
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build());
        }

        return TemplateImportResultDTO.builder()
                .success(errors.isEmpty())
                .errors(errors)
                .build();
    }

    @Override
    public List<TemplateDTO> parseImportFile(InputStream inputStream, String fileType) {
        try {
            if (TemplateConstants.IMPORT_FORMAT_JSON.equalsIgnoreCase(fileType)) {
                return objectMapper.readValue(inputStream, new TypeReference<List<TemplateDTO>>() {});
            } else {
                throw TemplateException.importFailed("Unsupported file type: " + fileType);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON import file", e);
            throw TemplateException.importFailed("Invalid JSON format: " + e.getMessage());
        } catch (IOException e) {
            log.error("Failed to read input stream", e);
            throw TemplateException.importFailed("Failed to read input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse import file", e);
            throw TemplateException.importFailed(e.getMessage());
        }
    }

    @Override
    public String generateImportTemplate(String fileType) {
        if (TemplateConstants.IMPORT_FORMAT_JSON.equalsIgnoreCase(fileType)) {
            TemplateDTO sample = TemplateDTO.builder()
                    .templateCode("TPL_SAMPLE_001")
                    .templateName("Sample Template")
                    .description("This is a sample template for import")
                    .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                    .category("basic")
                    .status(TemplateStatus.DRAFT)
                    .allowCustomFields(true)
                    .fields(List.of(
                            TemplateFieldDTO.builder()
                                    .fieldCode("FIELD_001")
                                    .fieldName("sampleField")
                                    .fieldLabel("Sample Field")
                                    .fieldType(FieldType.TEXT)
                                    .required(true)
                                    .displayOrder(1)
                                    .build()
                    ))
                    .build();

            try {
                return objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(List.of(sample));
            } catch (Exception e) {
                throw TemplateException.exportFailed(e.getMessage());
            }
        }

        return "";
    }

    private void validateTemplateDTO(TemplateDTO dto) {
        if (dto.getTemplateCode() == null || dto.getTemplateCode().isEmpty()) {
            throw new IllegalArgumentException("Template code is required");
        }
        if (dto.getTemplateName() == null || dto.getTemplateName().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (dto.getBusinessDomain() == null) {
            throw new IllegalArgumentException("Business domain is required");
        }
    }

    private TemplateDTO parseTemplateFromRow(Row row, Map<String, Integer> headerIndex) {
        return TemplateDTO.builder()
                .templateCode(getCellValue(row, headerIndex, "templateCode"))
                .templateName(getCellValue(row, headerIndex, "templateName"))
                .description(getCellValue(row, headerIndex, "description"))
                .businessDomain(BusinessDomain.valueOf(getCellValue(row, headerIndex, "businessDomain")))
                .category(getCellValue(row, headerIndex, "category"))
                .allowCustomFields(Boolean.parseBoolean(getCellValue(row, headerIndex, "allowCustomFields")))
                .build();
    }

    private String getCellValue(Row row, Map<String, Integer> headerIndex, String columnName) {
        Integer index = headerIndex.get(columnName);
        if (index == null) return "";

        Cell cell = row.getCell(index);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private void createTemplateHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);
        String[] headers = {"templateCode", "templateName", "description", "businessDomain", 
                           "category", "status", "version", "allowCustomFields"};
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void createFieldHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);
        String[] headers = {"templateCode", "fieldCode", "fieldName", "fieldLabel", "fieldType",
                           "required", "displayOrder", "validationRegex"};
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void populateTemplateRow(Row row, TemplateDTO dto) {
        row.createCell(0).setCellValue(dto.getTemplateCode());
        row.createCell(1).setCellValue(dto.getTemplateName());
        row.createCell(2).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
        row.createCell(3).setCellValue(dto.getBusinessDomain().name());
        row.createCell(4).setCellValue(dto.getCategory() != null ? dto.getCategory() : "");
        row.createCell(5).setCellValue(dto.getStatus() != null ? dto.getStatus().name() : "");
        row.createCell(6).setCellValue(dto.getVersion() != null ? dto.getVersion() : "");
        row.createCell(7).setCellValue(dto.getAllowCustomFields() != null ? dto.getAllowCustomFields().toString() : "false");
    }

    private void populateFieldRow(Row row, String templateCode, TemplateFieldDTO field) {
        row.createCell(0).setCellValue(templateCode);
        row.createCell(1).setCellValue(field.getFieldCode());
        row.createCell(2).setCellValue(field.getFieldName());
        row.createCell(3).setCellValue(field.getFieldLabel());
        row.createCell(4).setCellValue(field.getFieldType().name());
        row.createCell(5).setCellValue(field.getRequired() != null ? field.getRequired().toString() : "false");
        row.createCell(6).setCellValue(field.getDisplayOrder() != null ? field.getDisplayOrder() : 0);
        row.createCell(7).setCellValue(field.getValidationRegex() != null ? field.getValidationRegex() : "");
    }
}
