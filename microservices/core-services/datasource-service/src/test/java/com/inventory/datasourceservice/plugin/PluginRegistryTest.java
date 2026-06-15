package com.inventory.datasourceservice.plugin;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginRegistryTest {

    private PluginRegistry pluginRegistry;
    private DataSourcePlugin mockMySQLPlugin;
    private DataSourcePlugin mockPostgreSQLPlugin;

    @BeforeEach
    void setUp() {
        pluginRegistry = new PluginRegistry();
        
        mockMySQLPlugin = mock(DataSourcePlugin.class);
        when(mockMySQLPlugin.getPluginName()).thenReturn("MySQL DataSource Plugin");
        when(mockMySQLPlugin.getPluginVersion()).thenReturn("1.0.0");
        when(mockMySQLPlugin.getSupportedType()).thenReturn(DatasourceConfig.DatasourceType.MYSQL);
        when(mockMySQLPlugin.getSupportedVersions()).thenReturn(List.of("8.0", "5.7"));

        mockPostgreSQLPlugin = mock(DataSourcePlugin.class);
        when(mockPostgreSQLPlugin.getPluginName()).thenReturn("PostgreSQL DataSource Plugin");
        when(mockPostgreSQLPlugin.getPluginVersion()).thenReturn("1.0.0");
        when(mockPostgreSQLPlugin.getSupportedType()).thenReturn(DatasourceConfig.DatasourceType.POSTGRESQL);
        when(mockPostgreSQLPlugin.getSupportedVersions()).thenReturn(List.of("18", "17"));
    }

    @Nested
    @DisplayName("registerPlugin() method tests")
    class RegisterPluginTests {

        @Test
        @DisplayName("should register plugin successfully")
        void shouldRegisterPluginSuccessfully() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);

            Optional<DataSourcePlugin> plugin = pluginRegistry.getPlugin(DatasourceConfig.DatasourceType.MYSQL);
            
            assertTrue(plugin.isPresent());
            assertEquals(mockMySQLPlugin, plugin.get());
        }

        @Test
        @DisplayName("should overwrite existing plugin for same type")
        void shouldOverwriteExistingPluginForSameType() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);
            
            DataSourcePlugin newMySQLPlugin = mock(DataSourcePlugin.class);
            when(newMySQLPlugin.getSupportedType()).thenReturn(DatasourceConfig.DatasourceType.MYSQL);
            when(newMySQLPlugin.getPluginName()).thenReturn("New MySQL Plugin");
            
            pluginRegistry.registerPlugin(newMySQLPlugin);

            Optional<DataSourcePlugin> plugin = pluginRegistry.getPlugin(DatasourceConfig.DatasourceType.MYSQL);
            
            assertTrue(plugin.isPresent());
            assertEquals(newMySQLPlugin, plugin.get());
        }

        @Test
        @DisplayName("should register multiple plugins")
        void shouldRegisterMultiplePlugins() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);
            pluginRegistry.registerPlugin(mockPostgreSQLPlugin);

            List<DataSourcePlugin> plugins = pluginRegistry.getAllPlugins();

            assertEquals(2, plugins.size());
        }
    }

    @Nested
    @DisplayName("getPlugin() method tests")
    class GetPluginTests {

        @Test
        @DisplayName("should return plugin when exists")
        void shouldReturnPluginWhenExists() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);

            Optional<DataSourcePlugin> plugin = pluginRegistry.getPlugin(DatasourceConfig.DatasourceType.MYSQL);

            assertTrue(plugin.isPresent());
            assertEquals(mockMySQLPlugin, plugin.get());
        }

        @Test
        @DisplayName("should return empty when plugin not found")
        void shouldReturnEmptyWhenPluginNotFound() {
            Optional<DataSourcePlugin> plugin = pluginRegistry.getPlugin(DatasourceConfig.DatasourceType.MYSQL);

            assertFalse(plugin.isPresent());
        }
    }

    @Nested
    @DisplayName("getRequiredPlugin() method tests")
    class GetRequiredPluginTests {

        @Test
        @DisplayName("should return plugin when exists")
        void shouldReturnPluginWhenExists() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);

            DataSourcePlugin plugin = pluginRegistry.getRequiredPlugin(DatasourceConfig.DatasourceType.MYSQL);

            assertNotNull(plugin);
            assertEquals(mockMySQLPlugin, plugin);
        }

        @Test
        @DisplayName("should throw exception when plugin not found")
        void shouldThrowExceptionWhenPluginNotFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    pluginRegistry.getRequiredPlugin(DatasourceConfig.DatasourceType.MYSQL)
            );
        }
    }

    @Nested
    @DisplayName("getAllPlugins() method tests")
    class GetAllPluginsTests {

        @Test
        @DisplayName("should return empty list when no plugins registered")
        void shouldReturnEmptyListWhenNoPluginsRegistered() {
            List<DataSourcePlugin> plugins = pluginRegistry.getAllPlugins();

            assertNotNull(plugins);
            assertTrue(plugins.isEmpty());
        }

        @Test
        @DisplayName("should return all registered plugins")
        void shouldReturnAllRegisteredPlugins() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);
            pluginRegistry.registerPlugin(mockPostgreSQLPlugin);

            List<DataSourcePlugin> plugins = pluginRegistry.getAllPlugins();

            assertEquals(2, plugins.size());
        }
    }

    @Nested
    @DisplayName("getSupportedTypes() method tests")
    class GetSupportedTypesTests {

        @Test
        @DisplayName("should return empty list when no plugins registered")
        void shouldReturnEmptyListWhenNoPluginsRegistered() {
            List<DatasourceConfig.DatasourceType> types = pluginRegistry.getSupportedTypes();

            assertNotNull(types);
            assertTrue(types.isEmpty());
        }

        @Test
        @DisplayName("should return all supported types")
        void shouldReturnAllSupportedTypes() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);
            pluginRegistry.registerPlugin(mockPostgreSQLPlugin);

            List<DatasourceConfig.DatasourceType> types = pluginRegistry.getSupportedTypes();

            assertEquals(2, types.size());
            assertTrue(types.contains(DatasourceConfig.DatasourceType.MYSQL));
            assertTrue(types.contains(DatasourceConfig.DatasourceType.POSTGRESQL));
        }
    }

    @Nested
    @DisplayName("isSupported() method tests")
    class IsSupportedTests {

        @Test
        @DisplayName("should return true when type is supported")
        void shouldReturnTrueWhenTypeIsSupported() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);

            assertTrue(pluginRegistry.isSupported(DatasourceConfig.DatasourceType.MYSQL));
        }

        @Test
        @DisplayName("should return false when type is not supported")
        void shouldReturnFalseWhenTypeIsNotSupported() {
            assertFalse(pluginRegistry.isSupported(DatasourceConfig.DatasourceType.MYSQL));
        }
    }

    @Nested
    @DisplayName("getPluginInfo() method tests")
    class GetPluginInfoTests {

        @Test
        @DisplayName("should return plugin info when plugin exists")
        void shouldReturnPluginInfoWhenPluginExists() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);

            Map<String, Object> info = pluginRegistry.getPluginInfo(DatasourceConfig.DatasourceType.MYSQL);

            assertNotNull(info);
            assertEquals("MySQL DataSource Plugin", info.get("name"));
            assertEquals("1.0.0", info.get("version"));
            assertEquals("MYSQL", info.get("type"));
        }

        @Test
        @DisplayName("should return empty map when plugin not found")
        void shouldReturnEmptyMapWhenPluginNotFound() {
            Map<String, Object> info = pluginRegistry.getPluginInfo(DatasourceConfig.DatasourceType.MYSQL);

            assertNotNull(info);
            assertTrue(info.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllPluginInfo() method tests")
    class GetAllPluginInfoTests {

        @Test
        @DisplayName("should return all plugin info")
        void shouldReturnAllPluginInfo() {
            pluginRegistry.registerPlugin(mockMySQLPlugin);
            pluginRegistry.registerPlugin(mockPostgreSQLPlugin);

            List<Map<String, Object>> infoList = pluginRegistry.getAllPluginInfo();

            assertEquals(2, infoList.size());
        }

        @Test
        @DisplayName("should return empty list when no plugins registered")
        void shouldReturnEmptyListWhenNoPluginsRegistered() {
            List<Map<String, Object>> infoList = pluginRegistry.getAllPluginInfo();

            assertNotNull(infoList);
            assertTrue(infoList.isEmpty());
        }
    }
}
