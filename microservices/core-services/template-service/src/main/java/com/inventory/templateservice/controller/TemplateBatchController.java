package com.inventory.templateservice.controller;

import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.templateservice.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模板批量操作控制器 - 管理模板批量操作
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/templates/batch")
@RequiredArgsConstructor
@Tag(name = "批量操作", description = "模板批量操作接口")
@Validated
public class TemplateBatchController {

    private final TemplateService templateService;

    @PostMapping("/create")
    @Operation(summary = "批量创建模板", description = "一次性创建多个模板")
    public ResponseEntity<Map<String, Object>> batchCreate(
            @RequestBody List<TemplateDTO> templates,
            @Parameter(description = "是否忽略错误继续执行") @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (TemplateDTO template : templates) {
            try {
                templateService.createTemplate(template);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add(template.getTemplateCode() + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", templates.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @PutMapping("/update")
    @Operation(summary = "批量更新模板", description = "一次性更新多个模板")
    public ResponseEntity<Map<String, Object>> batchUpdate(
            @RequestBody List<TemplateDTO> templates,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (TemplateDTO template : templates) {
            try {
                if (template.getId() != null) {
                    templateService.updateTemplate(template.getId(), template);
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                errors.add(template.getTemplateCode() + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", templates.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "批量删除模板", description = "一次性删除多个模板")
    public ResponseEntity<Map<String, Object>> batchDelete(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Long id : ids) {
            try {
                templateService.deleteTemplate(id);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("ID " + id + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @PostMapping("/publish")
    @Operation(summary = "批量发布模板", description = "一次性发布多个模板")
    public ResponseEntity<Map<String, Object>> batchPublish(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Long id : ids) {
            try {
                templateService.publishTemplate(id);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("ID " + id + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @PostMapping("/deprecate")
    @Operation(summary = "批量废弃模板", description = "一次性废弃多个模板")
    public ResponseEntity<Map<String, Object>> batchDeprecate(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Long id : ids) {
            try {
                templateService.deprecateTemplate(id);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("ID " + id + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @PostMapping("/archive")
    @Operation(summary = "批量归档模板", description = "一次性归档多个模板")
    public ResponseEntity<Map<String, Object>> batchArchive(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Long id : ids) {
            try {
                templateService.archiveTemplate(id);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("ID " + id + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }

    @PostMapping("/copy")
    @Operation(summary = "批量复制模板", description = "复制多个模板到新模板")
    public ResponseEntity<Map<String, Object>> batchCopy(
            @RequestBody Map<Long, String> sourceToTarget,
            @RequestParam(defaultValue = "false") boolean continueOnError) {
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Map.Entry<Long, String> entry : sourceToTarget.entrySet()) {
            try {
                templateService.copyTemplate(entry.getKey(), entry.getValue());
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("ID " + entry.getKey() + ": " + e.getMessage());
                if (!continueOnError) {
                    break;
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "total", sourceToTarget.size(),
                "success", successCount,
                "failed", failCount,
                "errors", errors
        ));
    }
}
