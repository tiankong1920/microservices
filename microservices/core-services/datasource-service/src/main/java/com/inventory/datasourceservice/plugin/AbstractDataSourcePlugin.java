package com.inventory.datasourceservice.plugin;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDataSourcePlugin implements DataSourcePlugin {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    protected static final int DEFAULT_READ_TIMEOUT = 30000;

    @Override
    public ConnectionTestResultDTO testConnection(DatasourceConfig config) {
        return doTestConnection(config);
    }

    protected abstract String buildJdbcUrl(DatasourceConfig config);

    protected abstract String getDriverClassName();

    protected Connection createConnection(DatasourceConfig config) throws SQLException {
        String url = buildJdbcUrl(config);
        String username = config.getUsername();
        String password = decryptPassword(config.getPassword());

        try {
            Class.forName(getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found: " + getDriverClassName(), e);
        }

        DriverManager.setLoginTimeout(DEFAULT_CONNECTION_TIMEOUT / 1000);
        return DriverManager.getConnection(url, username, password);
    }

    protected ConnectionTestResultDTO doTestConnection(DatasourceConfig config) {
        long startTime = System.nanoTime();
        String datasourceName = config.getName();

        try (Connection conn = createConnection(config)) {
            if (conn.isValid(DEFAULT_CONNECTION_TIMEOUT / 1000)) {
                long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                return ConnectionTestResultDTO.success(config.getId(), datasourceName, (int) responseTime);
            } else {
                return ConnectionTestResultDTO.failure(
                        config.getId(),
                        datasourceName,
                        "INVALID_CONNECTION",
                        "连接验证失败",
                        List.of("请检查数据库服务是否正常运行", "请验证用户名和密码是否正确", "请检查网络连接是否正常")
                );
            }
        } catch (SQLException e) {
            long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            String errorCode = e.getSQLState() != null ? e.getSQLState() : "UNKNOWN";
            List<String> suggestions = diagnoseError(errorCode, e.getMessage());

            return ConnectionTestResultDTO.failure(
                    config.getId(),
                    datasourceName,
                    errorCode,
                    e.getMessage(),
                    suggestions,
                    (int) responseTime
            );
        } catch (Exception e) {
            logger.error("Connection test failed for datasource: {}", config.getName(), e);
            return ConnectionTestResultDTO.failure(
                    config.getId(),
                    datasourceName,
                    "INTERNAL_ERROR",
                    e.getMessage(),
                    List.of("请检查配置参数是否正确", "请查看系统日志获取详细错误信息", "请联系系统管理员")
            );
        }
    }

    protected String decryptPassword(String encryptedPassword) {
        return encryptedPassword;
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return new HashMap<>();
    }

    @Override
    public List<ConfigField> getConfigFields() {
        return new ArrayList<>();
    }

    @Override
    public List<String> diagnoseError(String errorCode, String errorMessage) {
        List<String> suggestions = new ArrayList<>();

        String lowerMessage = errorMessage != null ? errorMessage.toLowerCase() : "";

        if (lowerMessage.contains("connection refused") || lowerMessage.contains("connect timed out")) {
            suggestions.add("请检查目标服务器是否正在运行");
            suggestions.add("请验证主机地址和端口是否正确");
            suggestions.add("请检查防火墙是否允许该端口的连接");
        } else if (lowerMessage.contains("access denied") || lowerMessage.contains("authentication failed")) {
            suggestions.add("请检查用户名是否正确");
            suggestions.add("请验证密码是否正确");
            suggestions.add("请确认用户是否有远程访问权限");
        } else if (lowerMessage.contains("unknown database") || lowerMessage.contains("database") && lowerMessage.contains("not exist")) {
            suggestions.add("请检查数据库名称是否正确");
            suggestions.add("请确认数据库是否已创建");
            suggestions.add("请验证用户是否有访问该数据库的权限");
        } else if (lowerMessage.contains("ssl") || lowerMessage.contains("tls")) {
            suggestions.add("请检查SSL/TLS配置是否正确");
            suggestions.add("请验证证书是否有效");
            suggestions.add("如果不需要SSL，可以尝试禁用SSL连接");
        } else {
            suggestions.add("请检查所有配置参数是否正确");
            suggestions.add("请查看详细错误信息以获取更多线索");
            suggestions.add("请联系系统管理员或数据库管理员");
        }

        return suggestions;
    }
}
