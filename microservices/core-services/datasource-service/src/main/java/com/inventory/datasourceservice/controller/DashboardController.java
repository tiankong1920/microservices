package com.inventory.datasourceservice.controller;

import com.inventory.datasourceservice.dto.ApiResponse;
import com.inventory.datasourceservice.dto.DashboardStatsDTO;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.service.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器 - 管理数据源监控仪表盘
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘", description = "数据源监控仪表盘接口")
@Validated
public class DashboardController {

    private final IDatasourceConfigRepository datasourceConfigRepository;
    private final IConnectionStatusRepository connectionStatusRepository;
    private final IConnectionTestLogRepository connectionTestLogRepository;
    private final TenantContext tenantContext;

    /**
     * 获取仪表盘统计数据
     *
     * @return 数据源监控仪表盘的统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据", description = "获取数据源监控仪表盘的统计数据")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        String tenantId = tenantContext.getCurrentTenant();
        
        long totalDatasources = datasourceConfigRepository.countByTenantIdAndStatus(
                tenantId, com.inventory.datasourceservice.entity.DatasourceConfig.DatasourceStatus.ACTIVE);
        
        List<com.inventory.datasourceservice.entity.DatasourceConfig> activeDatasources = 
                datasourceConfigRepository.findByTenantIdAndStatus(
                        tenantId, com.inventory.datasourceservice.entity.DatasourceConfig.DatasourceStatus.ACTIVE);
        
        List<Long> datasourceIds = activeDatasources.stream()
                .map(com.inventory.datasourceservice.entity.DatasourceConfig::getId)
                .toList();
        
        List<com.inventory.datasourceservice.entity.ConnectionStatus> latestStatuses = 
                connectionStatusRepository.findLatestByDatasourceIds(datasourceIds);
        
        long connectedCount = latestStatuses.stream()
                .filter(s -> s.getStatus() == com.inventory.datasourceservice.entity.ConnectionStatus.ConnectionStatusEnum.CONNECTED)
                .count();
        long disconnectedCount = latestStatuses.stream()
                .filter(s -> s.getStatus() == com.inventory.datasourceservice.entity.ConnectionStatus.ConnectionStatusEnum.DISCONNECTED)
                .count();
        long errorCount = latestStatuses.stream()
                .filter(s -> s.getStatus() == com.inventory.datasourceservice.entity.ConnectionStatus.ConnectionStatusEnum.ERROR)
                .count();
        
        double avgResponseTime = latestStatuses.stream()
                .filter(s -> s.getResponseTime() != null)
                .mapToInt(com.inventory.datasourceservice.entity.ConnectionStatus::getResponseTime)
                .average()
                .orElse(0);
        
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        long totalTests = connectionTestLogRepository.countByResultSince(null, since);
        long successTests = connectionTestLogRepository.countByResultSince(
                com.inventory.datasourceservice.entity.ConnectionTestLog.TestResult.SUCCESS, since);
        
        Map<String, Long> typeDistribution = activeDatasources.stream()
                .collect(Collectors.groupingBy(
                        ds -> ds.getType().name(),
                        Collectors.counting()
                ));
        
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("CONNECTED", connectedCount);
        statusDistribution.put("DISCONNECTED", disconnectedCount);
        statusDistribution.put("ERROR", errorCount);
        
        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalDatasources(totalDatasources)
                .activeDatasources(totalDatasources)
                .connectedDatasources(connectedCount)
                .disconnectedDatasources(disconnectedCount)
                .errorDatasources(errorCount)
                .averageResponseTime(avgResponseTime)
                .connectionSuccessRate(totalTests > 0 ? (double) successTests / totalTests : 0)
                .totalTestCount(totalTests)
                .successTestCount(successTests)
                .failedTestCount(totalTests - successTests)
                .datasourceTypeDistribution(typeDistribution)
                .connectionStatusDistribution(statusDistribution)
                .lastUpdated(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取健康概览
     *
     * @return 所有数据源的健康状态概览
     */
    @GetMapping("/health-overview")
    @Operation(summary = "获取健康概览", description = "获取所有数据源的健康状态概览")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHealthOverview() {
        String tenantId = tenantContext.getCurrentTenant();
        
        List<com.inventory.datasourceservice.entity.DatasourceConfig> datasources = 
                datasourceConfigRepository.findByTenantIdAndStatus(
                        tenantId, com.inventory.datasourceservice.entity.DatasourceConfig.DatasourceStatus.ACTIVE);
        
        List<Long> datasourceIds = datasources.stream()
                .map(com.inventory.datasourceservice.entity.DatasourceConfig::getId)
                .toList();
        
        Map<Long, com.inventory.datasourceservice.entity.ConnectionStatus> statusMap = 
                connectionStatusRepository.findLatestByDatasourceIds(datasourceIds).stream()
                        .collect(Collectors.toMap(
                                com.inventory.datasourceservice.entity.ConnectionStatus::getDatasourceId,
                                s -> s,
                                (a, b) -> a
                        ));
        
        List<Map<String, Object>> overview = datasources.stream()
                .map(ds -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", ds.getId());
                    item.put("name", ds.getName());
                    item.put("type", ds.getType().name());
                    item.put("host", ds.getHost());
                    item.put("port", ds.getPort());
                    
                    com.inventory.datasourceservice.entity.ConnectionStatus status = statusMap.get(ds.getId());
                    if (status != null) {
                        item.put("status", status.getStatus().name());
                        item.put("responseTime", status.getResponseTime());
                        item.put("lastChecked", status.getCheckedAt());
                        item.put("errorMessage", status.getErrorMessage());
                    } else {
                        item.put("status", "UNKNOWN");
                    }
                    
                    return item;
                })
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
}
