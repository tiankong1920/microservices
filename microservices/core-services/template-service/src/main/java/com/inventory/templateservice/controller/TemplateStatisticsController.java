package com.inventory.templateservice.controller;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.templateservice.repository.ITemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板统计控制器 - 管理模板统计分析
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/templates/statistics")
@RequiredArgsConstructor
@Tag(name = "模板统计", description = "模板统计分析接口")
@Validated
public class TemplateStatisticsController {

    private final ITemplateRepository templateRepository;

    @GetMapping("/overview")
    @Operation(summary = "获取模板概览统计", description = "获取模板的整体统计数据")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        
        long total = templateRepository.count();
        overview.put("totalTemplates", total);
        
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TemplateStatus status : TemplateStatus.values()) {
            byStatus.put(status.name(), templateRepository.countByStatus(status));
        }
        overview.put("byStatus", byStatus);
        
        Map<String, Long> byDomain = new LinkedHashMap<>();
        for (BusinessDomain domain : BusinessDomain.values()) {
            byDomain.put(domain.name(), templateRepository.countByBusinessDomain(domain));
        }
        overview.put("byDomain", byDomain);
        
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/by-status")
    @Operation(summary = "按状态统计", description = "按状态统计模板数量")
    public ResponseEntity<Map<String, Long>> getByStatus() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        for (TemplateStatus status : TemplateStatus.values()) {
            stats.put(status.name(), templateRepository.countByStatus(status));
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/by-domain")
    @Operation(summary = "按领域统计", description = "按业务领域统计模板数量")
    public ResponseEntity<Map<String, Long>> getByDomain() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        for (BusinessDomain domain : BusinessDomain.values()) {
            stats.put(domain.name(), templateRepository.countByBusinessDomain(domain));
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/by-tenant/{tenantId}")
    @Operation(summary = "按租户统计", description = "按租户统计模板数量")
    public ResponseEntity<Map<String, Object>> getByTenant(
            @PathVariable String tenantId) {
        
        Map<String, Object> stats = new LinkedHashMap<>();
        
        long total = templateRepository.countByTenantId(tenantId);
        stats.put("tenantId", tenantId);
        stats.put("totalTemplates", total);
        
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TemplateStatus status : TemplateStatus.values()) {
            byStatus.put(status.name(), templateRepository.countByTenantIdAndStatus(tenantId, status));
        }
        stats.put("byStatus", byStatus);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势统计", description = "获取模板创建趋势")
    public ResponseEntity<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("period", days + " days");
        trend.put("data", Collections.emptyList());
        trend.put("message", "Trend analysis requires custom query implementation");
        
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘数据", description = "获取仪表盘展示所需的统计数据")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        
        dashboard.put("totalTemplates", templateRepository.count());
        dashboard.put("activeTemplates", 
                templateRepository.countByStatus(TemplateStatus.PUBLISHED));
        dashboard.put("draftTemplates", 
                templateRepository.countByStatus(TemplateStatus.DRAFT));
        dashboard.put("deprecatedTemplates", 
                templateRepository.countByStatus(TemplateStatus.DEPRECATED));
        
        List<Map<String, Object>> topDomains = new ArrayList<>();
        for (BusinessDomain domain : BusinessDomain.values()) {
            long count = templateRepository.countByBusinessDomain(domain);
            if (count > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("domain", domain.name());
                item.put("count", count);
                topDomains.add(item);
            }
        }
        topDomains.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        dashboard.put("topDomains", topDomains.size() > 5 ? topDomains.subList(0, 5) : topDomains);
        
        return ResponseEntity.ok(dashboard);
    }
}
