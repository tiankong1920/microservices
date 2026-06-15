package com.inventory.templateservice;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
public class TestSecurityConfig {
}
