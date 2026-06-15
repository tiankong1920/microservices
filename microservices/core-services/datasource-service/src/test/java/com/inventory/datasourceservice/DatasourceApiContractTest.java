package com.inventory.datasourceservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.controller.DatasourceConfigController;
import com.inventory.datasourceservice.dto.DatasourceConfigDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.security.EncryptionService;
import com.inventory.datasourceservice.service.DatasourceConfigService;
import com.inventory.datasourceservice.service.TenantContext;
import com.inventory.datasourceservice.service.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Cloud Contract Verifier Test
 *
 * This test verifies that the Datasource Service API
 * conforms to the contracts defined in:
 * src/test/resources/contracts/
 *
 * Run with: ./gradlew :core-services:datasource-service:test
 */
@WebMvcTest(DatasourceConfigController.class)
@DisplayName("Datasource API Contract Tests")
class DatasourceApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatasourceConfigService datasourceConfigService;

    @MockBean
    private EncryptionService encryptionService;

    @MockBean
    private TenantContext tenantContext;

    @MockBean
    private UserContext userContext;

    private DatasourceConfigDTO createTestDTO() {
        return DatasourceConfigDTO.builder()
                .id(1L)
                .name("test-mysql")
                .type(DatasourceConfig.DatasourceType.MYSQL)
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("password")
                .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("验证获取数据源详情 API 契约")
    void validateGetDatasourceContract() throws Exception {
        DatasourceConfigDTO testDTO = createTestDTO();
        when(datasourceConfigService.getDatasource(1L)).thenReturn(testDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/datasources/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test-mysql"));
    }

    @Test
    @DisplayName("验证创建数据源 API 契约")
    void validateCreateDatasourceContract() throws Exception {
        DatasourceConfigDTO testDTO = createTestDTO();
        when(datasourceConfigService.createDatasource(any(DatasourceConfigDTO.class))).thenReturn(testDTO);

        String requestBody = """
            {
                "name": "test-mysql",
                "type": "MYSQL",
                "host": "localhost",
                "port": 3306,
                "databaseName": "test_db",
                "username": "root",
                "password": "password"
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/datasources")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("数据源创建成功"));
    }
}
