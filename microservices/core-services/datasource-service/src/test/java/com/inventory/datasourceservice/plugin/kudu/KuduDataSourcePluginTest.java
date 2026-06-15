package com.inventory.datasourceservice.plugin.kudu;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KuduDataSourcePlugin Unit Tests")
class KuduDataSourcePluginTest {

    private KuduDataSourcePlugin plugin;

    private DatasourceConfig testConfig;

    @BeforeEach
    void setUp() {
        plugin = new KuduDataSourcePlugin();
        
        testConfig = DatasourceConfig.builder()
                .id(1L)
                .name("test-kudu")
                .type(DatasourceConfig.DatasourceType.KUDU)
                .host("localhost")
                .port(7051)
                .databaseName("test-cluster")
                .build();
    }

    @Nested
    @DisplayName("Plugin metadata tests")
    class PluginMetadataTests {

        @Test
        @DisplayName("should return correct plugin name")
        void shouldReturnCorrectPluginName() {
            assertEquals("Kudu DataSource Plugin", plugin.getPluginName());
        }

        @Test
        @DisplayName("should return correct plugin version")
        void shouldReturnCorrectPluginVersion() {
            assertEquals("1.0.0", plugin.getPluginVersion());
        }

        @Test
        @DisplayName("should return correct supported type")
        void shouldReturnCorrectSupportedType() {
            assertEquals(DatasourceConfig.DatasourceType.KUDU, plugin.getSupportedType());
        }

        @Test
        @DisplayName("should return supported versions")
        void shouldReturnSupportedVersions() {
            List<String> versions = plugin.getSupportedVersions();

            assertNotNull(versions);
            assertTrue(versions.contains("1.17"));
            assertTrue(versions.contains("1.16"));
            assertTrue(versions.contains("1.15"));
            assertTrue(versions.contains("1.10"));
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
            assertEquals(7051, defaults.get("port"));
            assertEquals(30000, defaults.get("operationTimeout"));
            assertEquals(3000, defaults.get("connectionTimeout"));
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
        }

        @Test
        @DisplayName("should have correct field properties for host")
        void shouldHaveCorrectFieldPropertiesForHost() {
            DataSourcePlugin.ConfigField hostField = plugin.getConfigFields().stream()
                    .filter(f -> "host".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(hostField);
            assertEquals("Master地址", hostField.getLabel());
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
            assertEquals("Master端口", portField.getLabel());
            assertEquals("number", portField.getType());
            assertEquals("7051", portField.getDefaultValue());
        }

        @Test
        @DisplayName("should have timeout configuration fields")
        void shouldHaveTimeoutConfigurationFields() {
            List<String> fieldNames = plugin.getConfigFields().stream()
                    .map(DataSourcePlugin.ConfigField::getName)
                    .toList();

            assertTrue(fieldNames.contains("connectionTimeout"));
            assertTrue(fieldNames.contains("operationTimeout"));
        }
    }

    @Nested
    @DisplayName("diagnoseError() method tests")
    class DiagnoseErrorTests {

        @Test
        @DisplayName("should diagnose connection refused error")
        void shouldDiagnoseConnectionRefusedError() {
            List<String> suggestions = plugin.diagnoseError("", "Connection refused");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("Kudu Master服务是否正在运行")));
        }

        @Test
        @DisplayName("should diagnose timeout error")
        void shouldDiagnoseTimeoutError() {
            List<String> suggestions = plugin.diagnoseError("", "Connection timed out");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("超时")));
        }

        @Test
        @DisplayName("should diagnose tablet server error")
        void shouldDiagnoseTabletServerError() {
            List<String> suggestions = plugin.diagnoseError("", "Tablet server not available");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("Tablet")));
        }

        @Test
        @DisplayName("should diagnose network error")
        void shouldDiagnoseNetworkError() {
            List<String> suggestions = plugin.diagnoseError("", "Network is unreachable");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("网络")));
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
        @DisplayName("should return Unknown when connection fails")
        void shouldReturnUnknownWhenConnectionFails() {
            testConfig.setHost("non-existent-host");

            String version = plugin.detectVersion(testConfig);

            assertEquals("Unknown", version);
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
