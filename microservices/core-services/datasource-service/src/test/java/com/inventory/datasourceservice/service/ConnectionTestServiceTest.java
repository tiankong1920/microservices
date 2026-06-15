package com.inventory.datasourceservice.service;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.ConnectionStatus;
import com.inventory.datasourceservice.entity.ConnectionTestLog;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.plugin.PluginRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionTestService Unit Tests")
class ConnectionTestServiceTest {
    @Mock
    private IDatasourceConfigRepository datasourceConfigRepository;
    @Mock
    private IConnectionStatusRepository connectionStatusRepository;
    @Mock
    private IConnectionTestLogRepository connectionTestLogRepository;
    @Mock
    private AlertService alertService;
    @Mock
    private PluginRegistry pluginRegistry;
    @Mock
    private TenantContext tenantContext;
    @Mock
    private UserContext userContext;
    private ConnectionTestService connectionTestService;
    private DatasourceConfig testConfig;

    @BeforeEach
    void setUp() {
        connectionTestService = new ConnectionTestService(
            datasourceConfigRepository, connectionStatusRepository,
            connectionTestLogRepository, alertService, pluginRegistry,
            tenantContext, userContext);
        testConfig = DatasourceConfig.builder()
            .id(1L).tenantId("tenant-001").name("test-mysql")
            .type(DatasourceConfig.DatasourceType.MYSQL)
            .host("localhost").port(3306).build();
    }

    @Test
    @DisplayName("Should throw exception when datasource not found")
    void shouldThrowExceptionWhenDatasourceNotFound() {
        when(tenantContext.getCurrentTenant()).thenReturn("tenant-001");
        when(datasourceConfigRepository.findByIdAndTenantId(1L, "tenant-001"))
            .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> connectionTestService.testConnection(1L));
    }

    @Test
    @DisplayName("Should send alert when connection test fails")
    void shouldSendAlertWhenConnectionTestFails() {
        when(tenantContext.getCurrentTenant()).thenReturn("tenant-001");
        when(datasourceConfigRepository.findByIdAndTenantId(1L, "tenant-001"))
            .thenReturn(Optional.of(testConfig));
        DataSourcePlugin plugin = mock(DataSourcePlugin.class);
        when(pluginRegistry.getRequiredPlugin(testConfig.getType())).thenReturn(plugin);
        ConnectionTestResultDTO failureResult = ConnectionTestResultDTO.failure(
            1L, null, "CONNECTION_ERROR", "Connection refused", null);
        when(plugin.testConnection(testConfig)).thenReturn(failureResult);
        when(connectionTestLogRepository.save(any())).thenReturn(new ConnectionTestLog());
        when(connectionStatusRepository.save(any())).thenReturn(new ConnectionStatus());
        ConnectionTestResultDTO result = connectionTestService.testConnection(1L);
        assertEquals("CONNECTION_ERROR", result.getErrorCode());
        verify(alertService).sendConnectionAlert(eq(testConfig), any());
    }
}
