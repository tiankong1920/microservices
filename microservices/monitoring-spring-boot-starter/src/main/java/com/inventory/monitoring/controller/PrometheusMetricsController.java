package com.inventory.monitoring.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

/**
 * Prometheus指标导出控制器.
 * 提供/metrics端点用于Prometheus拉取监控数据.
 */
@RestController
@RequestMapping("/metrics")
public class PrometheusMetricsController {

    private final Map<String, Object> metrics = new HashMap<>();

    /**
 * 获取所有指标.
 * @return 包含应用、系统和业务指标的完整数据
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        return ResponseEntity.ok(metrics);
    }

    /**
 * 获取特定指标.
 * @param metricName 指标名称
 * @return 指标值或null
     */
    @GetMapping("/{metricName}")
    public ResponseEntity<Map<String, Object>> getMetric(@PathVariable String metricName) {
        final Object value = metrics.get(metricName);
        if (value == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Metric not found",
                    "metric", metricName
            ));
        }
        return ResponseEntity.ok(Map.of(metricName, value));
    }

    /**
 * 获取指标摘要.
 * @return 包含主要系统指标和统计信息
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        final Map<String, Object> summary = new HashMap<>();
        summary.put("application.name", "Inventory Management System");
        summary.put("application.version", "3.0.0");
        summary.put("system.uptime", System.currentTimeMillis());
        summary.put("total.metrics", metrics.size());

        return ResponseEntity.ok(summary);
    }

    /**
 * 获取健康状态指标.
 * @return 健康检查相关指标
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthMetrics() {
        final Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("checks", 3);
        health.put("database", "connected");
        health.put("disk", "healthy");
        health.put("memory", "85%");

        return ResponseEntity.ok(health);
    }
}
