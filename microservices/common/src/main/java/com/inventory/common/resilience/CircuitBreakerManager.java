package com.inventory.common.resilience;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.stereotype.Component;

/**
 * 熔断器管理器.
 *
 * <p>用于管理和监控熔断器实例，提供熔断器的创建、配置和状态查询功能。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@SuppressWarnings("null")
public class CircuitBreakerManager {

    /** 熔断器注册表. */
    private final CircuitBreakerRegistry registry;

    /** 熔断器映射表. */
    private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * 构造函数.
     */
    public CircuitBreakerManager() {
        final CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(100)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .minimumNumberOfCalls(20)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        this.registry = CircuitBreakerRegistry.of(defaultConfig);
    }

    /**
     * 获取熔断器.
     *
     * @param name 熔断器名称
     * @return 熔断器实例
     */
    public CircuitBreaker getCircuitBreaker(final String name) {
        return circuitBreakers.computeIfAbsent(name,
                key -> registry.circuitBreaker(key));
    }

    /**
     * 获取熔断器.
     *
     * @param name 熔断器名称
     * @param config 熔断器配置
     * @return 熔断器实例
     */
    public CircuitBreaker getCircuitBreaker(final String name, final CircuitBreakerConfig config) {
        return circuitBreakers.computeIfAbsent(name,
                key -> registry.circuitBreaker(key, config));
    }

    /**
     * 创建配置构建器.
     *
     * @return 配置构建器
     */
    public CircuitBreakerConfig.Builder createConfigBuilder() {
        return CircuitBreakerConfig.custom();
    }

    /**
     * 获取熔断器状态.
     *
     * @param name 熔断器名称
     * @return 熔断器状态
     */
    public CircuitBreaker.State getState(final String name) {
        final CircuitBreaker cb = circuitBreakers.get(name);
        return cb != null ? cb.getState() : null;
    }

    /**
     * 获取熔断器指标.
     *
     * @param name 熔断器名称
     * @return 熔断器指标
     */
    public CircuitBreakerMetrics getMetrics(final String name) {
        final CircuitBreaker cb = circuitBreakers.get(name);
        if (cb == null) {
            return null;
        }

        final var metrics = cb.getMetrics();
        return new CircuitBreakerMetrics(
                cb.getState(),
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfBufferedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls(),
                metrics.getNumberOfSuccessfulCalls()
        );
    }

    /**
     * 重置熔断器.
     *
     * @param name 熔断器名称
     */
    public void reset(final String name) {
        final CircuitBreaker cb = circuitBreakers.get(name);
        if (cb != null) {
            cb.reset();
        }
    }

    /**
     * 转换到打开状态.
     *
     * @param name 熔断器名称
     */
    public void transitionToOpenState(final String name) {
        final CircuitBreaker cb = circuitBreakers.get(name);
        if (cb != null) {
            cb.transitionToOpenState();
        }
    }

    /**
     * 转换到关闭状态.
     *
     * @param name 熔断器名称
     */
    public void transitionToClosedState(final String name) {
        final CircuitBreaker cb = circuitBreakers.get(name);
        if (cb != null) {
            cb.transitionToClosedState();
        }
    }

    /**
     * 熔断器指标记录.
     *
     * @param state 状态
     * @param failureRate 失败率
     * @param slowCallRate 慢调用率
     * @param bufferedCalls 缓冲调用数
     * @param failedCalls 失败调用数
     * @param slowCalls 慢调用数
     * @param successfulCalls 成功调用数
     */
    public static record CircuitBreakerMetrics(
        CircuitBreaker.State state,
        float failureRate,
        float slowCallRate,
        int bufferedCalls,
        int failedCalls,
        int slowCalls,
        int successfulCalls
    ) {}
}
