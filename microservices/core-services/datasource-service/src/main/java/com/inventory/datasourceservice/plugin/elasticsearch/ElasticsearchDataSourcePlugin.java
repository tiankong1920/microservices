package com.inventory.datasourceservice.plugin.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.exception.DatasourceException;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
@RequiredArgsConstructor
public class ElasticsearchDataSourcePlugin implements DataSourcePlugin {

    private final EncryptionService encryptionService;

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("8.x", "7.x", "6.x");
    private static final int DEFAULT_PORT = 9200;
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 30000;

    @Override
    public String getPluginName() {
        return "Elasticsearch DataSource Plugin";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    public DatasourceConfig.DatasourceType getSupportedType() {
        return DatasourceConfig.DatasourceType.ELASTICSEARCH;
    }

    @Override
    public List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    @Override
    public ConnectionTestResultDTO testConnection(DatasourceConfig config) {
        long startTime = System.currentTimeMillis();
        ElasticsearchClient client = null;
        RestClient restClient = null;
        
        try {
            restClient = createRestClient(config);
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper(new ObjectMapper()));
            client = new ElasticsearchClient(transport);

            HealthResponse health = client.cluster().health();
            long responseTime = System.currentTimeMillis() - startTime;

            if (health.status() != null) {
                return ConnectionTestResultDTO.success(
                        config.getId(),
                        config.getName(),
                        (int) responseTime
                );
            } else {
                return ConnectionTestResultDTO.failure(
                        config.getId(),
                        config.getName(),
                        "CLUSTER_UNAVAILABLE",
                        "无法获取集群健康状态",
                        diagnoseError("CLUSTER_UNAVAILABLE", "无法获取集群健康状态")
                );
            }
        } catch (ElasticsearchException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Elasticsearch connection test failed", e);
            return ConnectionTestResultDTO.failure(
                    config.getId(),
                    config.getName(),
                    "ES_" + e.status(),
                    e.getMessage(),
                    diagnoseError("ES_" + e.status(), e.getMessage())
            );
        } catch (IOException e) {
            log.error("Elasticsearch connection test failed", e);
            if (System.currentTimeMillis() - startTime >= CONNECTION_TIMEOUT) {
                return ConnectionTestResultDTO.timeout(config.getId(), config.getName());
            }
            return ConnectionTestResultDTO.failure(
                    config.getId(),
                    config.getName(),
                    "CONNECTION_ERROR",
                    e.getMessage(),
                    diagnoseError("CONNECTION_ERROR", e.getMessage())
            );
        } finally {
            closeClient(restClient);
        }
    }

    @Override
    public String detectVersion(DatasourceConfig config) {
        RestClient restClient = null;
        try {
            restClient = createRestClient(config);
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper(new ObjectMapper()));
            ElasticsearchClient client = new ElasticsearchClient(transport);

            var info = client.info();
            String version = info.version().number();
            
            Matcher matcher = VERSION_PATTERN.matcher(version);
            if (matcher.find()) {
                return matcher.group(1) + ".x";
            }
            return version;
        } catch (Exception e) {
            log.error("Failed to detect Elasticsearch version", e);
            return null;
        } finally {
            closeClient(restClient);
        }
    }

    @Override
    @SuppressWarnings("java:S1181")
    public MetadataInfo discoverMetadata(DatasourceConfig config) {
        RestClient restClient = null;
        try {
            restClient = createRestClient(config);
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper(new ObjectMapper()));
            ElasticsearchClient client = new ElasticsearchClient(transport);

            List<TableInfo> indices = new ArrayList<>();
            
            var indicesResponse = client.indices().get(i -> i.index("*"));
            
            for (var entry : indicesResponse.result().entrySet()) {
                String indexName = entry.getKey();
                var indexState = entry.getValue();
                
                List<ColumnInfo> columns = new ArrayList<>();
                if (indexState.mappings() != null && indexState.mappings().properties() != null) {
                    int position = 1;
                    for (var propEntry : indexState.mappings().properties().entrySet()) {
                        ColumnInfo column = ColumnInfo.builder()
                                .name(propEntry.getKey())
                                .type(getFieldType(propEntry.getValue()))
                                .nullable(true)
                                .position(position++)
                                .build();
                        columns.add(column);
                    }
                }

                TableInfo tableInfo = TableInfo.builder()
                        .name(indexName)
                        .type("INDEX")
                        .columns(columns)
                        .build();
                indices.add(tableInfo);
            }

            var clusterHealth = client.cluster().health();
            Map<String, Object> properties = new HashMap<>();
            properties.put("cluster_name", clusterHealth.clusterName());
            properties.put("status", clusterHealth.status().jsonValue());
            properties.put("number_of_nodes", clusterHealth.numberOfNodes());
            properties.put("number_of_data_nodes", clusterHealth.numberOfDataNodes());

            return MetadataInfo.builder()
                    .catalog(config.getDatabaseName())
                    .tables(indices)
                    .version(detectVersion(config))
                    .properties(properties)
                    .build();
        } catch (IOException | ElasticsearchException e) {
            log.error("Failed to discover Elasticsearch metadata", e);
            throw DatasourceException.metadataDiscoveryFailed("Elasticsearch", e.getMessage());
        } finally {
            closeClient(restClient);
        }
    }

    private String getFieldType(Object fieldMapping) {
        if (fieldMapping instanceof co.elastic.clients.elasticsearch._types.mapping.Property property) {
            return property._kind().jsonValue();
        }
        return "object";
    }

    private RestClient createRestClient(DatasourceConfig config) {
        String scheme = Boolean.parseBoolean(getExtraConfigValue(config, "useSSL", "false")) 
                ? "https" : "http";
        String host = config.getHost();
        int port = config.getPort() != null ? config.getPort() : DEFAULT_PORT;

        HttpHost httpHost = new HttpHost(host, port, scheme);
        RestClientBuilder builder = RestClient.builder(httpHost);

        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
        );

        if (config.getUsername() != null && config.getPassword() != null) {
            String decryptedPassword = encryptionService.decrypt(config.getPassword());
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), decryptedPassword)
            );
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        return builder.build();
    }

    private String getExtraConfigValue(DatasourceConfig config, String key, String defaultValue) {
        if (config.getExtraConfig() == null || config.getExtraConfig().isEmpty()) {
            return defaultValue;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> extraConfig = mapper.readValue(config.getExtraConfig(), Map.class);
            Object value = extraConfig.get(key);
            return value != null ? value.toString() : defaultValue;
        } catch (IOException e) {
            log.debug("Failed to parse extra config, using default value for key: {}", key);
            return defaultValue;
        }
    }

    private void closeClient(RestClient restClient) {
        if (restClient != null) {
            try {
                restClient.close();
            } catch (IOException e) {
                log.warn("Failed to close Elasticsearch client", e);
            }
        }
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 9200);
        config.put("useSSL", false);
        config.put("verifySSL", true);
        config.put("connectTimeout", 3000);
        config.put("socketTimeout", 30000);
        return config;
    }

    @Override
    public List<ConfigField> getConfigFields() {
        return Arrays.asList(
                ConfigField.builder()
                        .name("host")
                        .label("主机地址")
                        .type("text")
                        .required(true)
                        .description("Elasticsearch服务器的主机地址")
                        .placeholder("例如: localhost 或 192.168.1.100")
                        .build(),
                ConfigField.builder()
                        .name("port")
                        .label("端口")
                        .type("number")
                        .defaultValue("9200")
                        .required(true)
                        .description("Elasticsearch HTTP端口")
                        .minValue(1)
                        .maxValue(65535)
                        .build(),
                ConfigField.builder()
                        .name("username")
                        .label("用户名")
                        .type("text")
                        .description("Elasticsearch用户名（如果启用了安全认证）")
                        .build(),
                ConfigField.builder()
                        .name("password")
                        .label("密码")
                        .type("password")
                        .description("Elasticsearch密码（如果启用了安全认证）")
                        .build(),
                ConfigField.builder()
                        .name("useSSL")
                        .label("使用SSL")
                        .type("boolean")
                        .defaultValue("false")
                        .description("是否使用HTTPS连接")
                        .build(),
                ConfigField.builder()
                        .name("verifySSL")
                        .label("验证SSL证书")
                        .type("boolean")
                        .defaultValue("true")
                        .description("是否验证SSL证书")
                        .build(),
                ConfigField.builder()
                        .name("indexPrefix")
                        .label("索引前缀")
                        .type("text")
                        .description("索引名称前缀（可选）")
                        .build()
        );
    }

    @Override
    public List<String> diagnoseError(String errorCode, String errorMessage) {
        List<String> suggestions = new ArrayList<>();
        
        if (errorMessage.contains("401") || errorMessage.contains("Unauthorized")) {
            suggestions.add("请检查用户名和密码是否正确");
            suggestions.add("请确认Elasticsearch是否启用了安全认证");
            suggestions.add("请检查用户是否有访问权限");
        } else if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
            suggestions.add("用户没有访问权限，请检查角色配置");
            suggestions.add("请检查索引级别的权限设置");
        } else if (errorMessage.contains("Connection refused") || errorMessage.contains("connect timed out")) {
            suggestions.add("请检查Elasticsearch服务是否正在运行");
            suggestions.add("请检查主机地址和端口是否正确");
            suggestions.add("请检查防火墙是否允许该端口访问");
        } else if (errorMessage.contains("SSL") || errorMessage.contains("certificate")) {
            suggestions.add("如果不需要SSL，请设置useSSL=false");
            suggestions.add("如果需要SSL，请设置verifySSL=false跳过证书验证（仅用于测试）");
            suggestions.add("请确保使用正确的HTTPS协议");
        } else if (errorMessage.contains("cluster") && errorMessage.contains("unavailable")) {
            suggestions.add("Elasticsearch集群可能未就绪");
            suggestions.add("请检查集群健康状态");
            suggestions.add("请检查所有节点是否正常运行");
        } else {
            suggestions.add("请检查网络连接是否正常");
            suggestions.add("请确认Elasticsearch版本兼容性");
            suggestions.add("请查看Elasticsearch日志获取详细错误信息");
            suggestions.add("请联系系统管理员");
        }
        
        return suggestions;
    }
}
