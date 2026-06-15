package com.inventory.datasourceservice.service;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.ConnectionStatus;
import com.inventory.datasourceservice.entity.ConnectionTestLog;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.plugin.PluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionTestService {

    private final IDatasourceConfigRepository datasourceConfigRepository;
    private final IConnectionStatusRepository connectionStatusRepository;
    private final IConnectionTestLogRepository connectionTestLogRepository;
    private final AlertService alertService;
    private final PluginRegistry pluginRegistry;
    private final TenantContext tenantContext;
    private final UserContext userContext;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<Long, Boolean> testingDatasources = new ConcurrentHashMap<>();

    @Transactional
    public ConnectionTestResultDTO testConnection(Long datasourceId) {
        if (testingDatasources.getOrDefault(datasourceId, false)) {
            return ConnectionTestResultDTO.failure(
                    datasourceId, null, "TEST_IN_PROGRESS", "连接测试正在进行中", null
            );
        }

        testingDatasources.put(datasourceId, true);
        
        try {
            String tenantId = tenantContext.getCurrentTenant();
            DatasourceConfig config = datasourceConfigRepository.findByIdAndTenantId(datasourceId, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

            DataSourcePlugin plugin = pluginRegistry.getRequiredPlugin(config.getType());

            ConnectionTestResultDTO result = plugin.testConnection(config);

            saveTestLog(config, result, ConnectionTestLog.TestType.MANUAL);
            updateConnectionStatus(config, result);

            if (result.getResult() == ConnectionTestLog.TestResult.FAILURE ||
                result.getResult() == ConnectionTestLog.TestResult.TIMEOUT) {
                alertService.sendConnectionAlert(config, result);
            }

            return result;
        } finally {
            testingDatasources.remove(datasourceId);
        }
    }

    @Async
    public CompletableFuture<ConnectionTestResultDTO> testConnectionAsync(Long datasourceId) {
        return CompletableFuture.completedFuture(testConnection(datasourceId));
    }

    @Transactional
    public List<ConnectionTestResultDTO> batchTestConnections(List<Long> datasourceIds) {
        List<CompletableFuture<ConnectionTestResultDTO>> futures = new ArrayList<>();
        
        for (Long id : datasourceIds) {
            futures.add(testConnectionAsync(id));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    @Transactional
    public void autoTestAfterSave(Long datasourceId) {
        try {
            String tenantId = tenantContext.getCurrentTenant();
            DatasourceConfig config = datasourceConfigRepository.findByIdAndTenantId(datasourceId, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + datasourceId));

            DataSourcePlugin plugin = pluginRegistry.getPlugin(config.getType()).orElse(null);
            if (plugin == null) {
                log.warn("No plugin found for type: {}", config.getType());
                return;
            }

            ConnectionTestResultDTO result = plugin.testConnection(config);

            saveTestLog(config, result, ConnectionTestLog.TestType.AUTO);
            updateConnectionStatus(config, result);

            if (result.getResult() == ConnectionTestLog.TestResult.FAILURE ||
                result.getResult() == ConnectionTestLog.TestResult.TIMEOUT) {
                alertService.sendConnectionAlert(config, result);
            }
        } catch (Exception e) {
            log.error("Auto test failed for datasource: {}", datasourceId, e);
        }
    }

    @Transactional
    public void scheduledHealthCheck() {
        String tenantId = tenantContext.getCurrentTenant();
        List<DatasourceConfig> activeDatasources = datasourceConfigRepository
                .findByTenantIdAndStatus(tenantId, DatasourceConfig.DatasourceStatus.ACTIVE);

        for (DatasourceConfig config : activeDatasources) {
            try {
                DataSourcePlugin plugin = pluginRegistry.getPlugin(config.getType()).orElse(null);
                if (plugin == null) continue;

                ConnectionTestResultDTO result = plugin.testConnection(config);

                saveTestLog(config, result, ConnectionTestLog.TestType.SCHEDULED);
                updateConnectionStatus(config, result);

                if (result.getResult() == ConnectionTestLog.TestResult.FAILURE ||
                    result.getResult() == ConnectionTestLog.TestResult.TIMEOUT) {
                    alertService.sendConnectionAlert(config, result);
                }
            } catch (Exception e) {
                log.error("Health check failed for datasource: {}", config.getId(), e);
            }
        }
    }

    public PageResponse<ConnectionTestLog> getTestHistory(Long datasourceId, int page, int size,
                                                           ConnectionTestLog.TestResult result,
                                                           ConnectionTestLog.TestType testType,
                                                           LocalDateTime startTime,
                                                           LocalDateTime endTime) {
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, 
                        org.springframework.data.domain.Sort.by("testedAt").descending());

        org.springframework.data.domain.Page<ConnectionTestLog> logs = connectionTestLogRepository
                .search(datasourceId, result, testType, startTime, endTime, pageable);

        return PageResponse.of(
                logs.getContent(),
                logs.getTotalElements(),
                page,
                size
        );
    }

    public Map<String, Object> getTestStatistics(Long datasourceId, LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalTests = connectionTestLogRepository.countByResultSince(null, since);
        long successTests = connectionTestLogRepository.countByResultSince(ConnectionTestLog.TestResult.SUCCESS, since);
        long failedTests = connectionTestLogRepository.countByResultSince(ConnectionTestLog.TestResult.FAILURE, since);
        long timeoutTests = connectionTestLogRepository.countByResultSince(ConnectionTestLog.TestResult.TIMEOUT, since);
        Double avgResponseTime = connectionTestLogRepository.getAverageResponseTime(datasourceId, since);

        stats.put("totalTests", totalTests);
        stats.put("successTests", successTests);
        stats.put("failedTests", failedTests);
        stats.put("timeoutTests", timeoutTests);
        stats.put("successRate", totalTests > 0 ? (double) successTests / totalTests : 0);
        stats.put("averageResponseTime", avgResponseTime != null ? avgResponseTime : 0);

        return stats;
    }

    private void saveTestLog(DatasourceConfig config, ConnectionTestResultDTO result, 
                             ConnectionTestLog.TestType testType) {
        ConnectionTestLog log = ConnectionTestLog.builder()
                .datasourceId(config.getId())
                .testType(testType)
                .result(result.getResult())
                .responseTime(result.getResponseTime())
                .errorCode(result.getErrorCode())
                .errorMessage(result.getErrorMessage())
                .suggestions(result.getSuggestions() != null ? 
                        String.join("\n", result.getSuggestions()) : null)
                .testedBy(userContext.getCurrentUserId())
                .testedAt(LocalDateTime.now())
                .build();

        connectionTestLogRepository.save(log);
    }

    private void updateConnectionStatus(DatasourceConfig config, ConnectionTestResultDTO result) {
        ConnectionStatus.ConnectionStatusEnum status;
        if (result.getResult() == ConnectionTestLog.TestResult.SUCCESS) {
            status = ConnectionStatus.ConnectionStatusEnum.CONNECTED;
        } else if (result.getResult() == ConnectionTestLog.TestResult.TIMEOUT) {
            status = ConnectionStatus.ConnectionStatusEnum.ERROR;
        } else {
            status = ConnectionStatus.ConnectionStatusEnum.DISCONNECTED;
        }

        ConnectionStatus connectionStatus = ConnectionStatus.builder()
                .datasourceId(config.getId())
                .status(status)
                .responseTime(result.getResponseTime())
                .errorMessage(result.getErrorMessage())
                .checkedAt(LocalDateTime.now())
                .build();

        connectionStatusRepository.save(connectionStatus);

        if (result.getResult() == ConnectionTestLog.TestResult.SUCCESS) {
            config.setStatus(DatasourceConfig.DatasourceStatus.ACTIVE);
        } else {
            config.setStatus(DatasourceConfig.DatasourceStatus.ERROR);
        }
        datasourceConfigRepository.save(config);
    }
}
