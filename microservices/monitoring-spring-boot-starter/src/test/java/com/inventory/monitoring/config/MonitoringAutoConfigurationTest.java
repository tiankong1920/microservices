/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitoringAutoConfigurationTest {

    private MonitoringAutoConfiguration monitoringAutoConfiguration;
    private MeterRegistry meterRegistry;
    private MonitoringMetricsCollector collector;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        monitoringAutoConfiguration = new MonitoringAutoConfiguration();
        collector = monitoringAutoConfiguration.monitoringMetricsCollector(meterRegistry);
    }
    
    @AfterEach
    void tearDown() {
        if (collector != null) {
            collector.shutdown();
        }
    }

    @Test
    void testBusinessExceptionCounter() {
        final var result = monitoringAutoConfiguration.businessExceptionCounter(meterRegistry);
        assertNotNull(result);
    }

    @Test
    void testSystemExceptionCounter() {
        final var result = monitoringAutoConfiguration.systemExceptionCounter(meterRegistry);
        assertNotNull(result);
    }

    @Test
    void testRequestProcessingTimer() {
        final var result = monitoringAutoConfiguration.requestProcessingTimer(meterRegistry);
        assertNotNull(result);
    }

    @Test
    void testCacheOperationTimer() {
        final var result = monitoringAutoConfiguration.cacheOperationTimer(meterRegistry);
        assertNotNull(result);
    }

    @Test
    void testMessageProcessingTimer() {
        final var result = monitoringAutoConfiguration.messageProcessingTimer(meterRegistry);
        assertNotNull(result);
    }

    @Test
    void testMonitoringMetricsCollector() {
        assertNotNull(collector);
    }
}
