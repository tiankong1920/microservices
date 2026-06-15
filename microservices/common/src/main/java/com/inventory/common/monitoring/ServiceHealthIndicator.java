package com.inventory.common.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 服务健康状态指示器.
 *
 * <p>用于监控和报告服务的健康状态，包括数据库、Redis、Kafka等组件的连接状态。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@SuppressWarnings("null")
public class ServiceHealthIndicator implements HealthIndicator {

    /** 健康状态. */
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    /** 最后检查时间. */
    private final AtomicLong lastCheckTime = new AtomicLong(System.currentTimeMillis());

    /** 详细信息. */
    private final Map<String, Object> details = new ConcurrentHashMap<>();

    /**
     * 获取健康状态.
     *
     * @return 健康状态对象
     */
    @Override
    public Health health() {
        lastCheckTime.set(System.currentTimeMillis());

        final Health.Builder builder = healthy.get() ? Health.up() : Health.down();
        return builder
                .withDetails(details)
                .withDetail("lastCheck", lastCheckTime.get())
                .build();
    }

    /**
     * 设置健康状态.
     *
     * @param healthyValue 健康状态值
     */
    public void setHealthy(final boolean healthyValue) {
        this.healthy.set(healthyValue);
    }

    /**
     * 添加详细信息.
     *
     * @param key 键
     * @param value 值
     */
    public void addDetail(final String key, final Object value) {
        details.put(key, value);
    }

    /**
     * 移除详细信息.
     *
     * @param key 键
     */
    public void removeDetail(final String key) {
        details.remove(key);
    }

    /**
     * 更新数据库状态.
     *
     * @param connected 是否已连接
     * @param queryTimeMs 查询时间（毫秒）
     */
    public void updateDatabaseStatus(final boolean connected, final long queryTimeMs) {
        details.put("database.connected", connected);
        details.put("database.queryTimeMs", queryTimeMs);
        if (!connected) {
            healthy.set(false);
        }
    }

    /**
     * 更新Redis状态.
     *
     * @param connected 是否已连接
     * @param latencyMs 延迟时间（毫秒）
     */
    public void updateRedisStatus(final boolean connected, final long latencyMs) {
        details.put("redis.connected", connected);
        details.put("redis.latencyMs", latencyMs);
        if (!connected) {
            healthy.set(false);
        }
    }

    /**
     * 更新Kafka状态.
     *
     * @param connected 是否已连接
     * @param lag 消息延迟数
     */
    public void updateKafkaStatus(final boolean connected, final int lag) {
        details.put("kafka.connected", connected);
        details.put("kafka.lag", lag);
    }

    /**
     * 更新内存使用情况.
     *
     * @param used 已使用内存
     * @param max 最大内存
     * @param percentage 使用百分比
     */
    public void updateMemoryUsage(final long used, final long max, final double percentage) {
        details.put("memory.usedMB", used / (1024 * 1024));
        details.put("memory.maxMB", max / (1024 * 1024));
        details.put("memory.percentage", percentage);

        if (percentage > 90) {
            healthy.set(false);
        }
    }

    /**
     * 更新线程数.
     *
     * @param active 活动线程数
     * @param max 最大线程数
     */
    public void updateThreadCount(final int active, final int max) {
        details.put("threads.active", active);
        details.put("threads.max", max);

        if (active > max * 0.9) {
            healthy.set(false);
        }
    }

    /**
     * 更新请求指标.
     *
     * @param totalRequests 总请求数
     * @param failedRequests 失败请求数
     * @param avgResponseTimeMs 平均响应时间（毫秒）
     */
    public void updateRequestMetrics(final long totalRequests,
            final long failedRequests, final double avgResponseTimeMs) {
        details.put("requests.total", totalRequests);
        details.put("requests.failed", failedRequests);
        details.put("requests.avgResponseTimeMs", avgResponseTimeMs);

        if (totalRequests > 0 && (double) failedRequests / totalRequests > 0.1) {
            healthy.set(false);
        }
    }
}
