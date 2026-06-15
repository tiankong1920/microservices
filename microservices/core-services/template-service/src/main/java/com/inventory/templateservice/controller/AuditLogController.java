package com.inventory.templateservice.controller;

import com.inventory.templateservice.entity.TemplateAuditLog;
import com.inventory.templateservice.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器 - 管理模板操作审计日志
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "审计日志", description = "模板操作审计日志接口")
@Validated
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/template/{templateId}")
    @Operation(summary = "获取模板审计日志", description = "获取指定模板的所有审计日志")
    public ResponseEntity<List<TemplateAuditLog>> getAuditLogsByTemplate(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        List<TemplateAuditLog> logs = auditLogService.getAuditLogsByTemplate(templateId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/template/{templateId}/paged")
    @Operation(summary = "分页获取模板审计日志", description = "分页获取指定模板的审计日志")
    public ResponseEntity<Page<TemplateAuditLog>> getAuditLogsByTemplatePaged(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Page<TemplateAuditLog> logs = auditLogService.getAuditLogsByTemplate(templateId, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "获取租户审计日志", description = "分页获取指定租户的审计日志")
    public ResponseEntity<Page<TemplateAuditLog>> getAuditLogsByTenant(
            @Parameter(description = "租户ID") @PathVariable String tenantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Page<TemplateAuditLog> logs = auditLogService.getAuditLogsByTenant(tenantId, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/template/{templateId}/time-range")
    @Operation(summary = "按时间范围获取审计日志", description = "获取指定时间范围内的审计日志")
    public ResponseEntity<List<TemplateAuditLog>> getAuditLogsByTimeRange(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<TemplateAuditLog> logs = auditLogService.getAuditLogsByTimeRange(templateId, startTime, endTime);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索审计日志", description = "根据条件搜索审计日志")
    public ResponseEntity<Page<TemplateAuditLog>> searchAuditLogs(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operation,
            @Parameter(description = "操作人") @RequestParam(required = false) String operator,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Page<TemplateAuditLog> logs = auditLogService.searchAuditLogs(tenantId, operation, operator, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/template/{templateId}/statistics")
    @Operation(summary = "获取审计统计", description = "获取模板的审计统计数据")
    public ResponseEntity<Map<String, Object>> getAuditStatistics(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        Map<String, Object> stats = auditLogService.getAuditStatistics(templateId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/operator/{operator}/recent")
    @Operation(summary = "获取最近操作", description = "获取指定操作人的最近操作记录")
    public ResponseEntity<List<TemplateAuditLog>> getRecentOperations(
            @Parameter(description = "操作人") @PathVariable String operator,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        List<TemplateAuditLog> logs = auditLogService.getRecentOperations(operator, limit);
        return ResponseEntity.ok(logs);
    }
}
