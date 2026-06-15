package com.inventory.datasourceservice.plugin.postgresql;

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
@DisplayName("PostgreSQLDataSourcePlugin Unit Tests")
class PostgreSQLDataSourcePluginTest {

    private PostgreSQLDataSourcePlugin plugin;

    private DatasourceConfig testConfig;

    @Mock
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        plugin = new PostgreSQLDataSourcePlugin(encryptionService);
        
        testConfig = DatasourceConfig.builder()
                .id(1L)
                .name("test-postgres")
                .type(DatasourceConfig.DatasourceType.POSTGRESQL)
                .host("localhost")
                .port(5432)
                .databaseName("testdb")
                .username("postgres")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Plugin metadata tests")
    class PluginMetadataTests {

        @Test
        @DisplayName("should return correct plugin name")
        void shouldReturnCorrectPluginName() {
            assertEquals("PostgreSQL DataSource Plugin", plugin.getPluginName());
        }

        @Test
        @DisplayName("should return correct plugin version")
        void shouldReturnCorrectPluginVersion() {
            assertEquals("1.0.0", plugin.getPluginVersion());
        }

        @Test
        @DisplayName("should return correct supported type")
        void shouldReturnCorrectSupportedType() {
            assertEquals(DatasourceConfig.DatasourceType.POSTGRESQL, plugin.getSupportedType());
        }

        @Test
        @DisplayName("should return supported versions")
        void shouldReturnSupportedVersions() {
            List<String> versions = plugin.getSupportedVersions();

            assertNotNull(versions);
            assertTrue(versions.contains("18"));
            assertTrue(versions.contains("17"));
            assertTrue(versions.contains("16"));
        }
    }

    @Nested
    @DisplayName("buildJdbcUrl() method tests")
    class BuildJdbcUrlTests {

        @Test
        @DisplayName("should build correct JDBC URL with database name")
        void shouldBuildCorrectJdbcUrlWithDatabaseName() {
            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.startsWith("jdbc:postgresql://localhost:5432/testdb"));
            assertTrue(jdbcUrl.contains("sslmode=disable"));
        }

        @Test
        @DisplayName("should build JDBC URL with empty database when null")
        void shouldBuildJdbcUrlWithEmptyDatabaseWhenNull() {
            testConfig.setDatabaseName(null);

            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.contains("jdbc:postgresql://localhost:5432/"));
        }

        @Test
        @DisplayName("should include custom host and port")
        void shouldIncludeCustomHostAndPort() {
            testConfig.setHost("192.168.1.100");
            testConfig.setPort(5433);

            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.contains("192.168.1.100:5433"));
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
            assertEquals(5432, defaults.get("port"));
            assertEquals("public", defaults.get("schema"));
            assertEquals("disable", defaults.get("sslMode"));
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
            assertTrue(fieldNames.contains("databaseName"));
            assertTrue(fieldNames.contains("username"));
            assertTrue(fieldNames.contains("password"));
            assertTrue(fieldNames.contains("schema"));
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
            assertEquals("5432", portField.getDefaultValue());
        }

        @Test
        @DisplayName("should have schema field with correct default")
        void shouldHaveSchemaFieldWithCorrectDefault() {
            DataSourcePlugin.ConfigField schemaField = plugin.getConfigFields().stream()
                    .filter(f -> "schema".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(schemaField);
            assertEquals("public", schemaField.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("diagnoseError() method tests")
    class DiagnoseErrorTests {

        @Test
        @DisplayName("should diagnose authentication failed error")
        void shouldDiagnoseAuthenticationFailedError() {
            List<String> suggestions = plugin.diagnoseError("28P01", "password authentication failed for user \"postgres\"");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("用户名和密码")));
        }

        @Test
        @DisplayName("should diagnose database does not exist error")
        void shouldDiagnoseDatabaseDoesNotExistError() {
            List<String> suggestions = plugin.diagnoseError("3D000", "database \"testdb\" does not exist");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("数据库") && s.contains("不存在")));
        }

        @Test
        @DisplayName("should diagnose connection refused error")
        void shouldDiagnoseConnectionRefusedError() {
            List<String> suggestions = plugin.diagnoseError("", "Connection refused");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("PostgreSQL服务是否正在运行")));
        }

        @Test
        @DisplayName("should diagnose SSL error")
        void shouldDiagnoseSSLError() {
            List<String> suggestions = plugin.diagnoseError("", "SSL connection");

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
    @DisplayName("getDriverClassName() method tests")
    class GetDriverClassNameTests {

        @Test
        @DisplayName("should return correct driver class name")
        void shouldReturnCorrectDriverClassName() {
            assertEquals("org.postgresql.Driver", plugin.getDriverClassName());
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
