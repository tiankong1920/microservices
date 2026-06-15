package com.inventory.common.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

/**
 * Resilience4j统一配置类，用于规范化跨服务调用的容错策略.
 * 提供统一的熔断器、重试器和时间限制器配置，确保所有服务使用相同的容错标准.
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "resilience4j.enabled", havingValue = "true")
public class Resilience4jConfig {

    /** 默认失败率阈值百分比. */
    private static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50;

    /** 默认等待时间（秒）. */
    private static final int DEFAULT_WAIT_DURATION_SECONDS = 30;

    /** 默认半开状态允许调用数. */
    private static final int DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN = 10;

    /** 默认滑动窗口大小. */
    private static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;

    /** 默认最小调用数. */
    private static final int DEFAULT_MINIMUM_CALLS = 10;

    /** 默认慢调用持续时间阈值（秒）. */
    private static final int DEFAULT_SLOW_CALL_DURATION_SECONDS = 5;

    /** 产品服务失败率阈值百分比. */
    private static final int PRODUCT_FAILURE_RATE_THRESHOLD = 30;

    /** 产品服务等待时间（秒）. */
    private static final int PRODUCT_WAIT_DURATION_SECONDS = 15;

    /** 产品服务半开状态允许调用数. */
    private static final int PRODUCT_PERMITTED_CALLS_IN_HALF_OPEN = 5;

    /** 产品服务滑动窗口大小. */
    private static final int PRODUCT_SLIDING_WINDOW_SIZE = 50;

    /** 产品服务最小调用数. */
    private static final int PRODUCT_MINIMUM_CALLS = 5;

    /** 产品服务慢调用持续时间阈值（秒）. */
    private static final int PRODUCT_SLOW_CALL_DURATION_SECONDS = 3;

    /** 默认最大重试次数. */
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    /** 默认重试等待时间（毫秒）. */
    private static final int DEFAULT_RETRY_WAIT_DURATION_MS = 500;

    /** 默认超时时间（秒）. */
    private static final int DEFAULT_TIMEOUT_SECONDS = 3;

    /**
     * 默认熔断器配置，设置失败阈值和恢复策略.
     *
     * @return 熔断器配置
     */
    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(DEFAULT_FAILURE_RATE_THRESHOLD)
                .waitDurationInOpenState(Duration.ofSeconds(DEFAULT_WAIT_DURATION_SECONDS))
                .permittedNumberOfCallsInHalfOpenState(DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(DEFAULT_SLIDING_WINDOW_SIZE)
                .minimumNumberOfCalls(DEFAULT_MINIMUM_CALLS)
                .slowCallDurationThreshold(Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_SECONDS))
                .build();
    }

    /**
     * 产品服务专用熔断器配置，更严格的阈值.
     *
     * @return 产品服务熔断器配置
     */
    @Bean("productServiceCircuitBreaker")
    public CircuitBreakerConfig productServiceCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(PRODUCT_FAILURE_RATE_THRESHOLD)
                .waitDurationInOpenState(Duration.ofSeconds(PRODUCT_WAIT_DURATION_SECONDS))
                .permittedNumberOfCallsInHalfOpenState(PRODUCT_PERMITTED_CALLS_IN_HALF_OPEN)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(PRODUCT_SLIDING_WINDOW_SIZE)
                .minimumNumberOfCalls(PRODUCT_MINIMUM_CALLS)
                .slowCallDurationThreshold(Duration.ofSeconds(PRODUCT_SLOW_CALL_DURATION_SECONDS))
                .build();
    }

    /**
     * 默认重试配置，设置最大重试次数和退避策略.
     *
     * @return 重试配置
     */
    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(DEFAULT_MAX_ATTEMPTS)
                .waitDuration(Duration.ofMillis(DEFAULT_RETRY_WAIT_DURATION_MS))
                .retryExceptions(Exception.class)
                .build();
    }

    /**
     * 默认时间限制器配置，限制并发调用频率.
     *
     * @return 时间限制器配置
     */
    @Bean
    public TimeLimiterConfig defaultTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * 创建通用熔断器实例.
     *
     * @param name 熔断器名称
     * @return 熔断器实例
     */
    @Bean
    public CircuitBreaker circuitBreaker(String name) {
        return CircuitBreaker.of(name, defaultCircuitBreakerConfig());
    }

    /**
     * 创建通用重试器实例.
     *
     * @param name 重试器名称
     * @return 重试器实例
     */
    @Bean
    public Retry retry(String name) {
        return Retry.of(name, defaultRetryConfig());
    }

    /**
     * 创建通用时间限制器实例.
     *
     * @param name 时间限制器名称
     * @return 时间限制器实例
     */
    @Bean
    public TimeLimiter timeLimiter(String name) {
        return TimeLimiter.of(name, defaultTimeLimiterConfig());
    }
}
