package com.inventory.templateservice.controller;

import com.inventory.common.template.FieldType;
import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.ValidationRuleDTO;
import com.inventory.common.template.service.ICustomFieldService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 自定义字段控制器 - 管理自定义字段和校验规则
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "自定义字段管理", description = "自定义字段和校验规则管理接口")
@Validated
public class CustomFieldController {

    private final ICustomFieldService customFieldService;

    @PostMapping("/templates/{templateId}/custom-fields")
    @Operation(summary = "创建自定义字段", description = "为指定模板创建自定义字段")
    public ResponseEntity<CustomFieldDTO> createCustomField(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Valid @RequestBody CustomFieldDTO customFieldDTO) {
        log.info("Creating custom field for template: {}", templateId);
        CustomFieldDTO created = customFieldService.createCustomField(templateId, customFieldDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/custom-fields/{id}")
    @Operation(summary = "更新自定义字段", description = "更新自定义字段配置")
    public ResponseEntity<CustomFieldDTO> updateCustomField(
            @Parameter(description = "字段ID") @PathVariable Long id,
            @Valid @RequestBody CustomFieldDTO customFieldDTO) {
        log.info("Updating custom field: {}", id);
        CustomFieldDTO updated = customFieldService.updateCustomField(id, customFieldDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/custom-fields/{id}")
    @Operation(summary = "删除自定义字段", description = "软删除自定义字段")
    public ResponseEntity<Void> deleteCustomField(
            @Parameter(description = "字段ID") @PathVariable Long id) {
        log.info("Deleting custom field: {}", id);
        customFieldService.deleteCustomField(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custom-fields/{id}/activate")
    @Operation(summary = "激活自定义字段", description = "激活已停用的自定义字段")
    public ResponseEntity<Void> activateCustomField(
            @Parameter(description = "字段ID") @PathVariable Long id) {
        log.info("Activating custom field: {}", id);
        customFieldService.activateCustomField(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/custom-fields/{id}/deactivate")
    @Operation(summary = "停用自定义字段", description = "停用自定义字段")
    public ResponseEntity<Void> deactivateCustomField(
            @Parameter(description = "字段ID") @PathVariable Long id) {
        log.info("Deactivating custom field: {}", id);
        customFieldService.deactivateCustomField(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/custom-fields/{id}")
    @Operation(summary = "获取自定义字段详情", description = "根据ID获取自定义字段详细信息")
    public ResponseEntity<CustomFieldDTO> getCustomField(
            @Parameter(description = "字段ID") @PathVariable Long id) {
        return customFieldService.getCustomFieldById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/templates/{templateId}/custom-fields")
    @Operation(summary = "获取模板的自定义字段", description = "获取指定模板的所有自定义字段")
    public ResponseEntity<List<CustomFieldDTO>> getCustomFieldsByTemplate(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        List<CustomFieldDTO> fields = customFieldService.getCustomFieldsByTemplate(templateId);
        return ResponseEntity.ok(fields);
    }

    @GetMapping("/templates/{templateId}/custom-fields/active")
    @Operation(summary = "获取模板的激活自定义字段", description = "获取指定模板的所有激活状态自定义字段")
    public ResponseEntity<List<CustomFieldDTO>> getActiveCustomFields(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        List<CustomFieldDTO> fields = customFieldService.getActiveCustomFields(templateId);
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/custom-fields/{id}/validate")
    @Operation(summary = "验证字段值", description = "验证值是否符合自定义字段的校验规则")
    public ResponseEntity<Map<String, Object>> validateFieldValue(
            @Parameter(description = "字段ID") @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Object value = request.get("value");
        Map<String, String> errors = customFieldService.validateFieldValue(id, value);
        
        return ResponseEntity.ok(Map.of(
                "valid", errors.isEmpty(),
                "errors", errors
        ));
    }

    @PostMapping("/validation-rules")
    @Operation(summary = "创建校验规则", description = "创建新的校验规则")
    public ResponseEntity<ValidationRuleDTO> createValidationRule(
            @Valid @RequestBody ValidationRuleDTO ruleDTO) {
        log.info("Creating validation rule: {}", ruleDTO.getRuleCode());
        ValidationRuleDTO created = customFieldService.createValidationRule(ruleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/validation-rules/{id}")
    @Operation(summary = "更新校验规则", description = "更新校验规则配置")
    public ResponseEntity<ValidationRuleDTO> updateValidationRule(
            @Parameter(description = "规则ID") @PathVariable Long id,
            @Valid @RequestBody ValidationRuleDTO ruleDTO) {
        log.info("Updating validation rule: {}", id);
        ValidationRuleDTO updated = customFieldService.updateValidationRule(id, ruleDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/validation-rules/{id}")
    @Operation(summary = "删除校验规则", description = "软删除校验规则")
    public ResponseEntity<Void> deleteValidationRule(
            @Parameter(description = "规则ID") @PathVariable Long id) {
        log.info("Deleting validation rule: {}", id);
        customFieldService.deleteValidationRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validation-rules/{id}")
    @Operation(summary = "获取校验规则详情", description = "根据ID获取校验规则详细信息")
    public ResponseEntity<ValidationRuleDTO> getValidationRule(
            @Parameter(description = "规则ID") @PathVariable Long id) {
        return customFieldService.getValidationRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validation-rules/code/{code}")
    @Operation(summary = "根据编码获取校验规则", description = "根据规则编码获取校验规则")
    public ResponseEntity<ValidationRuleDTO> getValidationRuleByCode(
            @Parameter(description = "规则编码") @PathVariable String code) {
        return customFieldService.getValidationRuleByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validation-rules")
    @Operation(summary = "获取所有校验规则", description = "获取所有激活状态的校验规则")
    public ResponseEntity<List<ValidationRuleDTO>> getAllValidationRules() {
        List<ValidationRuleDTO> rules = customFieldService.getAllValidationRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/validation-rules/builtin")
    @Operation(summary = "获取内置校验规则", description = "获取所有内置校验规则")
    public ResponseEntity<List<ValidationRuleDTO>> getBuiltInValidationRules() {
        List<ValidationRuleDTO> rules = customFieldService.getBuiltInValidationRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/validation-rules/field-type/{fieldType}")
    @Operation(summary = "按字段类型获取校验规则", description = "获取适用于指定字段类型的校验规则")
    public ResponseEntity<List<ValidationRuleDTO>> getValidationRulesByFieldType(
            @Parameter(description = "字段类型") @PathVariable FieldType fieldType) {
        List<ValidationRuleDTO> rules = customFieldService.getValidationRulesByFieldType(fieldType);
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/validation-rules/validate-regex")
    @Operation(summary = "验证正则表达式", description = "验证正则表达式是否有效")
    public ResponseEntity<Map<String, Object>> validateRegex(
            @RequestBody Map<String, String> request) {
        String pattern = request.get("pattern");
        boolean valid = customFieldService.isValidRegex(pattern);
        
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "pattern", pattern
        ));
    }
}
