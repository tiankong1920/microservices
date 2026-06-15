package com.inventory.gatewayservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLogServiceTest {

    private final AuditLogService auditLogService = new AuditLogService();

    @Test
    void testLogWithValidEntry() {
        AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .duration(50L)
                .clientIp("127.0.0.1")
                .userAgent("TestAgent")
                .apiKey("test-api-key-12345")
                .build();

        assertNotNull(entry);
        assertEquals("GET", entry.getMethod());
        assertEquals("/api/test", entry.getPath());
        assertEquals(200, entry.getStatusCode());

        auditLogService.log(entry);
    }

    @Test
    void testLogWithNullApiKey() {
        AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .method("POST")
                .path("/api/data")
                .statusCode(201)
                .duration(100L)
                .clientIp("192.168.1.1")
                .build();

        assertNotNull(entry);
        auditLogService.log(entry);
    }

    @Test
    void testLogWithShortApiKey() {
        AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .method("GET")
                .path("/api/short")
                .statusCode(200)
                .apiKey("short")
                .build();

        auditLogService.log(entry);
    }

    @Test
    void testLogWithError() {
        AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .method("GET")
                .path("/api/error")
                .statusCode(500)
                .error("Internal Server Error")
                .build();

        auditLogService.log(entry);
    }
}
