package com.inventory.monitoring.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 监控配置类.
 * 集成Prometheus指标收集和健康检查.
 */
@Configuration
@SuppressWarnings("unused")
public class MonitoringConfig {

    @Value("${spring.application.name:inventory-service}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    private final Map<String, Counter> counters = new HashMap<>();
    private final Map<String, Gauge> gauges = new HashMap<>();
    private final Map<String, Timer> timers = new HashMap<>();

    @Bean
    public Counter businessExceptionCounter(MeterRegistry registry) {
        return Counter.builder("business.exception.count")
                .tag("type", "business")
                .tag("service", applicationName)
                .description("Count of business exceptions")
                .register(registry);
    }

    @Bean
    public Counter orderExceptionCounter(MeterRegistry registry) {
        return Counter.builder("order.exception.count")
                .tag("type", "order")
                .tag("service", applicationName)
                .description("Count of order exceptions")
                .register(registry);
    }

    @Bean
    public Counter systemExceptionCounter(MeterRegistry registry) {
        return Counter.builder("system.exception.count")
                .tag("type", "system")
                .tag("service", applicationName)
                .description("Count of system exceptions")
                .register(registry);
    }

    @Bean
    public Gauge systemLoadGauge(MeterRegistry registry) {
        return Gauge.builder("system.load", this, MonitoringConfig::getSystemLoad)
                .tag("type", "system")
                .tag("service", applicationName)
                .description("Current system load")
                .register(registry);
    }

    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("api.response.time")
                .tag("type", "api")
                .tag("service", applicationName)
                .description("API response time")
                .register(registry);
    }

    @Bean
    public Gauge databaseConnectionGauge(MeterRegistry registry) {
        return Gauge.builder("database.connections.active", this, MonitoringConfig::getDatabaseConnections)
                .tag("type", "database")
                .tag("service", applicationName)
                .description("Active database connections")
                .register(registry);
    }

    @Bean
    public Supplier<Health> customHealthIndicator() {
        return () -> {
            try {
                // 简单的健康检查
                final boolean isHealthy = checkSystemHealth();

                if (isHealthy) {
                    return Health.up()
                            .withDetail("service", applicationName)
                            .withDetail("version", applicationVersion)
                            .withDetail("status", "Running")
                            .withDetail("timestamp", System.currentTimeMillis())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("service", applicationName)
                            .withDetail("error", "System health check failed")
                            .withDetail("timestamp", System.currentTimeMillis())
                            .build();
                }
            } catch (final RuntimeException e) {
                return Health.down()
                        .withDetail("service", applicationName)
                        .withDetail("error", e.getMessage())
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }
        };
    }

    private double getSystemLoad() {
        try {
            // 获取系统负载
            final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemLoadAverage();
        } catch (final RuntimeException e) {
            return 0.0;
        }
    }

    private double getDatabaseConnections() {
        try {
            // 这里应该从数据源获取活跃连接数
            // 简化实现，返回模拟值
            return 10.0;
        } catch (final RuntimeException e) {
            return 0.0;
        }
    }

    private boolean checkSystemHealth() {
        try {
            // 简单的健康检查逻辑
            // 可以检查数据库连接、外部服务依赖等
            return true;
        } catch (final RuntimeException e) {
            return false;
        }
    }
}
