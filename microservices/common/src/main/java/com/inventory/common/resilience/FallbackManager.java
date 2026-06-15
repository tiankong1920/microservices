package com.inventory.common.resilience;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

/**
 * 降级管理器.
 *
 * <p>用于管理服务降级策略，支持注册策略、执行降级逻辑和监控服务健康状态。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@SuppressWarnings("null")
public class FallbackManager {

    /** 降级策略映射表. */
    private final Map<String, FallbackStrategy> strategies = new ConcurrentHashMap<>();

    /** 默认响应映射表. */
    private final Map<String, Object> defaultResponses = new ConcurrentHashMap<>();

    /**
     * 注册降级策略.
     *
     * @param serviceName 服务名称
     * @param strategy 降级策略
     */
    public void registerStrategy(final String serviceName, final FallbackStrategy strategy) {
        strategies.put(serviceName, strategy);
    }

    /**
     * 注册默认响应.
     *
     * @param serviceName 服务名称
     * @param defaultResponse 默认响应
     */
    public void registerDefaultResponse(final String serviceName, final Object defaultResponse) {
        defaultResponses.put(serviceName, defaultResponse);
    }

    /**
     * 执行带降级的操作.
     *
     * @param <T> 返回类型
     * @param serviceName 服务名称
     * @param primary 主操作
     * @param fallback 降级操作
     * @return 操作结果
     */
    public <T> T executeWithFallback(final String serviceName,
            final Supplier<T> primary, final Supplier<T> fallback) {
        final FallbackStrategy strategy = strategies.get(serviceName);

        if (strategy != null && strategy.isDegraded()) {
            return fallback.get();
        }

        try {
            final T result = primary.get();
            if (strategy != null) {
                strategy.recordSuccess();
            }
            return result;
        } catch (Exception e) {
            if (strategy != null) {
                strategy.recordFailure();
            }
            return fallback.get();
        }
    }

    /**
     * 执行带默认降级的操作.
     *
     * @param <T> 返回类型
     * @param serviceName 服务名称
     * @param primary 主操作
     * @return 操作结果
     */
    @SuppressWarnings("unchecked")
    public <T> T executeWithDefaultFallback(final String serviceName, final Supplier<T> primary) {
        return executeWithFallback(serviceName, primary,
                () -> (T) defaultResponses.get(serviceName));
    }

    /**
     * 设置降级模式.
     *
     * @param serviceName 服务名称
     * @param degraded 是否降级
     */
    public void setDegradedMode(final String serviceName, final boolean degraded) {
        final FallbackStrategy strategy = strategies.get(serviceName);
        if (strategy != null) {
            strategy.setDegraded(degraded);
        }
    }

    /**
     * 检查是否降级.
     *
     * @param serviceName 服务名称
     * @return 是否降级
     */
    public boolean isDegraded(final String serviceName) {
        final FallbackStrategy strategy = strategies.get(serviceName);
        if (strategy == null) {
            return false;
        }
        return strategy.isDegraded();
    }

    /**
     * 获取服务健康状态.
     *
     * @param serviceName 服务名称
     * @return 服务健康状态
     */
    public ServiceHealth getHealth(final String serviceName) {
        final FallbackStrategy strategy = strategies.get(serviceName);
        if (strategy == null) {
            return new ServiceHealth(false, 0, 0, 1.0);
        }

        return new ServiceHealth(
                strategy.isDegraded(),
                strategy.getFailureCount(),
                strategy.getSuccessCount(),
                strategy.getSuccessRate()
        );
    }

    /**
     * 服务健康状态记录.
     *
     * @param degraded 是否降级
     * @param failureCount 失败次数
     * @param successCount 成功次数
     * @param successRate 成功率
     */
    public static record ServiceHealth(
        boolean degraded,
        int failureCount,
        int successCount,
        double successRate
    ) {}
}
