package com.inventory.datasourceservice.plugin.mysql;

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
@DisplayName("MySQLDataSourcePlugin Unit Tests")
class MySQLDataSourcePluginTest {

    private MySQLDataSourcePlugin plugin;

    private DatasourceConfig testConfig;

    @Mock
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        plugin = new MySQLDataSourcePlugin(encryptionService);
        
        testConfig = DatasourceConfig.builder()
                .id(1L)
                .name("test-mysql")
                .type(DatasourceConfig.DatasourceType.MYSQL)
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Plugin metadata tests")
    class PluginMetadataTests {

        @Test
        @DisplayName("should return correct plugin name")
        void shouldReturnCorrectPluginName() {
            assertEquals("MySQL DataSource Plugin", plugin.getPluginName());
        }

        @Test
        @DisplayName("should return correct plugin version")
        void shouldReturnCorrectPluginVersion() {
            assertEquals("1.0.0", plugin.getPluginVersion());
        }

        @Test
        @DisplayName("should return correct supported type")
        void shouldReturnCorrectSupportedType() {
            assertEquals(DatasourceConfig.DatasourceType.MYSQL, plugin.getSupportedType());
        }

        @Test
        @DisplayName("should return supported versions")
        void shouldReturnSupportedVersions() {
            List<String> versions = plugin.getSupportedVersions();

            assertNotNull(versions);
            assertTrue(versions.contains("5.7"));
            assertTrue(versions.contains("8.0"));
        }
    }

    @Nested
    @DisplayName("buildJdbcUrl() method tests")
    class BuildJdbcUrlTests {

        @Test
        @DisplayName("should build correct JDBC URL with database name")
        void shouldBuildCorrectJdbcUrlWithDatabaseName() {
            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.startsWith("jdbc:mysql://localhost:3306/testdb"));
            assertTrue(jdbcUrl.contains("useUnicode=true"));
            assertTrue(jdbcUrl.contains("characterEncoding=utf-8"));
            assertTrue(jdbcUrl.contains("serverTimezone=Asia/Shanghai"));
        }

        @Test
        @DisplayName("should build JDBC URL with empty database when null")
        void shouldBuildJdbcUrlWithEmptyDatabaseWhenNull() {
            testConfig.setDatabaseName(null);

            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.contains("jdbc:mysql://localhost:3306/?"));
        }

        @Test
        @DisplayName("should include custom host and port")
        void shouldIncludeCustomHostAndPort() {
            testConfig.setHost("192.168.1.100");
            testConfig.setPort(3307);

            String jdbcUrl = plugin.buildJdbcUrl(testConfig);

            assertTrue(jdbcUrl.contains("192.168.1.100:3307"));
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
            assertEquals(3306, defaults.get("port"));
            assertEquals("utf8mb4", defaults.get("charset"));
            assertEquals("utf8mb4_unicode_ci", defaults.get("collation"));
            assertEquals("Asia/Shanghai", defaults.get("timezone"));
            assertEquals(false, defaults.get("useSSL"));
            assertEquals(true, defaults.get("allowPublicKeyRetrieval"));
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
            assertEquals("3306", portField.getDefaultValue());
            assertEquals(1, portField.getMinValue());
            assertEquals(65535, portField.getMaxValue());
        }

        @Test
        @DisplayName("should have password field with correct type")
        void shouldHavePasswordFieldWithCorrectType() {
            DataSourcePlugin.ConfigField passwordField = plugin.getConfigFields().stream()
                    .filter(f -> "password".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(passwordField);
            assertEquals("password", passwordField.getType());
            assertTrue(passwordField.getRequired());
        }

        @Test
        @DisplayName("should have select options for charset field")
        void shouldHaveSelectOptionsForCharsetField() {
            DataSourcePlugin.ConfigField charsetField = plugin.getConfigFields().stream()
                    .filter(f -> "charset".equals(f.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(charsetField);
            assertEquals("select", charsetField.getType());
            assertNotNull(charsetField.getOptions());
            assertTrue(charsetField.getOptions().contains("utf8mb4"));
            assertTrue(charsetField.getOptions().contains("utf8"));
        }
    }

    @Nested
    @DisplayName("diagnoseError() method tests")
    class DiagnoseErrorTests {

        @Test
        @DisplayName("should diagnose access denied error")
        void shouldDiagnoseAccessDeniedError() {
            List<String> suggestions = plugin.diagnoseError("1045", "Access denied for user 'root'@'localhost'");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("用户名和密码")));
        }

        @Test
        @DisplayName("should diagnose unknown database error")
        void shouldDiagnoseUnknownDatabaseError() {
            List<String> suggestions = plugin.diagnoseError("1049", "Unknown database 'testdb'");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("数据库名称不存在")));
        }

        @Test
        @DisplayName("should diagnose connection refused error")
        void shouldDiagnoseConnectionRefusedError() {
            List<String> suggestions = plugin.diagnoseError("2003", "Connection refused");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("MySQL服务是否正在运行")));
        }

        @Test
        @DisplayName("should diagnose public key retrieval error")
        void shouldDiagnosePublicKeyRetrievalError() {
            List<String> suggestions = plugin.diagnoseError("", "Public Key Retrieval is not allowed");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("allowPublicKeyRetrieval")));
        }

        @Test
        @DisplayName("should diagnose SSL connection error")
        void shouldDiagnoseSSLConnectionError() {
            List<String> suggestions = plugin.diagnoseError("", "SSL connection error");

            assertNotNull(suggestions);
            assertFalse(suggestions.isEmpty());
            assertTrue(suggestions.stream().anyMatch(s -> s.contains("useSSL")));
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
            assertEquals("com.mysql.cj.jdbc.Driver", plugin.getDriverClassName());
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
