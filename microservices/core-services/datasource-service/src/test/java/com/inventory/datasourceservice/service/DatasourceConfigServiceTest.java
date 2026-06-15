package com.inventory.datasourceservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.dto.DatasourceConfigDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.AuditLog;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.plugin.PluginRegistry;
import com.inventory.datasourceservice.repository.IAuditLogRepository;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.security.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatasourceConfigService Unit Tests")
class DatasourceConfigServiceTest {

    @Mock
    private IDatasourceConfigRepository datasourceConfigRepository;

    @Mock
    private IConnectionStatusRepository connectionStatusRepository;

    @Mock
    private IConnectionTestLogRepository connectionTestLogRepository;

    @Mock
    private IAuditLogRepository auditLogRepository;

    @Mock
    private PluginRegistry pluginRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private UserContext userContext;

    @Mock
    private EncryptionService encryptionService;

    private DatasourceConfigService datasourceConfigService;

    private static final String TEST_TENANT_ID = "tenant-001";
    private static final String TEST_USER_ID = "user-001";
    private static final String TEST_USERNAME = "testuser";

    private DatasourceConfig testConfig;
    private DatasourceConfigDTO testDTO;

    @BeforeEach
    void setUp() {
        lenient().when(tenantContext.getCurrentTenant()).thenReturn(TEST_TENANT_ID);
        lenient().when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);
        lenient().when(userContext.getCurrentUsername()).thenReturn(TEST_USERNAME);
        lenient().when(userContext.getClientIp()).thenReturn("127.0.0.1");
        lenient().when(userContext.getUserAgent()).thenReturn("TestAgent");

        testConfig = DatasourceConfig.builder()
                .id(1L)
                .tenantId(TEST_TENANT_ID)
                .name("test-mysql")
                .type(DatasourceConfig.DatasourceType.MYSQL)
                .version("8.0")
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("password123")
                .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                .createdBy(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .build();

        testDTO = DatasourceConfigDTO.builder()
                .name("test-mysql")
                .type(DatasourceConfig.DatasourceType.MYSQL)
                .version("8.0")
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("password123")
                .build();

        datasourceConfigService = new DatasourceConfigService(
                datasourceConfigRepository,
                connectionStatusRepository,
                connectionTestLogRepository,
                auditLogRepository,
                pluginRegistry,
                objectMapper,
                tenantContext,
                userContext,
                encryptionService
        );
    }

    @Nested
    @DisplayName("createDatasource() method tests")
    class CreateDatasourceTests {

        @Test
        @DisplayName("should create datasource successfully")
        void shouldCreateDatasourceSuccessfully() {
            when(datasourceConfigRepository.existsByNameAndTenantId(anyString(), anyString()))
                    .thenReturn(false);
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(testConfig);
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenReturn(new AuditLog());

            DatasourceConfigDTO result = datasourceConfigService.createDatasource(testDTO);

            assertNotNull(result);
            assertEquals(testDTO.getName(), result.getName());
            assertEquals(testDTO.getType(), result.getType());
            verify(datasourceConfigRepository).save(any(DatasourceConfig.class));
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("should throw exception when datasource name already exists")
        void shouldThrowExceptionWhenNameExists() {
            when(datasourceConfigRepository.existsByNameAndTenantId(anyString(), anyString()))
                    .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> datasourceConfigService.createDatasource(testDTO)
            );

            assertTrue(exception.getMessage().contains("数据源名称已存在"));
            verify(datasourceConfigRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set default status to ACTIVE")
        void shouldSetDefaultStatusToActive() {
            when(datasourceConfigRepository.existsByNameAndTenantId(anyString(), anyString()))
                    .thenReturn(false);
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenAnswer(invocation -> {
                        DatasourceConfig saved = invocation.getArgument(0);
                        saved.setId(1L);
                        return saved;
                    });

            datasourceConfigService.createDatasource(testDTO);

            verify(datasourceConfigRepository).save(argThat(config ->
                    config.getStatus() == DatasourceConfig.DatasourceStatus.ACTIVE
            ));
        }

        @Test
        @DisplayName("should set tenantId from context")
        void shouldSetTenantIdFromContext() {
            when(datasourceConfigRepository.existsByNameAndTenantId(anyString(), anyString()))
                    .thenReturn(false);
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(testConfig);

            datasourceConfigService.createDatasource(testDTO);

            verify(datasourceConfigRepository).save(argThat(config ->
                    TEST_TENANT_ID.equals(config.getTenantId())
            ));
        }
    }

    @Nested
    @DisplayName("updateDatasource() method tests")
    class UpdateDatasourceTests {

        @Test
        @DisplayName("should update datasource successfully")
        void shouldUpdateDatasourceSuccessfully() {
            DatasourceConfig existingConfig = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TEST_TENANT_ID)
                    .name("old-name")
                    .type(DatasourceConfig.DatasourceType.MYSQL)
                    .host("old-host")
                    .port(3306)
                    .build();

            when(datasourceConfigRepository.findByIdAndTenantId(1L, TEST_TENANT_ID))
                    .thenReturn(Optional.of(existingConfig));
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(testConfig);

            DatasourceConfigDTO updateDTO = DatasourceConfigDTO.builder()
                    .name("test-mysql")
                    .host("localhost")
                    .port(3306)
                    .databaseName("testdb")
                    .username("root")
                    .build();

            DatasourceConfigDTO result = datasourceConfigService.updateDatasource(1L, updateDTO);

            assertNotNull(result);
            verify(datasourceConfigRepository).save(any(DatasourceConfig.class));
        }

        @Test
        @DisplayName("should throw exception when datasource not found")
        void shouldThrowExceptionWhenNotFound() {
            when(datasourceConfigRepository.findByIdAndTenantId(anyLong(), anyString()))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> datasourceConfigService.updateDatasource(999L, testDTO)
            );

            assertTrue(exception.getMessage().contains("数据源不存在"));
        }

        @Test
        @DisplayName("should not update password when null")
        void shouldNotUpdatePasswordWhenNull() {
            DatasourceConfig existingConfig = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TEST_TENANT_ID)
                    .password("oldPassword")
                    .build();

            when(datasourceConfigRepository.findByIdAndTenantId(1L, TEST_TENANT_ID))
                    .thenReturn(Optional.of(existingConfig));
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(existingConfig);

            DatasourceConfigDTO updateDTO = DatasourceConfigDTO.builder()
                    .name("test")
                    .password(null)
                    .build();

            datasourceConfigService.updateDatasource(1L, updateDTO);

            verify(datasourceConfigRepository).save(argThat(config ->
                    "oldPassword".equals(config.getPassword())
            ));
        }

        @Test
        @DisplayName("should not update password when empty")
        void shouldNotUpdatePasswordWhenEmpty() {
            DatasourceConfig existingConfig = DatasourceConfig.builder()
                    .id(1L)
                    .tenantId(TEST_TENANT_ID)
                    .password("oldPassword")
                    .build();

            when(datasourceConfigRepository.findByIdAndTenantId(1L, TEST_TENANT_ID))
                    .thenReturn(Optional.of(existingConfig));
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(existingConfig);

            DatasourceConfigDTO updateDTO = DatasourceConfigDTO.builder()
                    .name("test")
                    .password("")
                    .build();

            datasourceConfigService.updateDatasource(1L, updateDTO);

            verify(datasourceConfigRepository).save(argThat(config ->
                    "oldPassword".equals(config.getPassword())
            ));
        }
    }

    @Nested
    @DisplayName("deleteDatasource() method tests")
    class DeleteDatasourceTests {

        @Test
        @DisplayName("should soft delete datasource successfully")
        void shouldSoftDeleteDatasourceSuccessfully() {
            when(datasourceConfigRepository.findByIdAndTenantId(1L, TEST_TENANT_ID))
                    .thenReturn(Optional.of(testConfig));
            when(datasourceConfigRepository.save(any(DatasourceConfig.class)))
                    .thenReturn(testConfig);
            doNothing().when(connectionStatusRepository).deleteByDatasourceId(1L);

            datasourceConfigService.deleteDatasource(1L);

            verify(datasourceConfigRepository).save(argThat(config ->
                    config.getStatus() == DatasourceConfig.DatasourceStatus.DELETED
            ));
            verify(connectionStatusRepository).deleteByDatasourceId(1L);
        }

        @Test
        @DisplayName("should throw exception when deleting non-existent datasource")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            when(datasourceConfigRepository.findByIdAndTenantId(anyLong(), anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> datasourceConfigService.deleteDatasource(999L));

            verify(datasourceConfigRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getDatasource() method tests")
    class GetDatasourceTests {

        @Test
        @DisplayName("should return datasource when found")
        void shouldReturnDatasourceWhenFound() {
            when(datasourceConfigRepository.findByIdAndTenantId(1L, TEST_TENANT_ID))
                    .thenReturn(Optional.of(testConfig));

            DatasourceConfigDTO result = datasourceConfigService.getDatasource(1L);

            assertNotNull(result);
            assertEquals(testConfig.getName(), result.getName());
        }

        @Test
        @DisplayName("should throw exception when datasource not found")
        void shouldThrowExceptionWhenNotFound() {
            when(datasourceConfigRepository.findByIdAndTenantId(anyLong(), anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> datasourceConfigService.getDatasource(999L));
        }
    }

    @Nested
    @DisplayName("listDatasources() method tests")
    class ListDatasourcesTests {

        @Test
        @DisplayName("should return paginated list of datasources")
        void shouldReturnPaginatedList() {
            List<DatasourceConfig> configs = Arrays.asList(testConfig);
            Page<DatasourceConfig> page = new PageImpl<>(configs);
            
            when(datasourceConfigRepository.findByTenantId(eq(TEST_TENANT_ID), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<DatasourceConfigDTO> result = 
                    datasourceConfigService.listDatasources(0, 10, "createdAt", "desc");

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("should return empty list when no datasources")
        void shouldReturnEmptyListWhenNoDatasources() {
            Page<DatasourceConfig> emptyPage = new PageImpl<>(List.of());
            
            when(datasourceConfigRepository.findByTenantId(eq(TEST_TENANT_ID), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<DatasourceConfigDTO> result = 
                    datasourceConfigService.listDatasources(0, 10, "createdAt", "desc");

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("listByType() method tests")
    class ListByTypeTests {

        @Test
        @DisplayName("should return datasources filtered by type")
        void shouldReturnDatasourcesFilteredByType() {
            List<DatasourceConfig> configs = Arrays.asList(testConfig);
            
            when(datasourceConfigRepository.findByTenantIdAndType(TEST_TENANT_ID, 
                    DatasourceConfig.DatasourceType.MYSQL))
                    .thenReturn(configs);

            List<DatasourceConfigDTO> result = 
                    datasourceConfigService.listByType(DatasourceConfig.DatasourceType.MYSQL);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(DatasourceConfig.DatasourceType.MYSQL, result.get(0).getType());
        }

        @Test
        @DisplayName("should return empty list when no datasources of type")
        void shouldReturnEmptyListWhenNoDatasourcesOfType() {
            when(datasourceConfigRepository.findByTenantIdAndType(TEST_TENANT_ID, 
                    DatasourceConfig.DatasourceType.ELASTICSEARCH))
                    .thenReturn(List.of());

            List<DatasourceConfigDTO> result = 
                    datasourceConfigService.listByType(DatasourceConfig.DatasourceType.ELASTICSEARCH);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getConfigSchema() method tests")
    class GetConfigSchemaTests {

        @Test
        @DisplayName("should return config schema for valid type")
        void shouldReturnConfigSchemaForValidType() {
            DataSourcePlugin mockPlugin = mock(DataSourcePlugin.class);
            when(mockPlugin.getConfigFields()).thenReturn(List.of());
            when(mockPlugin.getDefaultConfig()).thenReturn(java.util.Map.of());
            when(pluginRegistry.getPlugin(DatasourceConfig.DatasourceType.MYSQL))
                    .thenReturn(Optional.of(mockPlugin));

            DataSourcePlugin.ConfigSchema schema = 
                    datasourceConfigService.getConfigSchema(DatasourceConfig.DatasourceType.MYSQL);

            assertNotNull(schema);
            assertEquals(DatasourceConfig.DatasourceType.MYSQL, schema.getType());
        }

        @Test
        @DisplayName("should throw exception for unsupported type")
        void shouldThrowExceptionForUnsupportedType() {
            when(pluginRegistry.getPlugin(any()))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> datasourceConfigService.getConfigSchema(DatasourceConfig.DatasourceType.MYSQL));
        }
    }
}
