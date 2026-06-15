package com.inventory.templateservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器 - 管理服务监控和健康检查
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "系统监控", description = "系统监控和健康检查接口")
@Validated
public class MonitoringController {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    private DataSource dataSource;

    @GetMapping("/info")
    @Operation(summary = "获取服务信息", description = "获取模板服务的基本信息")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("service", "template-service");
        info.put("version", buildProperties != null ? buildProperties.getVersion() : "1.0.0");
        info.put("description", "商业模板标准化系统服务");
        info.put("status", "UP");
        
        if (buildProperties != null) {
            info.put("buildTime", buildProperties.getTime());
            info.put("artifact", buildProperties.getArtifact());
            info.put("group", buildProperties.getGroup());
        }
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health/db")
    @Operation(summary = "数据库健康检查", description = "检查数据库连接状态")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            health.put("status", valid ? "UP" : "DOWN");
            health.put("database", "PostgreSQL");
            health.put("validationQuery", "SELECT 1");
            
            if (valid) {
                health.put("connectionPool", "HikariCP");
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/health/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否就绪接收请求")
    public ResponseEntity<Map<String, Object>> checkReadiness() {
        Map<String, Object> ready = new HashMap<>();
        
        boolean dbReady = checkDatabaseConnection();
        
        ready.put("ready", dbReady);
        ready.put("checks", Map.of(
            "database", dbReady ? "UP" : "DOWN"
        ));
        
        if (dbReady) {
            return ResponseEntity.ok(ready);
        } else {
            return ResponseEntity.status(503).body(ready);
        }
    }

    @GetMapping("/health/live")
    @Operation(summary = "存活检查", description = "检查服务是否存活")
    public ResponseEntity<Map<String, Object>> checkLiveness() {
        Map<String, Object> live = new HashMap<>();
        live.put("alive", true);
        return ResponseEntity.ok(live);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取服务统计", description = "获取模板服务的统计数据")
    public ResponseEntity<Map<String, Object>> getServiceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        stats.put("memory", Map.of(
            "max", formatBytes(maxMemory),
            "total", formatBytes(totalMemory),
            "free", formatBytes(freeMemory),
            "used", formatBytes(usedMemory),
            "usagePercent", String.format("%.2f", (double) usedMemory / maxMemory * 100)
        ));
        
        stats.put("processors", runtime.availableProcessors());
        
        stats.put("threads", Map.of(
            "active", Thread.activeCount()
        ));
        
        return ResponseEntity.ok(stats);
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
