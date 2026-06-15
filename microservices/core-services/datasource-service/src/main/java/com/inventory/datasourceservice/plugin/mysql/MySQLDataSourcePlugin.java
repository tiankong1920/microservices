package com.inventory.datasourceservice.plugin.mysql;

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
public class MySQLDataSourcePlugin extends AbstractDataSourcePlugin {

    private final EncryptionService encryptionService;

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String JDBC_URL_TEMPLATE = 
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8"
            + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("5.7", "8.0", "8.1", "8.2", "8.3");

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            log.warn("MySQL driver not found in classpath");
        }
    }

    @Override
    public String getPluginName() {
        return "MySQL DataSource Plugin";
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
        return DatasourceConfig.DatasourceType.MYSQL;
    }

    @Override
    public List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    @Override
    protected String buildJdbcUrl(DatasourceConfig config) {
        String database = config.getDatabaseName() != null ? config.getDatabaseName() : "";
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
             ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
            if (rs.next()) {
                String versionString = rs.getString(1);
                Matcher matcher = VERSION_PATTERN.matcher(versionString);
                if (matcher.find()) {
                    return matcher.group(1) + "." + matcher.group(2);
                }
                return versionString;
            }
        } catch (Exception e) {
            log.error("Failed to detect MySQL version", e);
        }
        return null;
    }

    @Override
    public MetadataInfo discoverMetadata(DatasourceConfig config) {
        try (Connection conn = createConnection(config)) {
            String catalog = config.getDatabaseName();
            DatabaseMetaData metaData = conn.getMetaData();
            
            List<TableInfo> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE", "VIEW"})) {
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
                    .build();
        } catch (Exception e) {
            log.error("Failed to discover MySQL metadata", e);
            throw DatasourceException.metadataDiscoveryFailed("MySQL", e.getMessage());
        }
    }

    private List<ColumnInfo> discoverColumns(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(catalog, null, tableName, "%")) {
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
                        .precision(rs.getInt("COLUMN_SIZE"))
                        .scale(rs.getInt("DECIMAL_DIGITS"))
                        .build();
                columns.add(column);
            }
        }
        return columns;
    }

    private List<String> discoverPrimaryKeys(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        try (ResultSet rs = metaData.getPrimaryKeys(catalog, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private List<IndexInfo> discoverIndexes(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        Map<String, List<String>> indexColumns = new LinkedHashMap<>();
        Map<String, Boolean> indexUnique = new LinkedHashMap<>();
        
        try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableName, false, false)) {
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

    private Map<String, Object> discoverServerProperties(Connection conn) throws SQLException {
        Map<String, Object> properties = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE '%version%'")) {
            while (rs.next()) {
                properties.put(rs.getString(1), rs.getString(2));
            }
        }
        return properties;
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 3306);
        config.put("charset", "utf8mb4");
        config.put("collation", "utf8mb4_unicode_ci");
        config.put("timezone", "Asia/Shanghai");
        config.put("useSSL", false);
        config.put("allowPublicKeyRetrieval", true);
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
                        .description("MySQL服务器的主机地址，可以是IP或域名")
                        .placeholder("例如: localhost 或 192.168.1.100")
                        .build(),
                ConfigField.builder()
                        .name("port")
                        .label("端口")
                        .type("number")
                        .defaultValue("3306")
                        .required(true)
                        .description("MySQL服务端口")
                        .minValue(1)
                        .maxValue(65535)
                        .build(),
                ConfigField.builder()
                        .name("databaseName")
                        .label("数据库名")
                        .type("text")
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
                        .name("charset")
                        .label("字符集")
                        .type("select")
                        .defaultValue("utf8mb4")
                        .options(Arrays.asList("utf8mb4", "utf8", "latin1", "gbk"))
                        .description("数据库字符集")
                        .build(),
                ConfigField.builder()
                        .name("timezone")
                        .label("时区")
                        .type("text")
                        .defaultValue("Asia/Shanghai")
                        .description("服务器时区设置")
                        .build()
        );
    }

    @Override
    public List<String> diagnoseError(String errorCode, String errorMessage) {
        List<String> suggestions = new ArrayList<>();
        
        if (errorMessage.contains("Access denied")) {
            suggestions.add("请检查用户名和密码是否正确");
            suggestions.add("请确认用户是否有访问该数据库的权限");
            suggestions.add("执行命令: GRANT ALL PRIVILEGES ON database_name.* TO 'username'@'host'");
        } else if (errorMessage.contains("Unknown database")) {
            suggestions.add("数据库名称不存在，请检查数据库名称是否正确");
            suggestions.add("请先创建数据库: CREATE DATABASE database_name");
        } else if (errorMessage.contains("Connection refused") || errorMessage.contains("Communications link failure")) {
            suggestions.add("请检查MySQL服务是否正在运行");
            suggestions.add("请检查主机地址和端口是否正确");
            suggestions.add("请检查防火墙是否允许该端口访问");
            suggestions.add("请检查MySQL配置文件中bind-address设置");
        } else if (errorMessage.contains("Public Key Retrieval is not allowed")) {
            suggestions.add("在连接参数中添加 allowPublicKeyRetrieval=true");
            suggestions.add("或者使用SSL连接方式");
        } else if (errorMessage.contains("SSL connection")) {
            suggestions.add("如果不需要SSL连接，请设置useSSL=false");
            suggestions.add("如果需要SSL连接，请配置正确的SSL证书");
        } else {
            suggestions.addAll(super.diagnoseError(errorCode, errorMessage));
        }
        
        return suggestions;
    }
}
