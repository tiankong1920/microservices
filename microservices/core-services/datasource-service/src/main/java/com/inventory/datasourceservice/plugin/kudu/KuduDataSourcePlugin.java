package com.inventory.datasourceservice.plugin.kudu;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.exception.DatasourceException;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.ListTablesResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KuduDataSourcePlugin implements DataSourcePlugin {

    private static final Pattern VERSION_PATTERN = Pattern.compile("kudu-(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("1.17", "1.16", "1.15", "1.14", "1.13", "1.12", "1.11", "1.10");
    private static final int DEFAULT_PORT = 7051;
    private static final long OPERATION_TIMEOUT_MS = 30000;
    private static final long ADMIN_OPERATION_TIMEOUT_MS = 30000;
    private static final long CONNECTION_TIMEOUT_MS = 3000;

    @Override
    public String getPluginName() {
        return "Kudu DataSource Plugin";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    public DatasourceConfig.DatasourceType getSupportedType() {
        return DatasourceConfig.DatasourceType.KUDU;
    }

    @Override
    public List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    @Override
    public ConnectionTestResultDTO testConnection(DatasourceConfig config) {
        long startTime = System.currentTimeMillis();
        KuduClient client = null;
        
        try {
            client = createKuduClient(config);
            
            ListTablesResponse tablesResponse = client.getTablesList();
            long responseTime = System.currentTimeMillis() - startTime;

            if (tablesResponse != null) {
                return ConnectionTestResultDTO.success(
                        config.getId(),
                        config.getName(),
                        (int) responseTime
                );
            } else {
                return ConnectionTestResultDTO.failure(
                        config.getId(),
                        config.getName(),
                        "CONNECTION_FAILED",
                        "无法获取表列表",
                        diagnoseError("CONNECTION_FAILED", "无法获取表列表")
                );
            }
        } catch (KuduException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Kudu connection test failed", e);
            
            if (responseTime >= CONNECTION_TIMEOUT_MS) {
                return ConnectionTestResultDTO.timeout(config.getId(), config.getName());
            }
            
            return ConnectionTestResultDTO.failure(
                    config.getId(),
                    config.getName(),
                    "KUDU_ERROR",
                    e.getMessage(),
                    diagnoseError("KUDU_ERROR", e.getMessage())
            );
        } finally {
            closeClient(client);
        }
    }

    @Override
    public String detectVersion(DatasourceConfig config) {
        KuduClient client = null;
        try {
            client = createKuduClient(config);
            
            String versionString = client.toString();
            Matcher matcher = VERSION_PATTERN.matcher(versionString);
            if (matcher.find()) {
                return matcher.group(1) + "." + matcher.group(2);
            }
            
            return "Unknown";
        } catch (Exception e) {
            log.error("Failed to detect Kudu version", e);
            return null;
        } finally {
            closeClient(client);
        }
    }

    @Override
    public MetadataInfo discoverMetadata(DatasourceConfig config) {
        KuduClient client = null;
        try {
            client = createKuduClient(config);
            
            List<TableInfo> tables = new ArrayList<>();
            ListTablesResponse tablesResponse = client.getTablesList();
            
            for (String tableName : tablesResponse.getTablesList()) {
                try {
                    KuduTable table = client.openTable(tableName);
                    Schema schema = table.getSchema();
                    
                    List<ColumnInfo> columns = new ArrayList<>();
                    List<String> primaryKeys = new ArrayList<>();
                    int position = 1;
                    
                    for (ColumnSchema columnSchema : schema.getColumns()) {
                        ColumnInfo column = ColumnInfo.builder()
                                .name(columnSchema.getName())
                                .type(kuduTypeToString(columnSchema.getType()))
                                .nullable(columnSchema.isNullable())
                                .defaultValue(columnSchema.getDefaultValue() != null 
                                        ? columnSchema.getDefaultValue().toString() : null)
                                .position(position++)
                                .length(columnSchema.getTypeSize())
                                .build();
                        columns.add(column);
                        
                        if (columnSchema.isKey()) {
                            primaryKeys.add(columnSchema.getName());
                        }
                    }

                    List<IndexInfo> indexes = new ArrayList<>();
                    if (!primaryKeys.isEmpty()) {
                        indexes.add(IndexInfo.builder()
                                .name("PRIMARY")
                                .columns(primaryKeys)
                                .unique(true)
                                .build());
                    }

                    TableInfo tableInfo = TableInfo.builder()
                            .name(tableName)
                            .type("TABLE")
                            .columns(columns)
                            .primaryKeys(primaryKeys)
                            .indexes(indexes)
                            .build();
                    tables.add(tableInfo);
                } catch (KuduException e) {
                    log.warn("Failed to get schema for table: {}", tableName, e);
                }
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put("tablet_server_count", client.listTabletServers().getTabletServersCount());
            properties.put("master_addresses", config.getHost() + ":" + config.getPort());

            return MetadataInfo.builder()
                    .catalog(config.getDatabaseName())
                    .tables(tables)
                    .version(detectVersion(config))
                    .properties(properties)
                    .build();
        } catch (Exception e) {
            log.error("Failed to discover Kudu metadata", e);
            throw DatasourceException.metadataDiscoveryFailed("Kudu", e.getMessage());
        } finally {
            closeClient(client);
        }
    }

    private String kuduTypeToString(Type type) {
        return switch (type) {
            case BOOL -> "BOOLEAN";
            case INT8 -> "TINYINT";
            case INT16 -> "SMALLINT";
            case INT32 -> "INT";
            case INT64 -> "BIGINT";
            case UNIXTIME_MICROS -> "TIMESTAMP";
            case FLOAT -> "FLOAT";
            case DOUBLE -> "DOUBLE";
            case DECIMAL -> "DECIMAL";
            case STRING -> "STRING";
            case BINARY -> "BINARY";
            case VARCHAR -> "VARCHAR";
            default -> type.getName();
        };
    }

    private KuduClient createKuduClient(DatasourceConfig config) {
        String masterAddress = config.getHost() + ":" + 
                (config.getPort() != null ? config.getPort() : DEFAULT_PORT);
        
        KuduClient.KuduClientBuilder builder = new KuduClient.KuduClientBuilder(masterAddress);
        
        builder.defaultOperationTimeoutMs(getExtraConfigValue(config, "operationTimeout", OPERATION_TIMEOUT_MS));
        builder.defaultAdminOperationTimeoutMs(getExtraConfigValue(config, "adminOperationTimeout", ADMIN_OPERATION_TIMEOUT_MS));
        
        if (getExtraConfigValue(config, "workerCount", 0) > 0) {
            builder.workerCount(getExtraConfigValue(config, "workerCount", 0));
        }
        
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <T> T getExtraConfigValue(DatasourceConfig config, String key, T defaultValue) {
        if (config.getExtraConfig() == null || config.getExtraConfig().isEmpty()) {
            return defaultValue;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> extraConfig = mapper.readValue(config.getExtraConfig(), Map.class);
            Object value = extraConfig.get(key);
            if (value == null) {
                return defaultValue;
            }
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } catch (IOException e) {
            log.debug("Failed to parse extra config, using default value for key: {}", key);
            return defaultValue;
        }
    }

    private void closeClient(KuduClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (KuduException e) {
                log.warn("Failed to close Kudu client", e);
            }
        }
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 7051);
        config.put("operationTimeout", 30000);
        config.put("adminOperationTimeout", 30000);
        config.put("connectionTimeout", 3000);
        config.put("workerCount", 4);
        return config;
    }

    @Override
    public List<ConfigField> getConfigFields() {
        return Arrays.asList(
                ConfigField.builder()
                        .name("host")
                        .label("Master地址")
                        .type("text")
                        .required(true)
                        .description("Kudu Master服务器的主机地址")
                        .placeholder("例如: kudu-master 或 192.168.1.100")
                        .build(),
                ConfigField.builder()
                        .name("port")
                        .label("Master端口")
                        .type("number")
                        .defaultValue("7051")
                        .required(true)
                        .description("Kudu Master RPC端口")
                        .minValue(1)
                        .maxValue(65535)
                        .build(),
                ConfigField.builder()
                        .name("operationTimeout")
                        .label("操作超时(ms)")
                        .type("number")
                        .defaultValue("30000")
                        .description("普通操作超时时间（毫秒）")
                        .minValue(100)
                        .maxValue(300000)
                        .build(),
                ConfigField.builder()
                        .name("adminOperationTimeout")
                        .label("管理操作超时(ms)")
                        .type("number")
                        .defaultValue("30000")
                        .description("管理操作超时时间（毫秒）")
                        .minValue(100)
                        .maxValue(300000)
                        .build(),
                ConfigField.builder()
                        .name("connectionTimeout")
                        .label("连接超时(ms)")
                        .type("number")
                        .defaultValue("3000")
                        .description("连接超时时间（毫秒）")
                        .minValue(100)
                        .maxValue(30000)
                        .build(),
                ConfigField.builder()
                        .name("workerCount")
                        .label("工作线程数")
                        .type("number")
                        .defaultValue("4")
                        .description("客户端工作线程数")
                        .minValue(1)
                        .maxValue(32)
                        .build()
        );
    }

    @Override
    public List<String> diagnoseError(String errorCode, String errorMessage) {
        List<String> suggestions = new ArrayList<>();
        
        if (errorMessage.contains("Connection refused") || errorMessage.contains("could not connect")) {
            suggestions.add("请检查Kudu Master服务是否正在运行");
            suggestions.add("请检查主机地址和端口是否正确");
            suggestions.add("请检查防火墙是否允许该端口访问");
            suggestions.add("请确认Kudu Master的RPC端口（默认7051）是否正确");
        } else if (errorMessage.contains("tablet not found") || errorMessage.contains("Tablet server")) {
            suggestions.add("Tablet服务器可能不可用，请检查集群状态");
            suggestions.add("请检查所有Tablet服务器是否正常运行");
            suggestions.add("请检查网络连接是否正常");
        } else if (errorMessage.contains("not authorized") || errorMessage.contains("Access denied")) {
            suggestions.add("请检查用户是否有访问权限");
            suggestions.add("请检查Kudu的安全认证配置");
        } else if (errorMessage.contains("timeout") || errorMessage.contains("timed out")) {
            suggestions.add("连接超时，请增加超时时间配置");
            suggestions.add("请检查网络延迟是否过高");
            suggestions.add("请检查Kudu集群负载是否过高");
        } else if (errorMessage.contains("version") || errorMessage.contains("incompatible")) {
            suggestions.add("Kudu客户端版本与服务器版本不兼容");
            suggestions.add("请检查Kudu版本是否在支持范围内（1.10+）");
        } else {
            suggestions.add("请检查网络连接是否正常");
            suggestions.add("请确认Kudu集群是否正常运行");
            suggestions.add("请查看Kudu Master日志获取详细错误信息");
            suggestions.add("请联系系统管理员");
        }
        
        return suggestions;
    }
}
