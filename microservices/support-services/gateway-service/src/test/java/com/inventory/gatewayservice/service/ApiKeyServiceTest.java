package com.inventory.gatewayservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyServiceTest {

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService();
    }

    @Test
    void testIsValidApiKeyWithPredefinedKey() {
        assertTrue(apiKeyService.isValidApiKey("test-api-key-12345"));
    }

    @Test
    void testIsValidApiKeyWithInvalidKey() {
        assertFalse(apiKeyService.isValidApiKey("invalid-key"));
    }

    @Test
    void testIsWhitelistedPathWithPublicPath() {
        assertTrue(apiKeyService.isWhitelistedPath("/api/public/endpoint"));
    }

    @Test
    void testIsWhitelistedPathWithHealthPath() {
        assertTrue(apiKeyService.isWhitelistedPath("/api/health"));
    }

    @Test
    void testIsWhitelistedPathWithActuatorHealthPath() {
        assertTrue(apiKeyService.isWhitelistedPath("/api/actuator/health"));
    }

    @Test
    void testIsWhitelistedPathWithNonWhitelistedPath() {
        assertFalse(apiKeyService.isWhitelistedPath("/api/product/list"));
    }

    @Test
    void testAddAndRemoveApiKey() {
        apiKeyService.addApiKey("new-dynamic-key");
        assertTrue(apiKeyService.isValidApiKey("new-dynamic-key"));

        apiKeyService.removeApiKey("new-dynamic-key");
        assertFalse(apiKeyService.isValidApiKey("new-dynamic-key"));
    }
}
