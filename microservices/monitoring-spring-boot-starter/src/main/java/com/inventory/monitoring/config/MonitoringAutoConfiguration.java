/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 监控自动配置类，用于配置Spring Boot Actuator和Micrometer集成。.
 * <p>.
 * 该类自动配置监控相关的Bean，包括指标收集、健康检查和监控端点。.
 * </p>.
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@Import({
        MonitoringProperties.class,
        CustomHealthIndicator.class,
        ElasticsearchConfig.class
})
public class MonitoringAutoConfiguration {

    /**
 * 创建业务异常计数器Bean，用于统计业务异常的发生次数。.
     *
 * @param registry 指标注册表
 * @return 业务异常计数器
     */
    @Bean
    @ConditionalOnMissingBean(name = "businessExceptionCounter")
    public Counter businessExceptionCounter(final MeterRegistry registry) {
        return Counter.builder("business.exception.count")
                .tag("type", "business")
                .description("Count of business exceptions")
                .register(registry);
    }

    /**
 * 创建系统异常计数器Bean，用于统计系统异常的发生次数。.
     *
 * @param registry 指标注册表
 * @return 系统异常计数器
     */
    @Bean
    @ConditionalOnMissingBean(name = "systemExceptionCounter")
    public Counter systemExceptionCounter(final MeterRegistry registry) {
        return Counter.builder("system.exception.count")
                .tag("type", "system")
                .description("Count of system exceptions")
                .register(registry);
    }

    /**
 * 创建请求处理计时器Bean，用于统计请求处理的耗时。.
     *
 * @param registry 指标注册表
 * @return 请求处理计时器
     */
    @Bean
    @ConditionalOnMissingBean(name = "requestProcessingTimer")
    public Timer requestProcessingTimer(final MeterRegistry registry) {
        return Timer.builder("request.processing.time")
                .tag("type", "http")
                .description("Request processing time")
                .register(registry);
    }

    /**
 * 创建缓存操作计时器Bean，用于统计缓存操作的耗时。.
     *
 * @param registry 指标注册表
 * @return 缓存操作计时器
     */
    @Bean
    @ConditionalOnMissingBean(name = "cacheOperationTimer")
    public Timer cacheOperationTimer(final MeterRegistry registry) {
        return Timer.builder("cache.operation.time")
                .tag("type", "cache")
                .description("Cache operation time")
                .register(registry);
    }

    /**
 * 创建消息处理计时器Bean，用于统计消息处理的耗时。.
     *
 * @param registry 指标注册表
 * @return 消息处理计时器
     */
    @Bean
    @ConditionalOnMissingBean(name = "messageProcessingTimer")
    public Timer messageProcessingTimer(final MeterRegistry registry) {
        return Timer.builder("message.processing.time")
                .tag("type", "message")
                .description("Message processing time")
                .register(registry);
    }

    /**
 * 创建监控指标收集器Bean，用于收集自定义监控指标。.
     *
 * @param registry 指标注册表
 * @return 监控指标收集器
     */
    @Bean
    @ConditionalOnMissingBean(MonitoringMetricsCollector.class)
    public MonitoringMetricsCollector monitoringMetricsCollector(final MeterRegistry registry) {
        return new MonitoringMetricsCollector(registry);
    }
}
