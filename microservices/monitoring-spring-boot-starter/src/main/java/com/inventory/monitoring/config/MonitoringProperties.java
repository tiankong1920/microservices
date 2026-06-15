/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 监控属性配置类，用于读取和管理监控相关的配置属性。.
 * <p>.
 * 该类通过@ConfigurationProperties注解自动绑定application.properties或application.yml中的配置.
 * </p>.
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "inventory.monitoring")
public class MonitoringProperties {

    /**
 * 默认健康检查超时时间（毫秒）。.
     */
    private static final long DEFAULT_HEALTH_CHECK_TIMEOUT_MS = 5000L;

    /**
 * 默认指标收集时间间隔（毫秒）。.
     */
    private static final long DEFAULT_METRICS_INTERVAL_MS = 10000L;

    /**
 * 是否启用监控功能，默认值为true。.
     */
    private boolean enabled = true;

    /**
 * 是否启用健康检查，默认值为true。.
     */
    private boolean healthCheckEnabled = true;

    /**
 * 是否启用指标收集，默认值为true。.
     */
    private boolean metricsEnabled = true;

    /**
 * 是否启用Prometheus指标导出，默认值为true。.
     */
    private boolean prometheusEnabled = true;

    /**
 * 自定义健康检查的超时时间（毫秒），默认值为5000。.
     */
    private long healthCheckTimeout = DEFAULT_HEALTH_CHECK_TIMEOUT_MS;

    /**
 * 指标收集的时间间隔（毫秒），默认值为10000。.
     */
    private long metricsInterval = DEFAULT_METRICS_INTERVAL_MS;

    /**
 * 健康检查配置。.
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    /**
 * 获取是否启用监控功能。.
     *
 * @return 是否启用监控功能
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
 * 设置是否启用监控功能。.
     *
 * @param enabled 是否启用监控功能
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
 * 获取是否启用健康检查。.
     *
 * @return 是否启用健康检查
     */
    public boolean isHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    /**
 * 设置是否启用健康检查。.
     *
 * @param healthCheckEnabled 是否启用健康检查
     */
    public void setHealthCheckEnabled(final boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }

    /**
 * 获取是否启用指标收集。.
     *
 * @return 是否启用指标收集
     */
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    /**
 * 设置是否启用指标收集。.
     *
 * @param metricsEnabled 是否启用指标收集
     */
    public void setMetricsEnabled(final boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    /**
 * 获取是否启用Prometheus指标导出。.
     *
 * @return 是否启用Prometheus指标导出
     */
    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }

    /**
 * 设置是否启用Prometheus指标导出。.
     *
 * @param prometheusEnabled 是否启用Prometheus指标导出
     */
    public void setPrometheusEnabled(final boolean prometheusEnabled) {
        this.prometheusEnabled = prometheusEnabled;
    }

    /**
 * 获取自定义健康检查的超时时间（毫秒）。.
     *
 * @return 自定义健康检查的超时时间（毫秒）
     */
    public long getHealthCheckTimeout() {
        return healthCheckTimeout;
    }

    /**
 * 设置自定义健康检查的超时时间（毫秒）。.
     *
 * @param healthCheckTimeout 自定义健康检查的超时时间（毫秒）
     */
    public void setHealthCheckTimeout(final long healthCheckTimeout) {
        this.healthCheckTimeout = healthCheckTimeout;
    }

    /**
 * 获取指标收集的时间间隔（毫秒）。.
     *
 * @return 指标收集的时间间隔（毫秒）
     */
    public long getMetricsInterval() {
        return metricsInterval;
    }

    /**
 * 设置指标收集的时间间隔（毫秒）。.
     *
 * @param metricsInterval 指标收集的时间间隔（毫秒）
     */
    public void setMetricsInterval(final long metricsInterval) {
        this.metricsInterval = metricsInterval;
    }

    /**
 * 获取健康检查配置。.
     *
 * @return 健康检查配置
     */
    public HealthCheckConfig getHealthCheck() {
        return healthCheck;
    }

    /**
 * 设置健康检查配置。.
     *
 * @param healthCheck 健康检查配置
     */
    public void setHealthCheck(final HealthCheckConfig healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
 * 健康检查配置类。.
     */
    public static class HealthCheckConfig {

        /**
 * 健康检查超时时间（秒），默认值为10。.
         */
        private int timeoutSeconds = 10;

        /**
 * 内存使用率阈值（百分比），默认值为90。.
         */
        private double memoryThreshold = 90.0;

        /**
 * 磁盘使用率阈值（百分比），默认值为90。.
         */
        private double diskThreshold = 90.0;

        /**
 * CPU使用率阈值（百分比），默认值为90。.
         */
        private double cpuThreshold = 90.0;

        /**
 * 获取健康检查超时时间（秒）。.
         *
 * @return 健康检查超时时间（秒）
         */
        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        /**
 * 设置健康检查超时时间（秒）。.
         *
 * @param timeoutSeconds 健康检查超时时间（秒）
         */
        public void setTimeoutSeconds(final int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        /**
 * 获取内存使用率阈值（百分比）。.
         *
 * @return 内存使用率阈值（百分比）
         */
        public double getMemoryThreshold() {
            return memoryThreshold;
        }

        /**
 * 设置内存使用率阈值（百分比）。.
         *
 * @param memoryThreshold 内存使用率阈值（百分比）
         */
        public void setMemoryThreshold(final double memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        /**
 * 获取磁盘使用率阈值（百分比）。.
         *
 * @return 磁盘使用率阈值（百分比）
         */
        public double getDiskThreshold() {
            return diskThreshold;
        }

        /**
 * 设置磁盘使用率阈值（百分比）。.
         *
 * @param diskThreshold 磁盘使用率阈值（百分比）
         */
        public void setDiskThreshold(final double diskThreshold) {
            this.diskThreshold = diskThreshold;
        }

        /**
 * 获取CPU使用率阈值（百分比）。.
         *
 * @return CPU使用率阈值（百分比）
         */
        public double getCpuThreshold() {
            return cpuThreshold;
        }

        /**
 * 设置CPU使用率阈值（百分比）。.
         *
 * @param cpuThreshold CPU使用率阈值（百分比）
         */
        public void setCpuThreshold(final double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }
    }
}
