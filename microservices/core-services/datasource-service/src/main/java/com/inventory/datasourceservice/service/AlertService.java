package com.inventory.datasourceservice.service;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.AlertConfig;
import com.inventory.datasourceservice.entity.AlertHistory;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IAlertConfigRepository;
import com.inventory.datasourceservice.repository.IAlertHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final IAlertConfigRepository alertConfigRepository;
    private final IAlertHistoryRepository alertHistoryRepository;
    private final NotificationService notificationService;
    private final TenantContext tenantContext;

    @Async
    @Transactional
    public void sendConnectionAlert(DatasourceConfig datasource, ConnectionTestResultDTO result) {
        String tenantId = tenantContext.getCurrentTenant();
        
        List<AlertConfig> alertConfigs = alertConfigRepository
                .findActiveByTenantIdAndDatasourceId(tenantId, datasource.getId());

        for (AlertConfig config : alertConfigs) {
            try {
                String message = buildAlertMessage(datasource, result);
                
                AlertHistory history = AlertHistory.builder()
                        .tenantId(tenantId)
                        .datasourceId(datasource.getId())
                        .alertConfigId(config.getId())
                        .alertLevel(config.getAlertLevel())
                        .channel(config.getAlertChannels())
                        .message(message)
                        .status(AlertHistory.AlertStatus.PENDING)
                        .build();

                boolean sent = notificationService.send(config, message);
                
                history.setStatus(sent ? AlertHistory.AlertStatus.SENT : AlertHistory.AlertStatus.FAILED);
                history.setSentAt(sent ? LocalDateTime.now() : null);
                if (!sent) {
                    history.setErrorMessage("Failed to send notification");
                }
                
                alertHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("Failed to send alert for datasource: {}", datasource.getId(), e);
            }
        }
    }

    private String buildAlertMessage(DatasourceConfig datasource, ConnectionTestResultDTO result) {
        StringBuilder sb = new StringBuilder();
        sb.append("【数据源连接告警】\n");
        sb.append("数据源名称: ").append(datasource.getName()).append("\n");
        sb.append("数据源类型: ").append(datasource.getType()).append("\n");
        sb.append("主机地址: ").append(datasource.getHost()).append(":").append(datasource.getPort()).append("\n");
        sb.append("测试结果: ").append(result.getResult()).append("\n");
        
        if (result.getResponseTime() != null) {
            sb.append("响应时间: ").append(result.getResponseTime()).append("ms\n");
        }
        
        if (result.getErrorMessage() != null) {
            sb.append("错误信息: ").append(result.getErrorMessage()).append("\n");
        }
        
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            sb.append("建议解决方案:\n");
            for (int i = 0; i < result.getSuggestions().size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(result.getSuggestions().get(i)).append("\n");
            }
        }
        
        sb.append("告警时间: ").append(LocalDateTime.now()).append("\n");
        
        return sb.toString();
    }
}
