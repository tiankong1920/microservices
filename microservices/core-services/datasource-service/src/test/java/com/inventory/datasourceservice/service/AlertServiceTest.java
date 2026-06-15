package com.inventory.datasourceservice.service;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.AlertConfig;
import com.inventory.datasourceservice.entity.AlertHistory;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IAlertConfigRepository;
import com.inventory.datasourceservice.repository.IAlertHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    @Mock
    private IAlertConfigRepository alertConfigRepository;

    @Mock
    private IAlertHistoryRepository alertHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TenantContext tenantContext;

    private AlertService alertService;

    private static final String TEST_TENANT_ID = "tenant-001";

    private DatasourceConfig testDatasource;
    private AlertConfig testAlertConfig;
    private ConnectionTestResultDTO failureResult;

    @BeforeEach
    void setUp() {
        lenient().when(tenantContext.getCurrentTenant()).thenReturn(TEST_TENANT_ID);

        testDatasource = DatasourceConfig.builder()
                .id(1L)
                .tenantId(TEST_TENANT_ID)
                .name("test-mysql")
                .type(DatasourceConfig.DatasourceType.MYSQL)
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .build();

        testAlertConfig = AlertConfig.builder()
                .id(1L)
                .tenantId(TEST_TENANT_ID)
                .datasourceIds("[1]")
                .name("Test Alert")
                .alertLevel(AlertConfig.AlertLevel.WARNING)
                .alertChannels("EMAIL,SLACK")
                .enabled(true)
                .build();

        failureResult = ConnectionTestResultDTO.failure(
                1L, "test-mysql", "CONN_ERROR", "Connection refused",
                List.of("Check if MySQL is running"), 0
        );

        alertService = new AlertService(alertConfigRepository, alertHistoryRepository, notificationService, tenantContext);
    }

    @Nested
    @DisplayName("sendConnectionAlert() method tests")
    class SendConnectionAlertTests {

        @Test
        @DisplayName("should send alert when alert configs exist")
        void shouldSendAlertWhenAlertConfigsExist() {
            when(alertConfigRepository.findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L))
                    .thenReturn(List.of(testAlertConfig));
            when(notificationService.send(any(AlertConfig.class), anyString()))
                    .thenReturn(true);
            when(alertHistoryRepository.save(any(AlertHistory.class)))
                    .thenReturn(new AlertHistory());

            alertService.sendConnectionAlert(testDatasource, failureResult);

            verify(alertConfigRepository).findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L);
            verify(notificationService).send(eq(testAlertConfig), anyString());
            verify(alertHistoryRepository).save(argThat(history ->
                    history.getStatus() == AlertHistory.AlertStatus.SENT
            ));
        }

        @Test
        @DisplayName("should handle no alert configs")
        void shouldHandleNoAlertConfigs() {
            when(alertConfigRepository.findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L))
                    .thenReturn(List.of());

            alertService.sendConnectionAlert(testDatasource, failureResult);

            verify(alertConfigRepository).findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L);
            verify(notificationService, never()).send(any(), anyString());
            verify(alertHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should handle notification failure")
        void shouldHandleNotificationFailure() {
            when(alertConfigRepository.findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L))
                    .thenReturn(List.of(testAlertConfig));
            when(notificationService.send(any(AlertConfig.class), anyString()))
                    .thenReturn(false);
            when(alertHistoryRepository.save(any(AlertHistory.class)))
                    .thenReturn(new AlertHistory());

            alertService.sendConnectionAlert(testDatasource, failureResult);

            verify(alertHistoryRepository).save(argThat(history ->
                    history.getStatus() == AlertHistory.AlertStatus.FAILED
            ));
        }

        @Test
        @DisplayName("should handle exception during notification")
        void shouldHandleExceptionDuringNotification() {
            when(alertConfigRepository.findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L))
                    .thenReturn(List.of(testAlertConfig));
            when(notificationService.send(any(AlertConfig.class), anyString()))
                    .thenThrow(new RuntimeException("Notification error"));

            alertService.sendConnectionAlert(testDatasource, failureResult);

            verify(alertHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should include suggestions in alert message")
        void shouldIncludeSuggestionsInAlertMessage() {
            when(alertConfigRepository.findActiveByTenantIdAndDatasourceId(TEST_TENANT_ID, 1L))
                    .thenReturn(List.of(testAlertConfig));
            when(notificationService.send(any(AlertConfig.class), anyString()))
                    .thenReturn(true);
            when(alertHistoryRepository.save(any(AlertHistory.class)))
                    .thenReturn(new AlertHistory());

            alertService.sendConnectionAlert(testDatasource, failureResult);

            verify(notificationService).send(eq(testAlertConfig), argThat(message ->
                    message.contains("建议解决方案") &&
                    message.contains("Check if MySQL is running")
            ));
        }
    }
}
