/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 监控指标收集器，用于收集和管理自定义监控指标。.
 * <p>.
 * 该类提供了指标收集的方法，包括计数器、计时器和仪表等，.
 * 并通过缓存机制、批处理和并发优化提高性能。.
 * </p>.
 */
@Component
public class MonitoringMetricsCollector {

    /**
 * 日志记录器，用于记录指标收集器的运行状态。.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringMetricsCollector.class);

    /**
 * 指标注册表，用于注册和管理指标。.
     */
    private final MeterRegistry registry;

    /**
 * 业务异常计数器映射，用于存储不同类型的业务异常计数器。.
     */
    private final ConcurrentMap<String, Counter> businessExceptionCounters = new ConcurrentHashMap<>();

    /**
 * 系统异常计数器映射，用于存储不同类型的系统异常计数器。.
     */
    private final ConcurrentMap<String, Counter> systemExceptionCounters = new ConcurrentHashMap<>();

    /**
 * 操作计时器映射，用于存储不同类型的操作计时器。.
     */
    private final ConcurrentMap<String, Timer> operationTimers = new ConcurrentHashMap<>();

    /**
 * 自定义仪表映射，用于存储不同类型的自定义仪表。.
     */
    private final ConcurrentMap<String, AtomicLong> customGauges = new ConcurrentHashMap<>();

    /**
 * 读写锁，用于保护重置操作，提高并发性能。.
     */
    private final ReadWriteLock resetLock = new ReentrantReadWriteLock();

    /**
 * 常用异常类型的缓存，减少重复创建计数器。.
     */
    private static final Map<String, String> COMMON_EXCEPTION_TYPES = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("UNKNOWN_ERROR", "UNKNOWN_ERROR");
                    put("INVALID_PARAMETER", "INVALID_PARAMETER");
                    put("RESOURCE_NOT_FOUND", "RESOURCE_NOT_FOUND");
                    put("DATABASE_ERROR", "DATABASE_ERROR");
                    put("NETWORK_ERROR", "NETWORK_ERROR");
                    put("AUTHENTICATION_ERROR", "AUTHENTICATION_ERROR");
                    put("AUTHORIZATION_ERROR", "AUTHORIZATION_ERROR");
                    put("SERVICE_UNAVAILABLE", "SERVICE_UNAVAILABLE");
                    put("TIMEOUT_ERROR", "TIMEOUT_ERROR");
                    put("VALIDATION_ERROR", "VALIDATION_ERROR");
                }
            }
    );

    /**
 * 异常严重程度计数器映射，用于按严重程度统计异常。.
     */
    private final ConcurrentMap<String, Counter> severityCounters = new ConcurrentHashMap<>();

    /**
 * 异常重试计数器映射，用于统计异常重试次数。.
     */
    private final ConcurrentMap<String, Counter> retryCounters = new ConcurrentHashMap<>();

    /**
 * 批量更新阈值，当缓存的指标数量达到此阈值时进行批量更新。.
     */
    private static final int BATCH_UPDATE_THRESHOLD = 100;

    /**
 * 批量更新间隔，定期执行批量更新的时间间隔（毫秒）。.
     */
    private static final long BATCH_UPDATE_INTERVAL = 5000;

    /**
 * 业务异常批量更新缓存，用于批量更新业务异常计数器。.
     */
    private final ConcurrentMap<String, AtomicLong> businessExceptionBatchCache = new ConcurrentHashMap<>();

    /**
 * 系统异常批量更新缓存，用于批量更新系统异常计数器。.
     */
    private final ConcurrentMap<String, AtomicLong> systemExceptionBatchCache = new ConcurrentHashMap<>();

    /**
 * 定时执行器，用于定期执行批量更新操作。.
     */
    private final ScheduledExecutorService scheduledExecutorService;

    /**
 * 构造方法，注入指标注册表。.
     *
 * @param registry 指标注册表
     */
    public MonitoringMetricsCollector(final MeterRegistry registry) {
        this.registry = registry;
        // 初始化默认指标
        initializeDefaultMetrics();
        // 初始化定时执行器
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "metrics-batch-updater");
            t.setDaemon(true);
            return t;
        });
        // 启动定期批量更新任务
        startBatchUpdateTask();
    }

    /**
 * 启动定期批量更新任务。.
     */
    private void startBatchUpdateTask() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                flushAllBatchCaches();
                LOGGER.debug("Batch update task executed successfully");
            } catch (final RuntimeException e) {
                LOGGER.warn("Failed to execute batch update task: {}", e.getMessage());
            }
        }, BATCH_UPDATE_INTERVAL, BATCH_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
 * 关闭定时执行器，释放资源。.
     */
    public void shutdown() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (final InterruptedException e) {
                scheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("Metrics batch updater shutdown successfully");
        }
    }

    /**
 * 初始化默认指标，包括业务异常计数器、系统异常计数器和操作计时器等。.
     */
    private void initializeDefaultMetrics() {
        // 初始化业务异常计数器
        businessExceptionCounters.put("business.exception", Counter.builder("business.exception.count")
                .tag("type", "business")
                .description("Count of business exceptions")
                .register(registry));

        // 初始化系统异常计数器
        systemExceptionCounters.put("system.exception", Counter.builder("system.exception.count")
                .tag("type", "system")
                .description("Count of system exceptions")
                .register(registry));

        // 初始化请求处理计时器
        operationTimers.put("request.processing", Timer.builder("request.processing.time")
                .tag("type", "http")
                .description("Request processing time")
                .register(registry));

        // 初始化缓存操作计时器
        operationTimers.put("cache.operation", Timer.builder("cache.operation.time")
                .tag("type", "cache")
                .description("Cache operation time")
                .register(registry));

        // 初始化消息处理计时器
        operationTimers.put("message.processing", Timer.builder("message.processing.time")
                .tag("type", "message")
                .description("Message processing time")
                .register(registry));

        // 初始化自定义仪表
        customGauges.put("active.requests", new AtomicLong(0));
        customGauges.put("queue.size", new AtomicLong(0));

        // 注册自定义仪表到指标注册表
        for (final var entry : customGauges.entrySet()) {
            final var name = entry.getKey();
            final var value = entry.getValue();
            Gauge.builder(name, value, AtomicLong::get)
                    .description("Custom gauge: " + name)
                    .register(registry);
        }
    }

    /**
 * 增加业务异常计数，用于统计业务异常的发生次数。.
 * <p>.
 * 使用批量更新缓存提高性能，当缓存达到阈值时进行批量更新。.
 * </p>.
     *
 * @param exceptionType 异常类型
     */
    public void incrementBusinessException(final String exceptionType) {
        // 使用缓存的异常类型，减少重复创建计数器
        final var normalizedType = COMMON_EXCEPTION_TYPES.getOrDefault(exceptionType, exceptionType);

        // 增加批量更新缓存中的计数
        final var count = businessExceptionBatchCache.computeIfAbsent(normalizedType, k -> new AtomicLong(0))
                .incrementAndGet();

        // 当缓存达到阈值时进行批量更新
        if (count % BATCH_UPDATE_THRESHOLD == 0) {
            flushBusinessExceptionBatchCache();
        }
    }

    /**
 * 增加系统异常计数，用于统计系统异常的发生次数。.
 * <p>.
 * 使用批量更新缓存提高性能，当缓存达到阈值时进行批量更新。.
 * </p>.
     *
 * @param exceptionType 异常类型
     */
    public void incrementSystemException(final String exceptionType) {
        // 使用缓存的异常类型，减少重复创建计数器
        final var normalizedType = COMMON_EXCEPTION_TYPES.getOrDefault(exceptionType, exceptionType);

        // 增加批量更新缓存中的计数
        final var count = systemExceptionBatchCache.computeIfAbsent(normalizedType, k -> new AtomicLong(0))
                .incrementAndGet();

        // 当缓存达到阈值时进行批量更新
        if (count % BATCH_UPDATE_THRESHOLD == 0) {
            flushSystemExceptionBatchCache();
        }
    }

    /**
 * 批量更新业务异常计数器。.
     */
    private void flushBusinessExceptionBatchCache() {
        for (final var entry : businessExceptionBatchCache.entrySet()) {
            final var exceptionType = entry.getKey();
            final var count = entry.getValue().getAndSet(0);

            if (count > 0) {
                final var counter = businessExceptionCounters.computeIfAbsent(exceptionType, key -> {
                    return Counter.builder("business.exception.count")
                            .tag("type", "business")
                            .tag("exception.type", key)
                            .description("Count of business exceptions: " + key)
                            .register(registry);
                });
                // 批量增加计数
                for (int i = 0; i < count; i++) {
                    counter.increment();
                }
            }
        }
    }

    /**
 * 批量更新系统异常计数器。.
     */
    private void flushSystemExceptionBatchCache() {
        for (final var entry : systemExceptionBatchCache.entrySet()) {
            final var exceptionType = entry.getKey();
            final var count = entry.getValue().getAndSet(0);

            if (count > 0) {
                final var counter = systemExceptionCounters.computeIfAbsent(exceptionType, key -> {
                    return Counter.builder("system.exception.count")
                            .tag("type", "system")
                            .tag("exception.type", key)
                            .description("Count of system exceptions: " + key)
                            .register(registry);
                });
                // 批量增加计数
                for (int i = 0; i < count; i++) {
                    counter.increment();
                }
            }
        }
    }

    /**
 * 记录操作耗时，用于统计操作的执行时间。.
     *
 * @param operationName 操作名称
 * @param duration      操作耗时（毫秒）
     */
    public void recordOperationTime(final String operationName, final long duration) {
        final var timer = operationTimers.computeIfAbsent(operationName, key -> {
            return Timer.builder("operation.processing.time")
                    .tag("operation", key)
                    .description("Processing time for operation: " + key)
                    .register(registry);
        });
        timer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
 * 增加自定义仪表值，用于统计自定义指标的数值。.
     *
 * @param gaugeName 仪表名称
 * @param value     增加的值
     */
    public void incrementGauge(final String gaugeName, final long value) {
        final var gauge = customGauges.computeIfAbsent(gaugeName, key -> {
            final var atomicLong = new AtomicLong(0);
            Gauge.builder(key, atomicLong, AtomicLong::get)
                    .description("Custom gauge: " + key)
                    .register(registry);
            return atomicLong;
        });
        gauge.addAndGet(value);
    }

    /**
 * 设置自定义仪表值，用于设置自定义指标的数值。.
     *
 * @param gaugeName 仪表名称
 * @param value     设置的值
     */
    public void setGauge(final String gaugeName, final long value) {
        final var gauge = customGauges.computeIfAbsent(gaugeName, key -> {
            final var atomicLong = new AtomicLong(0);
            Gauge.builder(key, atomicLong, AtomicLong::get)
                    .description("Custom gauge: " + key)
                    .register(registry);
            return atomicLong;
        });
        gauge.set(value);
    }

    /**
 * 增加异常严重程度计数，用于按严重程度统计异常。.
     *
 * @param severity 异常严重程度
     */
    public void incrementSeverityCount(final String severity) {
        final var counter = severityCounters.computeIfAbsent(severity, key -> {
            return Counter.builder("exception.severity.count")
                    .tag("severity", key)
                    .description("Count of exceptions by severity: " + key)
                    .register(registry);
        });
        counter.increment();
    }

    /**
 * 增加异常重试计数，用于统计异常重试次数。.
     *
 * @param exceptionType 异常类型
     */
    public void incrementRetryCount(final String exceptionType) {
        final var normalizedType = COMMON_EXCEPTION_TYPES.getOrDefault(exceptionType, exceptionType);

        final var counter = retryCounters.computeIfAbsent(normalizedType, key -> {
            return Counter.builder("exception.retry.count")
                    .tag("exception.type", key)
                    .description("Count of exception retries: " + key)
                    .register(registry);
        });
        counter.increment();
    }

    /**
 * 重置所有指标，用于测试或重置指标状态。.
     */
    public void resetAllMetrics() {
        resetLock.writeLock().lock();
        try {
            // 清除计数器映射
            businessExceptionCounters.clear();
            systemExceptionCounters.clear();
            operationTimers.clear();
            customGauges.clear();
            severityCounters.clear();
            retryCounters.clear();
            businessExceptionBatchCache.clear();
            systemExceptionBatchCache.clear();

            // 重新初始化默认指标
            initializeDefaultMetrics();
        } finally {
            resetLock.writeLock().unlock();
        }
    }

    /**
 * 刷新所有批量更新缓存，确保所有指标都被更新。.
     */
    public void flushAllBatchCaches() {
        flushBusinessExceptionBatchCache();
        flushSystemExceptionBatchCache();
    }
}
