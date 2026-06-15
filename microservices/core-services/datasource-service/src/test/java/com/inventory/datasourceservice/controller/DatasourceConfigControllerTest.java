package com.inventory.datasourceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.dto.DatasourceConfigDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.service.DatasourceConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatasourceConfigController.class)
@DisplayName("DatasourceConfigController Unit Tests")
class DatasourceConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DatasourceConfigService datasourceConfigService;

    private DatasourceConfigDTO testDTO;

    @BeforeEach
    void setUp() {
        testDTO = DatasourceConfigDTO.builder()
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

    @Nested
    @DisplayName("createDatasource() endpoint tests")
    class CreateDatasourceTests {

        @Test
        @DisplayName("should create datasource successfully")
        void shouldCreateDatasourceSuccessfully() throws Exception {
            when(datasourceConfigService.createDatasource(any(DatasourceConfigDTO.class)))
                    .thenReturn(testDTO);

            mockMvc.perform(post("/api/v1/datasources")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("数据源创建成功"))
                    .andExpect(jsonPath("$.data.name").value("test-mysql"));

            verify(datasourceConfigService).createDatasource(any(DatasourceConfigDTO.class));
        }
    }

    @Nested
    @DisplayName("updateDatasource() endpoint tests")
    class UpdateDatasourceTests {

        @Test
        @DisplayName("should update datasource successfully")
        void shouldUpdateDatasourceSuccessfully() throws Exception {
            when(datasourceConfigService.updateDatasource(eq(1L), any(DatasourceConfigDTO.class)))
                    .thenReturn(testDTO);

            mockMvc.perform(put("/api/v1/datasources/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("数据源更新成功"));

            verify(datasourceConfigService).updateDatasource(eq(1L), any(DatasourceConfigDTO.class));
        }
    }

    @Nested
    @DisplayName("deleteDatasource() endpoint tests")
    class DeleteDatasourceTests {

        @Test
        @DisplayName("should delete datasource successfully")
        void shouldDeleteDatasourceSuccessfully() throws Exception {
            doNothing().when(datasourceConfigService).deleteDatasource(1L);

            mockMvc.perform(delete("/api/v1/datasources/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("数据源删除成功"));

            verify(datasourceConfigService).deleteDatasource(1L);
        }
    }

    @Nested
    @DisplayName("getDatasource() endpoint tests")
    class GetDatasourceTests {

        @Test
        @DisplayName("should get datasource by id")
        void shouldGetDatasourceById() throws Exception {
            when(datasourceConfigService.getDatasource(1L)).thenReturn(testDTO);

            mockMvc.perform(get("/api/v1/datasources/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("test-mysql"));

            verify(datasourceConfigService).getDatasource(1L);
        }
    }

    @Nested
    @DisplayName("listDatasources() endpoint tests")
    class ListDatasourcesTests {

        @Test
        @DisplayName("should list datasources with pagination")
        void shouldListDatasourcesWithPagination() throws Exception {
            PageResponse<DatasourceConfigDTO> pageResponse = PageResponse.<DatasourceConfigDTO>builder()
                    .content(List.of(testDTO))
                    .totalElements(1L)
                    .totalPages(1)
                    .size(10)
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            when(datasourceConfigService.listDatasources(0, 10, "createdAt", "desc"))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/datasources")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value("test-mysql"));

            verify(datasourceConfigService).listDatasources(0, 10, "createdAt", "desc");
        }
    }

    @Nested
    @DisplayName("searchDatasources() endpoint tests")
    class SearchDatasourcesTests {

        @Test
        @DisplayName("should search datasources by criteria")
        void shouldSearchDatasourcesByCriteria() throws Exception {
            PageResponse<DatasourceConfigDTO> pageResponse = PageResponse.<DatasourceConfigDTO>builder()
                    .content(List.of(testDTO))
                    .totalElements(1L)
                    .totalPages(1)
                    .size(10)
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            when(datasourceConfigService.searchDatasources("test", null, null, 0, 10))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/datasources/search")
                            .param("name", "test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(datasourceConfigService).searchDatasources("test", null, null, 0, 10);
        }

        @Test
        @DisplayName("should search datasources by type")
        void shouldSearchDatasourcesByType() throws Exception {
            PageResponse<DatasourceConfigDTO> pageResponse = PageResponse.<DatasourceConfigDTO>builder()
                    .content(List.of(testDTO))
                    .totalElements(1L)
                    .totalPages(1)
                    .size(10)
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            when(datasourceConfigService.searchDatasources(null, DatasourceConfig.DatasourceType.MYSQL, null, 0, 10))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/datasources/search")
                            .param("type", "MYSQL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(datasourceConfigService).searchDatasources(null, DatasourceConfig.DatasourceType.MYSQL, null, 0, 10);
        }
    }

    @Nested
    @DisplayName("listByType() endpoint tests")
    class ListByTypeTests {

        @Test
        @DisplayName("should list datasources by type")
        void shouldListDatasourcesByType() throws Exception {
            when(datasourceConfigService.listByType(DatasourceConfig.DatasourceType.MYSQL))
                    .thenReturn(List.of(testDTO));

            mockMvc.perform(get("/api/v1/datasources/type/MYSQL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(datasourceConfigService).listByType(DatasourceConfig.DatasourceType.MYSQL);
        }
    }

    @Nested
    @DisplayName("getConfigSchema() endpoint tests")
    class GetConfigSchemaTests {

        @Test
        @DisplayName("should get config schema for type")
        void shouldGetConfigSchemaForType() throws Exception {
            DataSourcePlugin.ConfigSchema schema = DataSourcePlugin.ConfigSchema.builder()
                    .type(DatasourceConfig.DatasourceType.MYSQL)
                    .fields(List.of())
                    .defaults(java.util.Map.of())
                    .build();
            when(datasourceConfigService.getConfigSchema(DatasourceConfig.DatasourceType.MYSQL))
                    .thenReturn(schema);

            mockMvc.perform(get("/api/v1/datasources/config-schema/MYSQL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(datasourceConfigService).getConfigSchema(DatasourceConfig.DatasourceType.MYSQL);
        }
    }
}
