package com.inventory.gatewayservice.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ApiKeyService {

    private static final Set<String> VALID_API_KEYS = new HashSet<>();
    private static final Set<String> WHITELISTED_PATHS = Set.of(
        "/api/public",
        "/api/health",
        "/api/actuator/health"
    );

    static {
        VALID_API_KEYS.add("test-api-key-12345");
        VALID_API_KEYS.add("prod-api-key-67890");
    }

    public boolean isValidApiKey(String apiKey) {
        return VALID_API_KEYS.contains(apiKey);
    }

    public boolean isWhitelistedPath(String path) {
        return WHITELISTED_PATHS.stream().anyMatch(path::startsWith);
    }

    public void addApiKey(String apiKey) {
        VALID_API_KEYS.add(apiKey);
    }

    public void removeApiKey(String apiKey) {
        VALID_API_KEYS.remove(apiKey);
    }
}
