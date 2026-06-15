package com.inventory.datasourceservice.plugin.elasticsearch;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.security.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElasticsearchDataSourcePlugin Unit Tests")
class ElasticsearchDataSourcePluginTest {

    private ElasticsearchDataSourcePlugin plugin;

    private DatasourceConfig testConfig;

    @Mock
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        plugin = new ElasticsearchDataSourcePlugin(encryptionService);
        
        testConfig = DatasourceConfig.builder()
                .id(1L)
                .name("test-es")
                .type(DatasourceConfig.DatasourceType.ELASTICSEARCH)
                .host("localhost")
                .port(9200)
                .databaseName("test-cluster")
                .username("elastic")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Plugin metadata tests")
    class PluginMetadataTests {

        @Test
        @DisplayName("should return correct plugin name")
        void shouldReturnCorrectPluginName() {
            assertEquals("Elasticsearch DataSource Plugin", plugin.getPluginName());
        }

        @Test
        @DisplayName("should return correct plugin version")
        void shouldReturnCorrectPluginVersion() {
            assertEquals("1.0.0", plugin.getPluginVersion());
        }

        @Test
        @DisplayName("should return correct supported type")
        void shouldReturnCorrectSupportedType() {
            assertEquals(DatasourceConfig.DatasourceType.ELASTICSEARCH, plugin.getSupportedType());
        }

        @Test
        @DisplayName("should return supported versions")
        void shouldReturnSupportedVersions() {
            List<String> versions = plugin.getSupportedVersions();

            assertNotNull(versions);
            assertTrue(versions.contains("8.x"));
            assertTrue(versions.contains("7.x"));
            assertTrue(versions.contains("6.x"));
        }
    }

    @Nested
    @DisplayName("getDefaultConfig() method tests")
    class GetDefaultConfigTests {

        @Test
        @DisplayName("should return default configuration values")
        void shouldReturnDefaultConfigurationValues() {
            Map<String, Object> defaults = plugin.getDefaultConfig();

            assertNotNull(defaults);
            assertEquals(9200, defaults.get("port"));
            assertEquals(false, defaults.get("useSSL"));
            assertEquals(true, defaults.get("verifySSL"));
        }
    }

    @Nested
    @DisplayName("getConfigFields() method tests")
    class GetConfigFieldsTests {

        @Test
        @DisplayName("should return all required config fields")
        void shouldReturnAllRequiredConfigFields() {
            List<DataSourcePlugin.ConfigField> fields = plugin.getConfigFields();

            assertNotNull(fields);
            assertFalse(fields.isEmpty());

            List<String> fieldNames = fields.stream()
                    .map(DataSourcePlugin.ConfigField::getName)
                    .toList();

            assertTrue(fieldNames.contains("host"));
            assertTrue(fieldNames.contains("port"));
            assertTrue(fieldNames.contains("username"));
            assertTrue(fieldNames.contains("password"));
            assertTrue(fieldNames.contains("useSSL"));
        }

        @Test
        @DisplayName("should have correct field properties for host")
        void shouldHaveCorrectFieldPropertiesForHost() {
            DataSourcePlugin.ConfigField hostField = plugin.getConfigFields().stream()
                    .filter(f -> "host".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(hostField);
            assertEquals("主机地址", hostField.getLabel());
            assertEquals("text", hostField.getType());
            assertTrue(hostField.getRequired());
        }

        @Test
        @DisplayName("should have correct field properties for port")
        void shouldHaveCorrectFieldPropertiesForPort() {
            DataSourcePlugin.ConfigField portField = plugin.getConfigFields().stream()
                    .filter(f -> "port".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(portField);
            assertEquals("端口", portField.getLabel());
            assertEquals("number", portField.getType());
            assertEquals("9200", portField.getDefaultValue());
        }

        @Test
        @DisplayName("should have useSSL field with correct type")
        void shouldHaveUseSSLFieldWithCorrectType() {
            DataSourcePlugin.ConfigField useSSLField = plugin.getConfigFields().stream()
                    .filter(f -> "useSSL".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(useSSLField);
            assertEquals("boolean", useSSLField.getType());
            assertEquals("false", useSSLField.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("diagnoseError() method tests")
    class DiagnoseErrorTests {

        @Test
        @DisplayName("should diagnose authentication error")
        void shouldDiagnoseAuthenticationError() {
            List<String> suggestions = plugin.diagnoseError("401", "Unauthorized");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("用户名和密码")));
        }

        @Test
        @DisplayName("should diagnose connection refused error")
        void shouldDiagnoseConnectionRefusedError() {
            List<String> suggestions = plugin.diagnoseError("", "Connection refused");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("Elasticsearch服务是否正在运行")));
        }

        @Test
        @DisplayName("should diagnose forbidden error")
        void shouldDiagnoseForbiddenError() {
            List<String> suggestions = plugin.diagnoseError("403", "Forbidden");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("权限")));
        }

        @Test
        @DisplayName("should diagnose SSL error")
        void shouldDiagnoseSSLError() {
            List<String> suggestions = plugin.diagnoseError("", "SSL certificate");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("SSL")));
        }

        @Test
        @DisplayName("should return generic suggestions for unknown error")
        void shouldReturnGenericSuggestionsForUnknownError() {
            List<String> suggestions = plugin.diagnoseError("9999", "Unknown error message");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
        }
    }

    @Nested
    @DisplayName("testConnection() method tests")
    class TestConnectionTests {

        @Test
        @DisplayName("should return failure result when connection fails")
        void shouldReturnFailureResultWhenConnectionFails() {
            testConfig.setHost("non-existent-host");
            testConfig.setPort(9999);

            ConnectionTestResultDTO result = plugin.testConnection(testConfig);

            assertNotNull(result);
            assertEquals(testConfig.getId(), result.getDatasourceId());
            assertNotNull(result.getErrorCode());
            assertNotNull(result.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("detectVersion() method tests")
    class DetectVersionTests {

        @Test
        @DisplayName("should return null when connection fails")
        void shouldReturnNullWhenConnectionFails() {
            testConfig.setHost("non-existent-host");

            String version = plugin.detectVersion(testConfig);

            assertNull(version);
        }
    }

    @Nested
    @DisplayName("discoverMetadata() method tests")
    class DiscoverMetadataTests {

        @Test
        @DisplayName("should throw exception when connection fails")
        void shouldThrowExceptionWhenConnectionFails() {
            testConfig.setHost("non-existent-host");

            assertThrows(RuntimeException.class, () -> 
                plugin.discoverMetadata(testConfig)
            );
        }
    }
}
