package com.inventory.datasourceservice.plugin.postgresql;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.exception.DatasourceException;
import com.inventory.datasourceservice.plugin.AbstractDataSourcePlugin;
import com.inventory.datasourceservice.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSQLDataSourcePlugin extends AbstractDataSourcePlugin {

    private final EncryptionService encryptionService;

    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String JDBC_URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?sslmode=disable";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?");
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("18", "17", "16", "15", "14", "13", "12");

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            log.warn("PostgreSQL driver not found in classpath");
        }
    }

    @Override
    public String getPluginName() {
        return "PostgreSQL DataSource Plugin";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    protected String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return encryptedPassword;
        }
        return encryptionService.decrypt(encryptedPassword);
    }

    @Override
    public DatasourceConfig.DatasourceType getSupportedType() {
        return DatasourceConfig.DatasourceType.POSTGRESQL;
    }

    @Override
    public List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    @Override
    protected String buildJdbcUrl(DatasourceConfig config) {
        String database = config.getDatabaseName() != null ? config.getDatabaseName() : "postgres";
        return String.format(JDBC_URL_TEMPLATE, config.getHost(), config.getPort(), database);
    }

    @Override
    protected String getDriverClassName() {
        return DRIVER_CLASS;
    }

    @Override
    public String detectVersion(DatasourceConfig config) {
        try (Connection conn = createConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version()")) {
            if (rs.next()) {
                String versionString = rs.getString(1);
                Matcher matcher = VERSION_PATTERN.matcher(versionString);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                return versionString;
            }
        } catch (Exception e) {
            log.error("Failed to detect PostgreSQL version", e);
        }
        return null;
    }

    @Override
    public MetadataInfo discoverMetadata(DatasourceConfig config) {
        try (Connection conn = createConnection(config)) {
            String catalog = config.getDatabaseName();
            DatabaseMetaData metaData = conn.getMetaData();
            
            List<TableInfo> tables = new ArrayList<>();
            String[] tableTypes = {"TABLE", "VIEW", "MATERIALIZED VIEW"};
            try (ResultSet rs = metaData.getTables(catalog, "public", "%", tableTypes)) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");
                    String remarks = rs.getString("REMARKS");
                    
                    TableInfo tableInfo = TableInfo.builder()
                            .name(tableName)
                            .schema(rs.getString("TABLE_SCHEM"))
                            .type(tableType)
                            .comment(remarks)
                            .columns(discoverColumns(metaData, catalog, tableName))
                            .primaryKeys(discoverPrimaryKeys(metaData, catalog, tableName))
                            .indexes(discoverIndexes(metaData, catalog, tableName))
                            .build();
                    tables.add(tableInfo);
                }
            }

            return MetadataInfo.builder()
                    .catalog(catalog)
                    .tables(tables)
                    .version(detectVersion(config))
                    .properties(discoverServerProperties(conn))
                    .functions(discoverFunctions(metaData, catalog))
                    .build();
        } catch (Exception e) {
            log.error("Failed to discover PostgreSQL metadata", e);
            throw DatasourceException.metadataDiscoveryFailed("PostgreSQL", e.getMessage());
        }
    }

    private List<ColumnInfo> discoverColumns(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(catalog, "public", tableName, "%")) {
            int position = 1;
            while (rs.next()) {
                ColumnInfo column = ColumnInfo.builder()
                        .name(rs.getString("COLUMN_NAME"))
                        .type(rs.getString("TYPE_NAME"))
                        .nullable("YES".equals(rs.getString("IS_NULLABLE")))
                        .defaultValue(rs.getString("COLUMN_DEF"))
                        .comment(rs.getString("REMARKS"))
                        .position(position++)
                        .length(rs.getInt("COLUMN_SIZE"))
                        .build();
                columns.add(column);
            }
        }
        return columns;
    }

    private List<String> discoverPrimaryKeys(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        try (ResultSet rs = metaData.getPrimaryKeys(catalog, "public", tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private List<IndexInfo> discoverIndexes(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        Map<String, List<String>> indexColumns = new LinkedHashMap<>();
        Map<String, Boolean> indexUnique = new LinkedHashMap<>();
        
        try (ResultSet rs = metaData.getIndexInfo(catalog, "public", tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName == null) continue;
                
                indexColumns.computeIfAbsent(indexName, k -> new ArrayList<>())
                        .add(rs.getString("COLUMN_NAME"));
                indexUnique.putIfAbsent(indexName, !rs.getBoolean("NON_UNIQUE"));
            }
        }
        
        return indexColumns.entrySet().stream()
                .map(entry -> IndexInfo.builder()
                        .name(entry.getKey())
                        .columns(entry.getValue())
                        .unique(indexUnique.get(entry.getKey()))
                        .build())
                .toList();
    }

    private List<FunctionInfo> discoverFunctions(DatabaseMetaData metaData, String catalog) throws SQLException {
        List<FunctionInfo> functions = new ArrayList<>();
        try (ResultSet rs = metaData.getFunctions(catalog, "public", "%")) {
            while (rs.next()) {
                FunctionInfo function = FunctionInfo.builder()
                        .name(rs.getString("FUNCTION_NAME"))
                        .returnType(rs.getString("FUNCTION_TYPE"))
                        .description(rs.getString("REMARKS"))
                        .build();
                functions.add(function);
            }
        }
        return functions;
    }

    private Map<String, Object> discoverServerProperties(Connection conn) throws SQLException {
        Map<String, Object> properties = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW ALL")) {
            while (rs.next()) {
                properties.put(rs.getString("name"), rs.getString("setting"));
            }
        }
        return properties;
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 5432);
        config.put("schema", "public");
        config.put("sslMode", "disable");
        config.put("connectTimeout", 10);
        config.put("socketTimeout", 30);
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
                        .description("PostgreSQL服务器的主机地址")
                        .placeholder("例如: localhost 或 192.168.1.100")
                        .build(),
                ConfigField.builder()
                        .name("port")
                        .label("端口")
                        .type("number")
                        .defaultValue("5432")
                        .required(true)
                        .description("PostgreSQL服务端口")
                        .minValue(1)
                        .maxValue(65535)
                        .build(),
                ConfigField.builder()
                        .name("databaseName")
                        .label("数据库名")
                        .type("text")
                        .defaultValue("postgres")
                        .required(true)
                        .description("要连接的数据库名称")
                        .build(),
                ConfigField.builder()
                        .name("username")
                        .label("用户名")
                        .type("text")
                        .required(true)
                        .description("数据库连接用户名")
                        .build(),
                ConfigField.builder()
                        .name("password")
                        .label("密码")
                        .type("password")
                        .required(true)
                        .description("数据库连接密码")
                        .build(),
                ConfigField.builder()
                        .name("schema")
                        .label("Schema")
                        .type("text")
                        .defaultValue("public")
                        .description("默认Schema名称")
                        .build(),
                ConfigField.builder()
                        .name("sslMode")
                        .label("SSL模式")
                        .type("select")
                        .defaultValue("disable")
                        .options(Arrays.asList("disable", "allow", "prefer", "require", "verify-ca", "verify-full"))
                        .description("SSL连接模式")
                        .build()
        );
    }

    @Override
    public List<String> diagnoseError(String errorCode, String errorMessage) {
        List<String> suggestions = new ArrayList<>();
        
        if (errorMessage.contains("password authentication failed")) {
            suggestions.add("请检查用户名和密码是否正确");
            suggestions.add("请检查pg_hba.conf中的认证配置");
            suggestions.add("请确认用户是否有访问该数据库的权限");
        } else if (errorMessage.contains("database") && errorMessage.contains("does not exist")) {
            suggestions.add("数据库名称不存在，请检查数据库名称是否正确");
            suggestions.add("请先创建数据库: CREATE DATABASE database_name");
        } else if (errorMessage.contains("Connection refused") || errorMessage.contains("connect failed")) {
            suggestions.add("请检查PostgreSQL服务是否正在运行");
            suggestions.add("请检查主机地址和端口是否正确");
            suggestions.add("请检查postgresql.conf中的listen_addresses设置");
            suggestions.add("请检查pg_hba.conf是否允许远程连接");
        } else if (errorMessage.contains("no pg_hba.conf entry")) {
            suggestions.add("请在pg_hba.conf中添加允许该客户端IP的连接配置");
            suggestions.add("执行: host database_name username client_ip/32 md5");
        } else if (errorMessage.contains("SSL connection")) {
            suggestions.add("如果不需要SSL连接，请设置sslMode=disable");
            suggestions.add("如果需要SSL连接，请配置正确的SSL证书");
        } else if (errorMessage.contains("too many connections")) {
            suggestions.add("数据库连接数已达上限");
            suggestions.add("请增加max_connections配置或关闭不必要的连接");
        } else {
            suggestions.addAll(super.diagnoseError(errorCode, errorMessage));
        }
        
        return suggestions;
    }
}
