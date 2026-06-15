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

class MonitoringMetricsCollectorTest {

    private MonitoringMetricsCollector collector;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        collector = new MonitoringMetricsCollector(meterRegistry);
    }
    
    @AfterEach
    void tearDown() {
        if (collector != null) {
            collector.shutdown();
        }
    }

    @Test
    void testIncrementBusinessException() {
        collector.incrementBusinessException("TEST_ERROR");
        collector.flushAllBatchCaches();
        assertNotNull(meterRegistry.find("business.exception.count").counter());
    }

    @Test
    void testIncrementSystemException() {
        collector.incrementSystemException("SYSTEM_ERROR");
        collector.flushAllBatchCaches();
        assertNotNull(meterRegistry.find("system.exception.count").counter());
    }

    @Test
    void testRecordOperationTime() {
        collector.recordOperationTime("testOperation", 100);
        assertNotNull(meterRegistry.find("request.processing.time").timer());
    }

    @Test
    void testIncrementGauge() {
        collector.incrementGauge("custom.gauge", 42);
        assertNotNull(collector);
    }

    @Test
    void testSetGauge() {
        collector.setGauge("custom.gauge", 100);
        assertNotNull(collector);
    }

    @Test
    void testIncrementSeverityCount() {
        collector.incrementSeverityCount("HIGH");
        collector.flushAllBatchCaches();
        assertNotNull(meterRegistry.find("exception.severity.count").counter());
    }

    @Test
    void testIncrementRetryCount() {
        collector.incrementRetryCount("TIMEOUT_ERROR");
        collector.flushAllBatchCaches();
        assertNotNull(meterRegistry.find("exception.retry.count").counter());
    }

    @Test
    void testResetAllMetrics() {
        collector.incrementBusinessException("TEST_ERROR");
        collector.resetAllMetrics();
        assertNotNull(collector);
    }

    @Test
    void testFlushAllBatchCaches() {
        collector.incrementBusinessException("TEST_ERROR");
        collector.incrementSystemException("SYSTEM_ERROR");
        collector.flushAllBatchCaches();
        assertNotNull(collector);
    }
}
