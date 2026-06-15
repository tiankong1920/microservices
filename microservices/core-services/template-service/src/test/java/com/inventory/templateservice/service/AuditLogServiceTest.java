package com.inventory.templateservice.service;

import com.inventory.templateservice.entity.TemplateAuditLog;
import com.inventory.templateservice.repository.ITemplateAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private ITemplateAuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    private AuditLogService auditLogService;

    private TemplateAuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testAuditLog = TemplateAuditLog.builder()
                .id(1L)
                .templateId(1L)
                .templateCode("TPL_TEST_001")
                .operation("CREATE")
                .operator("test-user")
                .operatorIp("127.0.0.1")
                .tenantId("default")
                .operationResult("SUCCESS")
                .operationTime(LocalDateTime.now())
                .build();

        auditLogService = new AuditLogService(auditLogRepository, objectMapper);
    }

    @Test
    @DisplayName("记录操作日志 - 成功")
    void logOperation_Success() {
        when(auditLogRepository.save(any(TemplateAuditLog.class))).thenReturn(testAuditLog);

        auditLogService.logOperation(1L, "TPL_TEST_001", "CREATE", "test-user", "127.0.0.1", "default");

        verify(auditLogRepository, timeout(1000)).save(any(TemplateAuditLog.class));
    }

    @Test
    @DisplayName("获取模板审计日志")
    void getAuditLogsByTemplate() {
        when(auditLogRepository.findByTemplateIdOrderByOperationTimeDesc(anyLong())).thenReturn(List.of(testAuditLog));

        List<TemplateAuditLog> result = auditLogService.getAuditLogsByTemplate(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).getOperation());
    }

    @Test
    @DisplayName("获取审计统计")
    void getAuditStatistics() {
        when(auditLogRepository.countByTemplateId(anyLong())).thenReturn(10L);
        when(auditLogRepository.countByOperation(anyLong())).thenReturn(List.of(new Object[]{"CREATE", 5L}, new Object[]{"UPDATE", 3L}));

        Map<String, Object> stats = auditLogService.getAuditStatistics(1L);

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalOperations"));
    }

    @Test
    @DisplayName("获取最近操作")
    void getRecentOperations() {
        when(auditLogRepository.findByOperatorOrderByOperationTimeDesc(anyString())).thenReturn(List.of(testAuditLog));

        List<TemplateAuditLog> result = auditLogService.getRecentOperations("test-user", 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
