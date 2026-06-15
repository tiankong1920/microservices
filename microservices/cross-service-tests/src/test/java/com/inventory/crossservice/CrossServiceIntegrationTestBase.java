package com.inventory.crossservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class CrossServiceIntegrationTestBase {

    @BeforeEach
    void setup() {
        // Common setup logic for all tests
    }
}
