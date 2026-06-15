package com.inventory.templateservice.controller;

import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateFieldDTO;
import com.inventory.common.template.dto.TemplateVersionDTO;
import com.inventory.common.template.service.ITemplateService;
import com.inventory.common.template.service.ITemplateVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模板控制器 - 管理业务模板的增删改查
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "模板管理", description = "模板的增删改查接口")
@Validated
public class TemplateController {

    private final ITemplateService templateService;
    private final ITemplateVersionService versionService;

    @PostMapping
    @Operation(summary = "创建模板", description = "创建新的业务模板")
    public ResponseEntity<TemplateDTO> createTemplate(@Valid @RequestBody TemplateDTO templateDTO) {
        log.info("Creating template: {}", templateDTO.getTemplateCode());
        TemplateDTO created = templateService.createTemplate(templateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板", description = "更新指定模板的配置")
    public ResponseEntity<TemplateDTO> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Valid @RequestBody TemplateDTO templateDTO) {
        log.info("Updating template: {}", id);
        TemplateDTO updated = templateService.updateTemplate(id, templateDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情", description = "根据ID获取模板详细信息")
    public ResponseEntity<TemplateDTO> getTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取模板", description = "根据模板编码获取模板")
    public ResponseEntity<TemplateDTO> getTemplateByCode(
            @Parameter(description = "模板编码") @PathVariable String code) {
        return templateService.getTemplateByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "搜索模板", description = "根据条件搜索模板列表")
    public ResponseEntity<List<TemplateDTO>> searchTemplates(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "业务领域") @RequestParam(required = false) String domain,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        List<TemplateDTO> templates = templateService.searchTemplates(keyword, domain, status, page, size);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/domain/{domain}")
    @Operation(summary = "按领域获取模板", description = "获取指定业务领域的所有模板")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByDomain(
            @Parameter(description = "业务领域") @PathVariable String domain) {
        List<TemplateDTO> templates = templateService.getTemplatesByDomain(domain);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态获取模板", description = "获取指定状态的所有模板")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByStatus(
            @Parameter(description = "状态") @PathVariable String status) {
        List<TemplateDTO> templates = templateService.getTemplatesByStatus(status);
        return ResponseEntity.ok(templates);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板", description = "删除或归档指定模板")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Deleting template: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布模板", description = "将审核通过的模板发布")
    public ResponseEntity<TemplateDTO> publishTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Publishing template: {}", id);
        TemplateDTO published = templateService.publishTemplate(id);
        return ResponseEntity.ok(published);
    }

    @PostMapping("/{id}/deprecate")
    @Operation(summary = "废弃模板", description = "将模板标记为废弃状态")
    public ResponseEntity<TemplateDTO> deprecateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Deprecating template: {}", id);
        TemplateDTO deprecated = templateService.deprecateTemplate(id);
        return ResponseEntity.ok(deprecated);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "归档模板", description = "将模板归档")
    public ResponseEntity<TemplateDTO> archiveTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("Archiving template: {}", id);
        TemplateDTO archived = templateService.archiveTemplate(id);
        return ResponseEntity.ok(archived);
    }

    @PostMapping("/{templateId}/fields")
    @Operation(summary = "添加字段", description = "向模板添加新字段")
    public ResponseEntity<TemplateDTO> addField(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Valid @RequestBody TemplateFieldDTO fieldDTO) {
        log.info("Adding field to template: {}", templateId);
        TemplateDTO updated = templateService.addField(templateId, fieldDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{templateId}/fields/{fieldId}")
    @Operation(summary = "更新字段", description = "更新模板中的字段配置")
    public ResponseEntity<TemplateDTO> updateField(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "字段ID") @PathVariable Long fieldId,
            @Valid @RequestBody TemplateFieldDTO fieldDTO) {
        log.info("Updating field {} in template {}", fieldId, templateId);
        TemplateDTO updated = templateService.updateField(templateId, fieldId, fieldDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{templateId}/fields/{fieldId}")
    @Operation(summary = "删除字段", description = "从模板中删除字段")
    public ResponseEntity<TemplateDTO> removeField(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "字段ID") @PathVariable Long fieldId) {
        log.info("Removing field {} from template {}", fieldId, templateId);
        TemplateDTO updated = templateService.removeField(templateId, fieldId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{templateId}/validate")
    @Operation(summary = "验证数据", description = "验证数据是否符合模板规则")
    public ResponseEntity<Map<String, Object>> validateData(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @RequestBody Map<String, Object> data) {
        TemplateDTO template = templateService.getTemplateById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        
        Map<String, String> errors = templateService.validateFieldValues(template.getTemplateCode(), data);
        
        return ResponseEntity.ok(Map.of(
                "valid", errors.isEmpty(),
                "errors", errors
        ));
    }

    @GetMapping("/{templateCode}/required-fields")
    @Operation(summary = "获取必填字段", description = "获取模板的所有必填字段")
    public ResponseEntity<List<TemplateFieldDTO>> getRequiredFields(
            @Parameter(description = "模板编码") @PathVariable String templateCode) {
        List<TemplateFieldDTO> fields = templateService.getRequiredFields(templateCode);
        return ResponseEntity.ok(fields);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "获取版本历史", description = "获取模板的版本变更历史")
    public ResponseEntity<List<TemplateVersionDTO>> getVersionHistory(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        List<TemplateVersionDTO> versions = versionService.getVersionHistory(id, page, size);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/versions/{versionId}")
    @Operation(summary = "获取版本详情", description = "获取指定版本的详细信息")
    public ResponseEntity<TemplateVersionDTO> getVersion(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        return versionService.getVersionById(versionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/rollback/{versionId}")
    @Operation(summary = "回滚版本", description = "将模板回滚到指定版本")
    public ResponseEntity<TemplateDTO> rollbackToVersion(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "版本ID") @PathVariable Long versionId,
            @Parameter(description = "回滚原因") @RequestParam String reason) {
        log.info("Rolling back template {} to version {}", id, versionId);
        TemplateDTO rolledBack = versionService.rollbackToVersion(id, versionId, reason);
        return ResponseEntity.ok(rolledBack);
    }

    @GetMapping("/{id}/versions/compare")
    @Operation(summary = "对比版本", description = "对比两个版本的差异")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "版本1 ID") @RequestParam Long versionId1,
            @Parameter(description = "版本2 ID") @RequestParam Long versionId2) {
        Map<String, Object> comparison = versionService.compareVersions(id, versionId1, versionId2);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/{id}/versions/statistics")
    @Operation(summary = "版本统计", description = "获取模板版本统计数据")
    public ResponseEntity<Map<String, Object>> getVersionStatistics(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        Map<String, Object> stats = versionService.getVersionStatistics(id);
        return ResponseEntity.ok(stats);
    }
}
