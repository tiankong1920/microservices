package com.inventory.common.resilience;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import org.springframework.stereotype.Component;

/**
 * 自适应限流器.
 *
 * <p>根据错误率和延迟动态调整限流阈值，实现自适应流量控制。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@SuppressWarnings("null")
public class AdaptiveRateLimiter {

    /** 限流器注册表. */
    private final RateLimiterRegistry registry;

    /** 限流器映射表. */
    private final ConcurrentMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /** 自适应配置映射表. */
    private final ConcurrentMap<String, AdaptiveConfig> adaptiveConfigs = new ConcurrentHashMap<>();

    /**
     * 构造函数.
     */
    public AdaptiveRateLimiter() {
        final RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        this.registry = RateLimiterRegistry.of(defaultConfig);
    }

    /**
     * 获取限流器.
     *
     * @param name 限流器名称
     * @return 限流器实例
     */
    public RateLimiter getRateLimiter(final String name) {
        return rateLimiters.computeIfAbsent(name,
                key -> registry.rateLimiter(key));
    }

    /**
     * 获取限流器.
     *
     * @param name 限流器名称
     * @param limitForPeriod 周期内限制数
     * @param limitRefreshPeriod 刷新周期
     * @return 限流器实例
     */
    public RateLimiter getRateLimiter(final String name,
            final int limitForPeriod, final Duration limitRefreshPeriod) {
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        return rateLimiters.computeIfAbsent(name,
                key -> registry.rateLimiter(key, config));
    }

    /**
     * 配置自适应限流.
     *
     * @param name 限流器名称
     * @param initialLimit 初始限制
     * @param minLimit 最小限制
     * @param maxLimit 最大限制
     */
    public void configureAdaptive(final String name,
            final int initialLimit, final int minLimit, final int maxLimit) {
        final AdaptiveConfig config = new AdaptiveConfig(initialLimit, minLimit, maxLimit);
        adaptiveConfigs.put(name, config);

        final RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(initialLimit)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        rateLimiters.put(name, registry.rateLimiter(name, rateLimiterConfig));
    }

    /**
     * 调整限制.
     *
     * @param name 限流器名称
     * @param errorRate 错误率
     * @param latencyMs 延迟（毫秒）
     */
    public void adjustLimit(final String name, final double errorRate, final double latencyMs) {
        final AdaptiveConfig config = adaptiveConfigs.get(name);
        if (config == null) {
            return;
        }

        final int currentLimit = config.currentLimit();
        int newLimit = currentLimit;

        if (errorRate > 0.05 || latencyMs > 1000) {
            newLimit = Math.max(config.minLimit(), (int) (currentLimit * 0.8));
        } else if (errorRate < 0.01 && latencyMs < 200) {
            newLimit = Math.min(config.maxLimit(), (int) (currentLimit * 1.2));
        }

        if (newLimit != currentLimit) {
            config.setCurrentLimit(newLimit);
            final RateLimiter rateLimiter = rateLimiters.get(name);
            if (rateLimiter != null) {
                rateLimiter.changeLimitForPeriod(newLimit);
            }
        }
    }

    /**
     * 获取限流器指标.
     *
     * @param name 限流器名称
     * @return 限流器指标
     */
    public RateLimiterMetrics getMetrics(final String name) {
        final RateLimiter rl = rateLimiters.get(name);
        if (rl == null) {
            return null;
        }

        final var metrics = rl.getMetrics();
        return new RateLimiterMetrics(
                metrics.getAvailablePermissions(),
                metrics.getNumberOfWaitingThreads(),
                rl.getRateLimiterConfig().getLimitForPeriod()
        );
    }

    /**
     * 尝试获取许可.
     *
     * @param name 限流器名称
     * @return 是否获取成功
     */
    public boolean tryAcquire(final String name) {
        final RateLimiter rl = rateLimiters.get(name);
        return rl == null || rl.acquirePermission();
    }

    /**
     * 限流器指标记录.
     *
     * @param availablePermissions 可用许可数
     * @param numberOfWaitingThreads 等待线程数
     * @param limitForPeriod 周期限制数
     */
    public static record RateLimiterMetrics(
        int availablePermissions,
        int numberOfWaitingThreads,
        int limitForPeriod
    ) {}

    /**
     * 自适应配置.
     */
    private static class AdaptiveConfig {

        /** 当前限制. */
        private volatile int currentLimit;

        /** 最小限制. */
        private final int minLimit;

        /** 最大限制. */
        private final int maxLimit;

        /**
         * 构造函数.
         *
         * @param initialLimit 初始限制
         * @param minLimit 最小限制
         * @param maxLimit 最大限制
         */
        AdaptiveConfig(final int initialLimit, final int minLimit, final int maxLimit) {
            this.currentLimit = initialLimit;
            this.minLimit = minLimit;
            this.maxLimit = maxLimit;
        }

        /**
         * 获取当前限制.
         *
         * @return 当前限制
         */
        int currentLimit() {
            return currentLimit;
        }

        /**
         * 获取最小限制.
         *
         * @return 最小限制
         */
        int minLimit() {
            return minLimit;
        }

        /**
         * 获取最大限制.
         *
         * @return 最大限制
         */
        int maxLimit() {
            return maxLimit;
        }

        /**
         * 设置当前限制.
         *
         * @param limit 当前限制
         */
        void setCurrentLimit(final int limit) {
            this.currentLimit = limit;
        }
    }
}
