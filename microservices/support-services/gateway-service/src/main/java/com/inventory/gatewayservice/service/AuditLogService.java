package com.inventory.gatewayservice.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AuditLogService {

    @Data
    @Builder
    public static class AuditLogEntry {
        private Instant timestamp;
        private String method;
        private String path;
        private int statusCode;
        private long duration;
        private String clientIp;
        private String userAgent;
        private String apiKey;
        private String error;
    }

    public void log(AuditLogEntry entry) {
        log.info("AuditLog: {} {} {} - Status: {} - Duration: {}ms - IP: {} - API Key: {} - UserAgent: {} - Error: {}",
            entry.getTimestamp(),
            entry.getMethod(),
            entry.getPath(),
            entry.getStatusCode(),
            entry.getDuration(),
            entry.getClientIp(),
            maskApiKey(entry.getApiKey()),
            entry.getUserAgent(),
            entry.getError());
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
