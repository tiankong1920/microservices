package com.inventory.datasourceservice.controller;

import com.inventory.datasourceservice.entity.ConnectionStatus;
import com.inventory.datasourceservice.entity.ConnectionTestLog;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.service.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController Unit Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDatasourceConfigRepository datasourceConfigRepository;

    @MockitoBean
    private IConnectionStatusRepository connectionStatusRepository;

    @MockitoBean
    private IConnectionTestLogRepository connectionTestLogRepository;

    @MockitoBean
    private TenantContext tenantContext;

    private static final String TENANT_ID = "test-tenant";

    @BeforeEach
    void setUp() {
        when(tenantContext.getCurrentTenant()).thenReturn(TENANT_ID);
    }

    @Nested
    @DisplayName("getDashboardStats() endpoint tests")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("should return dashboard stats successfully")
        void shouldReturnDashboardStatsSuccessfully() throws Exception {
            DatasourceConfig datasource = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .name("test-mysql")
                    .type(DatasourceConfig.DatasourceType.MYSQL)
                    .host("localhost")
                    .port(3306)
                    .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                    .build();

            ConnectionStatus connectedStatus = ConnectionStatus.builder()
                    .id(1L)
                    .datasourceId(1L)
                    .status(ConnectionStatus.ConnectionStatusEnum.CONNECTED)
                    .responseTime(100)
                    .checkedAt(LocalDateTime.now())
                    .build();

            when(datasourceConfigRepository.countByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(1L);

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of(datasource));

            when(connectionStatusRepository.findLatestByDatasourceIds(List.of(1L)))
                    .thenReturn(List.of(connectedStatus));

            when(connectionTestLogRepository.countByResultSince(any(), any()))
                    .thenReturn(10L);

            when(connectionTestLogRepository.countByResultSince(
                    eq(ConnectionTestLog.TestResult.SUCCESS), any()))
                    .thenReturn(8L);

            mockMvc.perform(get("/api/v1/dashboard/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalDatasources").value(1))
                    .andExpect(jsonPath("$.data.connectedDatasources").value(1))
                    .andExpect(jsonPath("$.data.disconnectedDatasources").value(0))
                    .andExpect(jsonPath("$.data.errorDatasources").value(0));
        }

        @Test
        @DisplayName("should return zero stats when no datasources exist")
        void shouldReturnZeroStatsWhenNoDatasourcesExist() throws Exception {
            when(datasourceConfigRepository.countByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(0L);

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of());

            when(connectionStatusRepository.findLatestByDatasourceIds(any()))
                    .thenReturn(List.of());

            when(connectionTestLogRepository.countByResultSince(any(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/v1/dashboard/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalDatasources").value(0))
                    .andExpect(jsonPath("$.data.connectedDatasources").value(0))
                    .andExpect(jsonPath("$.data.averageResponseTime").value(0));
        }

        @Test
        @DisplayName("should calculate correct success rate")
        void shouldCalculateCorrectSuccessRate() throws Exception {
            when(datasourceConfigRepository.countByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(1L);

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of());

            when(connectionStatusRepository.findLatestByDatasourceIds(any()))
                    .thenReturn(List.of());

            when(connectionTestLogRepository.countByResultSince(any(), any()))
                    .thenReturn(100L);

            when(connectionTestLogRepository.countByResultSince(
                    eq(ConnectionTestLog.TestResult.SUCCESS), any()))
                    .thenReturn(75L);

            mockMvc.perform(get("/api/v1/dashboard/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.connectionSuccessRate").value(0.75));
        }
    }

    @Nested
    @DisplayName("getHealthOverview() endpoint tests")
    class GetHealthOverviewTests {

        @Test
        @DisplayName("should return health overview successfully")
        void shouldReturnHealthOverviewSuccessfully() throws Exception {
            DatasourceConfig datasource = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .name("test-mysql")
                    .type(DatasourceConfig.DatasourceType.MYSQL)
                    .host("localhost")
                    .port(3306)
                    .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                    .build();

            ConnectionStatus connectedStatus = ConnectionStatus.builder()
                    .id(1L)
                    .datasourceId(1L)
                    .status(ConnectionStatus.ConnectionStatusEnum.CONNECTED)
                    .responseTime(50)
                    .checkedAt(LocalDateTime.now())
                    .build();

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of(datasource));

            when(connectionStatusRepository.findLatestByDatasourceIds(List.of(1L)))
                    .thenReturn(List.of(connectedStatus));

            mockMvc.perform(get("/api/v1/dashboard/health-overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("test-mysql"))
                    .andExpect(jsonPath("$.data[0].type").value("MYSQL"))
                    .andExpect(jsonPath("$.data[0].status").value("CONNECTED"));
        }

        @Test
        @DisplayName("should return UNKNOWN status when no connection status exists")
        void shouldReturnUnknownStatusWhenNoConnectionStatusExists() throws Exception {
            DatasourceConfig datasource = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .name("test-postgres")
                    .type(DatasourceConfig.DatasourceType.POSTGRESQL)
                    .host("localhost")
                    .port(5432)
                    .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                    .build();

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of(datasource));

            when(connectionStatusRepository.findLatestByDatasourceIds(List.of(1L)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/dashboard/health-overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].status").value("UNKNOWN"));
        }

        @Test
        @DisplayName("should return multiple datasources with different statuses")
        void shouldReturnMultipleDatasourcesWithDifferentStatuses() throws Exception {
            DatasourceConfig mysqlDs = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .name("mysql-primary")
                    .type(DatasourceConfig.DatasourceType.MYSQL)
                    .host("mysql.local")
                    .port(3306)
                    .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                    .build();

            DatasourceConfig postgresDs = DatasourceConfig.builder()
                    .id(2L)
                    .tenantId(TENANT_ID)
                    .name("postgres-replica")
                    .type(DatasourceConfig.DatasourceType.POSTGRESQL)
                    .host("postgres.local")
                    .port(5432)
                    .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                    .build();

            ConnectionStatus mysqlStatus = ConnectionStatus.builder()
                    .id(1L)
                    .datasourceId(1L)
                    .status(ConnectionStatus.ConnectionStatusEnum.CONNECTED)
                    .responseTime(30)
                    .checkedAt(LocalDateTime.now())
                    .build();

            ConnectionStatus postgresStatus = ConnectionStatus.builder()
                    .id(2L)
                    .datasourceId(2L)
                    .status(ConnectionStatus.ConnectionStatusEnum.ERROR)
                    .errorMessage("Connection refused")
                    .responseTime(0)
                    .checkedAt(LocalDateTime.now())
                    .build();

            when(datasourceConfigRepository.findByTenantIdAndStatus(
                    eq(TENANT_ID), eq(DatasourceConfig.DatasourceStatus.ACTIVE)))
                    .thenReturn(List.of(mysqlDs, postgresDs));

            when(connectionStatusRepository.findLatestByDatasourceIds(List.of(1L, 2L)))
                    .thenReturn(List.of(mysqlStatus, postgresStatus));

            mockMvc.perform(get("/api/v1/dashboard/health-overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].status").value("CONNECTED"))
                    .andExpect(jsonPath("$.data[1].status").value("ERROR"))
                    .andExpect(jsonPath("$.data[1].errorMessage").value("Connection refused"));
        }
    }
}
