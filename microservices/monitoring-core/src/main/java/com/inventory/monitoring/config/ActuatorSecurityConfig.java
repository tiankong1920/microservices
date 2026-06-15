package com.inventory.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;
/**
 * Actuator security configuration to protect sensitive endpoints
 * while keeping health/info endpoints publicly accessible.
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class ActuatorSecurityConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(final HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/prometheus").hasRole("MONITORING")
                .requestMatchers("/actuator/**").authenticated()
            )
            .httpBasic(basic -> {});
        return http.build();
    }
}
