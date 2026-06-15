/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomHealthIndicatorTest {

    private CustomHealthIndicator healthIndicator;
    private MonitoringProperties monitoringProperties;

    @BeforeEach
    void setUp() {
        monitoringProperties = new MonitoringProperties();
        monitoringProperties.setHealthCheck(new MonitoringProperties.HealthCheckConfig());
        monitoringProperties.getHealthCheck().setTimeoutSeconds(5);
        monitoringProperties.getHealthCheck().setMemoryThreshold(90.0);
        monitoringProperties.getHealthCheck().setDiskThreshold(90.0);
        monitoringProperties.getHealthCheck().setCpuThreshold(90.0);
        healthIndicator = new CustomHealthIndicator(monitoringProperties);
    }

    @Test
    void testHealthReturnsUp() {
        CustomHealthIndicator.HealthStatus health = healthIndicator.health();
        assertNotNull(health);
        assertEquals("UP", health.getStatus());
    }

    @Test
    void testHealthContainsDetails() {
        CustomHealthIndicator.HealthStatus health = healthIndicator.health();
        assertNotNull(health.getDetails());
    }

    @Test
    void testHealthStatusIsUp() {
        CustomHealthIndicator.HealthStatus health = healthIndicator.health();
        assertTrue(health.isUp());
    }
}
