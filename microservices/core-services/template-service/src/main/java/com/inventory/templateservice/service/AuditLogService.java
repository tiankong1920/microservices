package com.inventory.templateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.templateservice.entity.TemplateAuditLog;
import com.inventory.templateservice.repository.ITemplateAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final ITemplateAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_PUBLISH = "PUBLISH";
    public static final String OPERATION_DEPRECATE = "DEPRECATE";
    public static final String OPERATION_ARCHIVE = "ARCHIVE";
    public static final String OPERATION_ROLLBACK = "ROLLBACK";
    public static final String OPERATION_IMPORT = "IMPORT";
    public static final String OPERATION_EXPORT = "EXPORT";
    public static final String OPERATION_ADD_FIELD = "ADD_FIELD";
    public static final String OPERATION_REMOVE_FIELD = "REMOVE_FIELD";
    public static final String OPERATION_ADD_CUSTOM_FIELD = "ADD_CUSTOM_FIELD";
    public static final String OPERATION_REMOVE_CUSTOM_FIELD = "REMOVE_CUSTOM_FIELD";

    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILURE = "FAILURE";

    @Async
    @Transactional
    public void logOperation(Long templateId, String templateCode, String operation, 
                             String operator, String operatorIp, String tenantId) {
        logOperation(templateId, templateCode, operation, null, null, operator, operatorIp, tenantId, RESULT_SUCCESS, null);
    }

    @Async
    @Transactional
    public void logOperation(Long templateId, String templateCode, String operation,
                             Object oldValue, Object newValue,
                             String operator, String operatorIp, String tenantId) {
        logOperation(templateId, templateCode, operation, oldValue, newValue, operator, operatorIp, tenantId, RESULT_SUCCESS, null);
    }

    @Async
    @Transactional
    public void logOperation(Long templateId, String templateCode, String operation,
                             Object oldValue, Object newValue,
                             String operator, String operatorIp, String tenantId,
                             String result, String errorMessage) {
        try {
            TemplateAuditLog auditLog = TemplateAuditLog.builder()
                    .templateId(templateId)
                    .templateCode(templateCode)
                    .operation(operation)
                    .oldValue(toJson(oldValue))
                    .newValue(toJson(newValue))
                    .operator(operator)
                    .operatorIp(operatorIp)
                    .tenantId(tenantId)
                    .operationResult(result)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} by {}", templateCode, operation, operator);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    public List<TemplateAuditLog> getAuditLogsByTemplate(Long templateId) {
        return auditLogRepository.findByTemplateIdOrderByOperationTimeDesc(templateId);
    }

    public Page<TemplateAuditLog> getAuditLogsByTemplate(Long templateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByTemplateIdOrderByOperationTimeDesc(templateId, pageable);
    }

    public Page<TemplateAuditLog> getAuditLogsByTenant(String tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByTenantIdOrderByOperationTimeDesc(tenantId, pageable);
    }

    public List<TemplateAuditLog> getAuditLogsByTimeRange(Long templateId, 
                                                          LocalDateTime startTime, 
                                                          LocalDateTime endTime) {
        return auditLogRepository.findByTemplateIdAndTimeRange(templateId, startTime, endTime);
    }

    public Page<TemplateAuditLog> searchAuditLogs(String tenantId, String operation, 
                                                   String operator, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.search(tenantId, operation, operator, pageable);
    }

    public Map<String, Object> getAuditStatistics(Long templateId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalOperations", auditLogRepository.countByTemplateId(templateId));
        
        List<Object[]> operationCounts = auditLogRepository.countByOperation(templateId);
        Map<String, Long> operationDistribution = new LinkedHashMap<>();
        for (Object[] row : operationCounts) {
            operationDistribution.put((String) row[0], (Long) row[1]);
        }
        stats.put("operationDistribution", operationDistribution);

        return stats;
    }

    public List<TemplateAuditLog> getRecentOperations(String operator, int limit) {
        return auditLogRepository.findByOperatorOrderByOperationTimeDesc(operator)
                .stream()
                .limit(limit)
                .toList();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
