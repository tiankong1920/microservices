package com.inventory.common.template.service;

import com.inventory.common.template.dto.TemplateImportResultDTO;
import com.inventory.common.template.dto.TemplateDTO;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ITemplateImportExportService {

    TemplateImportResultDTO importFromJson(InputStream jsonStream);

    TemplateImportResultDTO importFromJson(InputStream jsonStream, boolean incremental);

    TemplateImportResultDTO importFromExcel(InputStream excelStream);

    TemplateImportResultDTO importFromExcel(InputStream excelStream, boolean incremental);

    void exportToJson(List<Long> templateIds, OutputStream outputStream);

    void exportToJson(String domain, OutputStream outputStream);

    void exportToExcel(List<Long> templateIds, OutputStream outputStream);

    void exportToExcel(String domain, OutputStream outputStream);

    byte[] exportTemplateAsJson(Long templateId);

    byte[] exportTemplateAsExcel(Long templateId);

    TemplateImportResultDTO validateImportFile(InputStream inputStream, String fileType);

    List<TemplateDTO> parseImportFile(InputStream inputStream, String fileType);

    String generateImportTemplate(String fileType);
}
