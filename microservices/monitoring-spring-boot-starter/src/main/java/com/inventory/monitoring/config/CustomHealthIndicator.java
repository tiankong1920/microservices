/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

/**
 * 自定义健康检查指示器，用于检查系统的健康状态.
 * <p>
 * 该类提供自定义的健康检查逻辑，包括系统状态、内存使用情况和服务可用性检查.
 * </p>
 */
@Component
public class CustomHealthIndicator {

    /**
 * 日志记录器，用于记录健康检查的执行状态.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHealthIndicator.class);

    /**
 * 监控属性配置，用于获取健康检查的超时时间等配置.
     */
    private final MonitoringProperties monitoringProperties;

    /**
 * 构造方法，注入监控属性配置.
     *
 * @param monitoringProperties 监控属性配置
     */
    public CustomHealthIndicator(final MonitoringProperties monitoringProperties) {
        this.monitoringProperties = monitoringProperties;
    }

    /**
 * 检查系统健康状态，返回健康信息.
     *
 * @return 健康信息对象，包含系统状态、内存使用情况等详细信息
     */
    public HealthStatus health() {
        final var executor = Executors.newSingleThreadExecutor();
        try {
            final Future<HealthStatus> future = executor.submit(this::performHealthCheck);
            return future.get(monitoringProperties.getHealthCheck().getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            LOGGER.error("Health check timed out");
            return HealthStatus.down("Health check timed out");
        } catch (final ExecutionException e) {
            LOGGER.error("Health check failed: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        } catch (final InterruptedException e) {
            LOGGER.error("Health check interrupted: {}", e.getMessage(), e);
            return HealthStatus.down("Health check interrupted");
        } finally {
            executor.shutdownNow();
        }
    }

    /**
 * 执行健康检查的具体逻辑。.
     *
 * @return 健康信息对象
     */
    private HealthStatus performHealthCheck() {
        try {
            final Map<String, Object> details = new HashMap<>();

            // 检查内存使用情况
            final HealthStatus memoryHealth = checkMemoryHealth();
            if (!memoryHealth.isUp()) {
                return memoryHealth;
            }
            details.put("memory", memoryHealth.getDetails());

            // 检查磁盘空间
            final HealthStatus diskHealth = checkDiskHealth();
            if (!diskHealth.isUp()) {
                return diskHealth;
            }
            details.put("disk", diskHealth.getDetails());

            // 检查CPU使用率
            final HealthStatus cpuHealth = checkCpuHealth();
            details.put("cpu", cpuHealth.getDetails());

            // 检查线程状态
            final HealthStatus threadHealth = checkThreadHealth();
            details.put("threads", threadHealth.getDetails());

            return HealthStatus.up(details);
        } catch (final RuntimeException e) {
            LOGGER.error("Error performing health check: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        }
    }

    /**
 * 检查内存健康状态。.
     *
 * @return 内存健康状态
     */
    private HealthStatus checkMemoryHealth() {
        try {
            final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            final MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            final MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            final long heapUsed = heapUsage.getUsed();
            final long heapMax = heapUsage.getMax();
            final double heapUsagePercent = heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0;

            final long nonHeapUsed = nonHeapUsage.getUsed();
            final long nonHeapMax = nonHeapUsage.getMax();
            final double nonHeapUsagePercent = nonHeapMax > 0 ? (double) nonHeapUsed / nonHeapMax * 100 : 0;

            final double threshold = monitoringProperties.getHealthCheck().getMemoryThreshold();

            final Map<String, Object> details = new HashMap<>();
            details.put("heap.used", heapUsed);
            details.put("heap.max", heapMax);
            details.put("heap.usage.percent", String.format("%.2f%%", heapUsagePercent));
            details.put("nonHeap.used", nonHeapUsed);
            details.put("nonHeap.max", nonHeapMax);
            details.put("nonHeap.usage.percent", String.format("%.2f%%", nonHeapUsagePercent));
            details.put("threshold.percent", threshold);

            if (heapUsagePercent > threshold) {
                return HealthStatus.down("Memory usage exceeds threshold", details);
            }

            return HealthStatus.up(details);
        } catch (final RuntimeException e) {
            LOGGER.error("Error checking memory health: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        }
    }

    /**
 * 检查磁盘健康状态。.
     *
 * @return 磁盘健康状态
     */
    private HealthStatus checkDiskHealth() {
        try {
            java.io.File root;
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("win")) {
                root = new java.io.File("C:\\");
            } else {
                root = new java.io.File("/");
            }
            final long totalSpace = root.getTotalSpace();
            final long freeSpace = root.getFreeSpace();
            final long usableSpace = root.getUsableSpace();
            final double usagePercent = totalSpace > 0 ? (double) (totalSpace - freeSpace) / totalSpace * 100 : 0;

            final double threshold = monitoringProperties.getHealthCheck().getDiskThreshold();

            final Map<String, Object> details = new HashMap<>();
            details.put("total.space", totalSpace);
            details.put("free.space", freeSpace);
            details.put("usable.space", usableSpace);
            details.put("usage.percent", String.format("%.2f%%", usagePercent));
            details.put("threshold.percent", threshold);

            if (usagePercent > threshold) {
                return HealthStatus.down("Disk usage exceeds threshold", details);
            }

            return HealthStatus.up(details);
        } catch (final RuntimeException e) {
            LOGGER.error("Error checking disk health: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        }
    }

    /**
 * 检查CPU健康状态。.
     *
 * @return CPU健康状态
     */
    private HealthStatus checkCpuHealth() {
        try {
            final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
            final double systemLoad = osMXBean.getSystemLoadAverage();
            final int availableProcessors = osMXBean.getAvailableProcessors();

            final double threshold = monitoringProperties.getHealthCheck().getCpuThreshold();

            final Map<String, Object> details = new HashMap<>();
            details.put("system.load.average", systemLoad >= 0 ? String.format("%.2f", systemLoad) : "N/A");
            details.put("available.processors", availableProcessors);
            details.put("threshold.percent", threshold);

            if (systemLoad >= 0 && systemLoad > threshold) {
                return HealthStatus.down("System load exceeds threshold", details);
            }

            return HealthStatus.up(details);
        } catch (final RuntimeException e) {
            LOGGER.error("Error checking CPU health: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        }
    }

    /**
 * 检查线程健康状态。.
     *
 * @return 线程健康状态
     */
    private HealthStatus checkThreadHealth() {
        try {
            final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            final int threadCount = threadMXBean.getThreadCount();
            final int peakThreadCount = threadMXBean.getPeakThreadCount();
            final long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
            final int daemonThreadCount = threadMXBean.getDaemonThreadCount();

            final Map<String, Object> details = new HashMap<>();
            details.put("thread.count", threadCount);
            details.put("peak.thread.count", peakThreadCount);
            details.put("total.started.thread.count", totalStartedThreadCount);
            details.put("daemon.thread.count", daemonThreadCount);

            return HealthStatus.up(details);
        } catch (final RuntimeException e) {
            LOGGER.error("Error checking thread health: {}", e.getMessage(), e);
            return HealthStatus.down(e.getMessage());
        }
    }

    /**
 * 健康状态类，用于表示健康检查结果。.
     */
    public static class HealthStatus {

        /**
 * 状态（UP或DOWN）。.
         */
        private final String status;

        /**
 * 详细信息。.
         */
        private final Map<String, Object> details;

        /**
 * 构造方法。.
         *
 * @param status  状态
 * @param details 详细信息
         */
        private HealthStatus(final String status, final Map<String, Object> details) {
            this.status = status;
            this.details = details;
        }

        /**
 * 创建UP状态的健康检查结果。.
         *
 * @return 健康检查结果
         */
        public static HealthStatus up() {
            return new HealthStatus("UP", new HashMap<>());
        }

        /**
 * 创建UP状态的健康检查结果。.
         *
 * @param details 详细信息
 * @return 健康检查结果
         */
        public static HealthStatus up(final Map<String, Object> details) {
            return new HealthStatus("UP", details);
        }

        /**
 * 创建DOWN状态的健康检查结果。.
         *
 * @param reason 原因
 * @return 健康检查结果
         */
        public static HealthStatus down(final String reason) {
            final Map<String, Object> details = new HashMap<>();
            details.put("reason", reason);
            return new HealthStatus("DOWN", details);
        }

        /**
 * 创建DOWN状态的健康检查结果。.
         *
 * @param reason  原因
 * @param details 详细信息
 * @return 健康检查结果
         */
        public static HealthStatus down(final String reason, final Map<String, Object> details) {
            details.put("reason", reason);
            return new HealthStatus("DOWN", details);
        }

        /**
 * 检查状态是否为UP。.
         *
 * @return 是否为UP
         */
        public boolean isUp() {
            return "UP".equals(status);
        }

        /**
 * 获取状态。.
         *
 * @return 状态
         */
        public String getStatus() {
            return status;
        }

        /**
 * 获取详细信息。.
         *
 * @return 详细信息
         */
        public Map<String, Object> getDetails() {
            return details;
        }
    }
}
