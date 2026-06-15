package com.inventory.common.monitoring;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Component;

/**
 * 业务指标收集器.
 *
 * <p>用于收集和记录各种业务相关的监控指标，包括订单、产品、库存、缓存、API调用等。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@SuppressWarnings("null")
public class BusinessMetricsCollector {

    /** 指标注册表. */
    private final MeterRegistry meterRegistry;

    /** 指标值映射表. */
    private final ConcurrentMap<String, AtomicLong> gauges = new ConcurrentHashMap<>();

    /** 计数器映射表. */
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    /** 计时器映射表. */
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    /** 服务标签名. */
    private static final String TAG_SERVICE = "service";

    /**
     * 构造函数.
     *
     * @param meterRegistry 指标注册表
     */
    public BusinessMetricsCollector(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录订单创建事件.
     *
     * @param orderType 订单类型
     */
    public void recordOrderCreated(final String orderType) {
        counter("orders.created", "type", orderType).increment();
    }

    /**
     * 记录订单完成事件.
     *
     * @param orderType 订单类型
     * @param duration 处理时长
     */
    public void recordOrderCompleted(final String orderType, final Duration duration) {
        counter("orders.completed", "type", orderType).increment();
        timer("orders.duration", "type", orderType).record(duration);
    }

    /**
     * 记录订单失败事件.
     *
     * @param orderType 订单类型
     * @param reason 失败原因
     */
    public void recordOrderFailed(final String orderType, final String reason) {
        counter("orders.failed", "type", orderType, "reason", reason).increment();
    }

    /**
     * 记录产品创建事件.
     *
     * @param category 产品分类
     */
    public void recordProductCreated(final String category) {
        counter("products.created", "category", category).increment();
    }

    /**
     * 记录产品更新事件.
     *
     * @param category 产品分类
     */
    public void recordProductUpdated(final String category) {
        counter("products.updated", "category", category).increment();
    }

    /**
     * 记录库存变更事件.
     *
     * @param productId 产品ID
     * @param quantity 变更数量
     * @param operation 操作类型
     */
    public void recordInventoryChange(final String productId,
            final int quantity, final String operation) {
        counter("inventory.changes",
                "productId", productId,
                "operation", operation).increment(quantity);
    }

    /**
     * 设置库存水平.
     *
     * @param productId 产品ID
     * @param level 库存水平
     */
    public void setInventoryLevel(final String productId, final long level) {
        gauge("inventory.level", "productId", productId).set(level);
    }

    /**
     * 记录缓存命中.
     *
     * @param cacheName 缓存名称
     */
    public void recordCacheHit(final String cacheName) {
        counter("cache.hits", "name", cacheName).increment();
    }

    /**
     * 记录缓存未命中.
     *
     * @param cacheName 缓存名称
     */
    public void recordCacheMiss(final String cacheName) {
        counter("cache.misses", "name", cacheName).increment();
    }

    /**
     * 记录API调用.
     *
     * @param serviceName 服务名称
     * @param endpoint 端点
     * @param duration 调用时长
     * @param success 是否成功
     */
    public void recordApiCall(final String serviceName, final String endpoint,
            final Duration duration, final boolean success) {
        timer("api.calls",
                TAG_SERVICE, serviceName,
                "endpoint", endpoint,
                "success", String.valueOf(success)).record(duration);
    }

    /**
     * 记录数据库查询.
     *
     * @param operation 操作类型
     * @param duration 查询时长
     */
    public void recordDatabaseQuery(final String operation, final Duration duration) {
        timer("database.queries", "operation", operation).record(duration);
    }

    /**
     * 设置活动连接数.
     *
     * @param service 服务名称
     * @param count 连接数
     */
    public void setActiveConnections(final String service, final long count) {
        gauge("connections.active", TAG_SERVICE, service).set(count);
    }

    /**
     * 记录错误.
     *
     * @param service 服务名称
     * @param errorType 错误类型
     */
    public void recordError(final String service, final String errorType) {
        counter("errors.total", TAG_SERVICE, service, "type", errorType).increment();
    }

    /**
     * 获取或创建计数器.
     *
     * @param name 计数器名称
     * @param tags 标签
     * @return 计数器
     */
    private Counter counter(final String name, final String... tags) {
        final String key = name + ":" + String.join(",", tags);
        return counters.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tags(tags)
                        .register(meterRegistry));
    }

    /**
     * 获取或创建计时器.
     *
     * @param name 计时器名称
     * @param tags 标签
     * @return 计时器
     */
    private Timer timer(final String name, final String... tags) {
        final String key = name + ":" + String.join(",", tags);
        return timers.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .tags(tags)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .minimumExpectedValue(Duration.ofMillis(1))
                        .maximumExpectedValue(Duration.ofSeconds(30))
                        .register(meterRegistry));
    }

    /**
     * 获取或创建指标值.
     *
     * @param name 指标名称
     * @param tags 标签
     * @return 指标值
     */
    private AtomicLong gauge(final String name, final String... tags) {
        final String key = name + ":" + String.join(",", tags);
        return gauges.computeIfAbsent(key, k -> {
            final AtomicLong value = new AtomicLong(0);
            Gauge.builder(name, value, AtomicLong::get)
                    .tags(tags)
                    .register(meterRegistry);
            return value;
        });
    }
}
