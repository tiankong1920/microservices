package com.inventory.datasourceservice.plugin;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PluginRegistry {

    private final Map<DatasourceConfig.DatasourceType, DataSourcePlugin> plugins = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<DataSourcePlugin> pluginList;

    @PostConstruct
    public void init() {
        if (pluginList != null) {
            for (DataSourcePlugin plugin : pluginList) {
                registerPlugin(plugin);
            }
        }
        log.info("Plugin registry initialized with {} plugins", plugins.size());
    }

    public void registerPlugin(DataSourcePlugin plugin) {
        DatasourceConfig.DatasourceType type = plugin.getSupportedType();
        if (plugins.containsKey(type)) {
            log.warn("Overwriting existing plugin for type: {}", type);
        }
        plugins.put(type, plugin);
        log.info("Registered plugin: {} v{} for type: {}",
                plugin.getPluginName(), plugin.getPluginVersion(), type);
    }

    public Optional<DataSourcePlugin> getPlugin(DatasourceConfig.DatasourceType type) {
        return Optional.ofNullable(plugins.get(type));
    }

    public DataSourcePlugin getRequiredPlugin(DatasourceConfig.DatasourceType type) {
        return getPlugin(type)
                .orElseThrow(() -> new IllegalArgumentException("No plugin found for type: " + type));
    }

    public List<DataSourcePlugin> getAllPlugins() {
        return List.copyOf(plugins.values());
    }

    public List<DatasourceConfig.DatasourceType> getSupportedTypes() {
        return List.copyOf(plugins.keySet());
    }

    public boolean isSupported(DatasourceConfig.DatasourceType type) {
        return plugins.containsKey(type);
    }

    public Map<String, Object> getPluginInfo(DatasourceConfig.DatasourceType type) {
        return getPlugin(type).map(plugin -> Map.<String, Object>of(
                "name", plugin.getPluginName(),
                "version", plugin.getPluginVersion(),
                "type", plugin.getSupportedType().name(),
                "supportedVersions", plugin.getSupportedVersions()
        )).orElse(Map.of());
    }

    public List<Map<String, Object>> getAllPluginInfo() {
        return plugins.values().stream()
                .map(plugin -> Map.<String, Object>of(
                        "name", plugin.getPluginName(),
                        "version", plugin.getPluginVersion(),
                        "type", plugin.getSupportedType().name(),
                        "supportedVersions", plugin.getSupportedVersions()
                ))
                .collect(Collectors.toList());
    }
}
