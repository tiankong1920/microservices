package com.inventory.configservice.controller;

import com.inventory.common.core.ApiResponse;
import com.inventory.configservice.dto.ConfigDTO;
import com.inventory.configservice.dto.ConfigHistoryDTO;
import com.inventory.configservice.service.ConfigManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ConfigManagementControllerTest {

    @Mock
    private ConfigManagementService configService;

    @InjectMocks
    private ConfigManagementController configManagementController;

    @Test
    void testGetConfigFound() {
        when(configService.getConfig("app.yaml", "DEFAULT_GROUP")).thenReturn("k: v");

        ResponseEntity<ApiResponse<String>> response = configManagementController.getConfig("app.yaml", "DEFAULT_GROUP");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("k: v", response.getBody().getData());
    }

    @Test
    void testGetConfigNotFound() {
        when(configService.getConfig("missing.yaml", "DEFAULT_GROUP")).thenReturn(null);

        ResponseEntity<ApiResponse<String>> response = configManagementController.getConfig("missing.yaml", "DEFAULT_GROUP");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetConfigDetail() {
        ConfigDTO dto = new ConfigDTO("app.yaml", "DEFAULT_GROUP", "ns", "k: v", "yaml", "desc", "app", 0L, 0L);
        when(configService.getConfigDetail(anyString(), anyString(), any())).thenReturn(dto);

        ResponseEntity<ApiResponse<ConfigDTO>> response =
                configManagementController.getConfigDetail("app.yaml", "DEFAULT_GROUP", "ns");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("app.yaml", response.getBody().getData().dataId());
    }

    @Test
    void testPublishConfigSuccess() {
        when(configService.publishConfig(anyString(), anyString(), any(), anyString())).thenReturn(true);

        ConfigManagementController.PublishConfigRequest req = new ConfigManagementController.PublishConfigRequest();
        req.setDataId("app.yaml");
        req.setGroup("DEFAULT_GROUP");
        req.setContent("k: v");
        req.setType("yaml");

        ResponseEntity<ApiResponse<java.util.Map<String, Object>>> response =
                configManagementController.publishConfig(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().getData().get("success"));
    }

    @Test
    void testPublishConfigFailure() {
        when(configService.publishConfig(anyString(), anyString(), any(), anyString())).thenReturn(false);

        ConfigManagementController.PublishConfigRequest req = new ConfigManagementController.PublishConfigRequest();
        req.setDataId("app.yaml");
        req.setGroup("DEFAULT_GROUP");
        req.setContent("k: v");

        ResponseEntity<ApiResponse<java.util.Map<String, Object>>> response =
                configManagementController.publishConfig(req);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("PUBLISH_FAILED", response.getBody().getErrorCode());
    }

    @Test
    void testRemoveConfigSuccess() {
        when(configService.removeConfig("app.yaml", "DEFAULT_GROUP")).thenReturn(true);

        ResponseEntity<ApiResponse<Boolean>> response =
                configManagementController.removeConfig("app.yaml", "DEFAULT_GROUP");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData());
    }

    @Test
    void testRemoveConfigFailed() {
        when(configService.removeConfig("missing.yaml", "DEFAULT_GROUP")).thenReturn(false);

        ResponseEntity<ApiResponse<Boolean>> response =
                configManagementController.removeConfig("missing.yaml", "DEFAULT_GROUP");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("DELETE_FAILED", response.getBody().getErrorCode());
    }

    @Test
    void testGetConfigHistory() {
        ConfigHistoryDTO history = new ConfigHistoryDTO(1L, "app.yaml", "DEFAULT_GROUP",
                "ns", "k: v", "publish", "127.0.0.1", "user", 0L, 0L);
        when(configService.getConfigHistory(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(history));

        ResponseEntity<ApiResponse<List<ConfigHistoryDTO>>> response =
                configManagementController.getConfigHistory("app.yaml", "DEFAULT_GROUP", 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testListConfigs() {
        ConfigDTO dto = new ConfigDTO("a", "g", "n", "c", "yaml", "d", "app", 0L, 0L);
        when(configService.listConfigs(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dto));

        ResponseEntity<ApiResponse<List<ConfigDTO>>> response =
                configManagementController.listConfigs("DEFAULT_GROUP", 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }
}
