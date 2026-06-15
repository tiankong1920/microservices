package com.inventory.common.resilience;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

/**
 * 弹性端点.
 *
 * <p>提供熔断器、限流器和降级管理器的监控端点。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Component
@Endpoint(id = "resilience")
@SuppressWarnings("null")
public class ResilienceEndpoint {

    /** 熔断器管理器. */
    private final CircuitBreakerManager circuitBreakerManager;

    /** 限流器. */
    private final AdaptiveRateLimiter rateLimiter;

    /** 降级管理器. */
    private final FallbackManager fallbackManager;

    /**
     * 构造函数.
     *
     * @param circuitBreakerManager 熔断器管理器
     * @param rateLimiter 限流器
     * @param fallbackManager 降级管理器
     */
    public ResilienceEndpoint(
            final CircuitBreakerManager circuitBreakerManager,
            final AdaptiveRateLimiter rateLimiter,
            final FallbackManager fallbackManager) {
        this.circuitBreakerManager = circuitBreakerManager;
        this.rateLimiter = rateLimiter;
        this.fallbackManager = fallbackManager;
    }

    /**
     * 获取概览信息.
     *
     * @return 概览信息
     */
    @ReadOperation
    public Map<String, Object> overview() {
        final Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("circuitBreakers", new HashMap<>());
        result.put("rateLimiters", new HashMap<>());
        result.put("fallbacks", new HashMap<>());
        return result;
    }

    /**
     * 获取熔断器信息.
     *
     * @param name 熔断器名称
     * @return 熔断器信息
     */
    @ReadOperation
    public Map<String, Object> circuitBreaker(@Selector final String name) {
        final Map<String, Object> result = new HashMap<>();
        final CircuitBreakerManager.CircuitBreakerMetrics metrics =
                circuitBreakerManager.getMetrics(name);
        if (metrics == null) {
            return result;
        }
        result.put("name", name);
        result.put("state", metrics.state().name());
        result.put("failureRate", metrics.failureRate());
        result.put("slowCallRate", metrics.slowCallRate());
        result.put("bufferedCalls", metrics.bufferedCalls());
        result.put("failedCalls", metrics.failedCalls());
        result.put("slowCalls", metrics.slowCalls());
        result.put("successfulCalls", metrics.successfulCalls());
        return result;
    }

    /**
     * 重置熔断器.
     *
     * @param name 熔断器名称
     */
    @WriteOperation
    public void resetCircuitBreaker(@Selector final String name) {
        circuitBreakerManager.reset(name);
    }

    /**
     * 获取限流器信息.
     *
     * @param name 限流器名称
     * @return 限流器信息
     */
    @ReadOperation
    public Map<String, Object> rateLimiter(@Selector final String name) {
        final Map<String, Object> result = new HashMap<>();
        final AdaptiveRateLimiter.RateLimiterMetrics metrics = rateLimiter.getMetrics(name);
        if (metrics == null) {
            return result;
        }
        result.put("name", name);
        result.put("availablePermissions", metrics.availablePermissions());
        result.put("waitingThreads", metrics.numberOfWaitingThreads());
        result.put("limitForPeriod", metrics.limitForPeriod());
        return result;
    }

    /**
     * 获取降级信息.
     *
     * @param name 服务名称
     * @return 降级信息
     */
    @ReadOperation
    public Map<String, Object> fallback(@Selector final String name) {
        final Map<String, Object> result = new HashMap<>();
        final FallbackManager.ServiceHealth health = fallbackManager.getHealth(name);
        if (health == null) {
            return result;
        }
        result.put("name", name);
        result.put("degraded", health.degraded());
        result.put("failureCount", health.failureCount());
        result.put("successCount", health.successCount());
        result.put("successRate", health.successRate());
        return result;
    }
}
