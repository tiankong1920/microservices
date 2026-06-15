package com.inventory.datasourceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.ConnectionTestLog;
import com.inventory.datasourceservice.service.ConnectionTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConnectionTestController.class)
@DisplayName("ConnectionTestController Unit Tests")
class ConnectionTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConnectionTestService connectionTestService;

    private ConnectionTestResultDTO testResult;

    @BeforeEach
    void setUp() {
        testResult = ConnectionTestResultDTO.success(1L, "test-mysql", 100);
    }

    @Nested
    @DisplayName("testConnection() endpoint tests")
    class TestConnectionTests {

        @Test
        @DisplayName("should test connection successfully")
        void shouldTestConnectionSuccessfully() throws Exception {
            when(connectionTestService.testConnection(1L)).thenReturn(testResult);

            mockMvc.perform(post("/api/v1/connection-test/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.datasourceId").value(1))
                    .andExpect(jsonPath("$.data.datasourceName").value("test-mysql"));

            verify(connectionTestService).testConnection(1L);
        }
    }

    @Nested
    @DisplayName("batchTestConnections() endpoint tests")
    class BatchTestConnectionsTests {

        @Test
        @DisplayName("should batch test connections successfully")
        void shouldBatchTestConnectionsSuccessfully() throws Exception {
            List<ConnectionTestResultDTO> results = List.of(testResult);
            when(connectionTestService.batchTestConnections(anyList())).thenReturn(results);

            mockMvc.perform(post("/api/v1/connection-test/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1, 2, 3]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(connectionTestService).batchTestConnections(anyList());
        }
    }

    @Nested
    @DisplayName("getTestHistory() endpoint tests")
    class GetTestHistoryTests {

        @Test
        @DisplayName("should get test history with pagination")
        void shouldGetTestHistoryWithPagination() throws Exception {
            ConnectionTestLog log = ConnectionTestLog.builder()
                    .id(1L)
                    .datasourceId(1L)
                    .testType(ConnectionTestLog.TestType.MANUAL)
                    .result(ConnectionTestLog.TestResult.SUCCESS)
                    .responseTime(100)
                    .testedAt(LocalDateTime.now())
                    .build();

            PageResponse<ConnectionTestLog> pageResponse = PageResponse.<ConnectionTestLog>builder()
                    .content(List.of(log))
                    .totalElements(1L)
                    .totalPages(1)
                    .size(10)
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            when(connectionTestService.getTestHistory(eq(1L), eq(0), eq(10), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/connection-test/1/history")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(connectionTestService).getTestHistory(eq(1L), eq(0), eq(10), isNull(), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("should get test history with filters")
        void shouldGetTestHistoryWithFilters() throws Exception {
            ConnectionTestLog log = ConnectionTestLog.builder()
                    .id(1L)
                    .datasourceId(1L)
                    .testType(ConnectionTestLog.TestType.MANUAL)
                    .result(ConnectionTestLog.TestResult.SUCCESS)
                    .responseTime(100)
                    .testedAt(LocalDateTime.now())
                    .build();

            PageResponse<ConnectionTestLog> pageResponse = PageResponse.<ConnectionTestLog>builder()
                    .content(List.of(log))
                    .totalElements(1L)
                    .totalPages(1)
                    .size(10)
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            when(connectionTestService.getTestHistory(eq(1L), eq(0), eq(10), 
                    eq(ConnectionTestLog.TestResult.SUCCESS), eq(ConnectionTestLog.TestType.MANUAL), 
                    isNull(), isNull()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/connection-test/1/history")
                            .param("page", "0")
                            .param("size", "10")
                            .param("result", "SUCCESS")
                            .param("testType", "MANUAL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(connectionTestService).getTestHistory(eq(1L), eq(0), eq(10), 
                    eq(ConnectionTestLog.TestResult.SUCCESS), eq(ConnectionTestLog.TestType.MANUAL), 
                    isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("getTestStatistics() endpoint tests")
    class GetTestStatisticsTests {

        @Test
        @DisplayName("should get test statistics")
        void shouldGetTestStatistics() throws Exception {
            Map<String, Object> stats = Map.of(
                    "totalTests", 10,
                    "successCount", 8,
                    "failureCount", 2,
                    "successRate", 80.0
            );
            when(connectionTestService.getTestStatistics(eq(1L), any(LocalDateTime.class)))
                    .thenReturn(stats);

            mockMvc.perform(get("/api/v1/connection-test/1/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalTests").value(10));

            verify(connectionTestService).getTestStatistics(eq(1L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should get test statistics with custom since time")
        void shouldGetTestStatisticsWithCustomSinceTime() throws Exception {
            Map<String, Object> stats = Map.of("totalTests", 5);
            when(connectionTestService.getTestStatistics(eq(1L), any(LocalDateTime.class)))
                    .thenReturn(stats);

            mockMvc.perform(get("/api/v1/connection-test/1/statistics")
                            .param("since", "2026-01-01T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(connectionTestService).getTestStatistics(eq(1L), any(LocalDateTime.class));
        }
    }
}
