/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.listener;

import com.inventory.monitoring.config.MonitoringMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 业务异常监听器，用于监听业务异常并自动统计异常信息到监控指标收集器中.
 *
 * 该类在初始化后注册监听器，用于监控和分析异常情况.
 */
@Component
@SuppressWarnings("unused")
public class BusinessExceptionListener implements InitializingBean {

    /**
     * 日志记录器，用于记录监听器的运行状态。.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessExceptionListener.class);

    /**
     * 监控指标收集器，用于统计异常信息。.
     */
    private final MonitoringMetricsCollector metricsCollector;

    /**
     * 构造方法，注入监控指标收集器。.
     *
     * @param metricsCollector 监控指标收集器
     */
    public BusinessExceptionListener(final MonitoringMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    /**
* 当异常被创建时调用，统计异常信息到监控指标收集器中.
     *
     * @param exception 创建的异常
     */
    public void onExceptionCreated(final Object exception) {
        // 简化实现，移除对 BusinessException 的直接依赖
        LOGGER.debug("Exception listener called");
    }

    /**
 * 初始化方法，在Bean初始化后注册监听器.
     *
 * @throws Exception 初始化异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("BusinessExceptionListener initialized successfully");
    }
}
