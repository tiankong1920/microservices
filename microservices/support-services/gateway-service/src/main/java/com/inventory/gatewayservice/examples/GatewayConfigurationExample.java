package com.inventory.gatewayservice.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Slf4j
@Configuration
public class GatewayConfigurationExample {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("product-service", r -> r
                .path("/api/products/**")
                .and()
                .method(HttpMethod.GET, HttpMethod.POST)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-From", "gateway")
                    .addResponseHeader("X-Response-From", "gateway")
                    .retry(3)
                    .circuitBreaker(c -> c
                        .setName("product-service-cb")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("lb://product-service")
            )
            .route("order-service", r -> r
                .path("/api/orders/**")
                .and()
                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-From", "gateway")
                    .circuitBreaker(c -> c
                        .setName("order-service-cb")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("lb://order-service")
            )
            .route("inventory-service", r -> r
                .path("/api/inventory/**")
                .and()
                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-From", "gateway")
                    .circuitBreaker(c -> c
                        .setName("inventory-service-cb")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("lb://inventory-service")
            )
            .route("customer-service", r -> r
                .path("/api/customers/**")
                .and()
                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-From", "gateway")
                    .circuitBreaker(c -> c
                        .setName("customer-service-cb")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("lb://customer-service")
            )
            .route("fallback", r -> r
                .path("/fallback/**")
                .filters(f -> f
                    .setResponseHeader("Content-Type", "application/json")
                    .rewritePath("/fallback/(?<segment>.*)", "/${segment}")
                )
                .uri("lb://fallback-service")
            )
            .build();
    }

    public void routingConfigurationExample() {
        log.info("=== Routing Configuration Example ===");

        log.info("Route 1: /api/products/** -> product-service");
        log.info("Route 2: /api/orders/** -> order-service");
        log.info("Route 3: /api/inventory/** -> inventory-service");
        log.info("Route 4: /api/customers/** -> customer-service");
        log.info("Route 5: /fallback/** -> fallback-service");
    }

    public void rateLimitingConfigurationExample() {
        log.info("=== Rate Limiting Configuration Example ===");

        log.info("Product service: 100 requests/second, burst 200");
        log.info("Order service: 50 requests/second, burst 100");
        log.info("Inventory service: 75 requests/second, burst 150");
        log.info("Customer service: 30 requests/second, burst 60");
    }

    public void circuitBreakerConfigurationExample() {
        log.info("=== Circuit Breaker Configuration Example ===");

        log.info("Circuit breaker enabled for all services");
        log.info("Fallback URI: /fallback");
        log.info("Circuit breaker will open after 5 consecutive failures");
        log.info("Circuit breaker will half-open after 30 seconds");
        log.info("Circuit breaker will close after 3 consecutive successes");
    }

    public void loadBalancingConfigurationExample() {
        log.info("=== Load Balancing Configuration Example ===");

        log.info("Load balancing strategy: Round Robin");
        log.info("Health check interval: 10 seconds");
        log.info("Health check timeout: 5 seconds");
        log.info("Retry attempts: 3");
        log.info("Retry backoff: exponential");
    }

    public void securityConfigurationExample() {
        log.info("=== Security Configuration Example ===");

        log.info("API key filter enabled");
        log.info("Authorization filter enabled");
        log.info("IP filter enabled");
        log.info("Audit log filter enabled");
    }

    public void retryConfigurationExample() {
        log.info("=== Retry Configuration Example ===");

        log.info("Retry attempts: 3");
        log.info("Retry backoff: exponential");
        log.info("Retry methods: GET, POST, PUT, DELETE");
        log.info("Retry status codes: 500, 502, 503, 504");
    }

    public void timeoutConfigurationExample() {
        log.info("=== Timeout Configuration Example ===");

        log.info("Connect timeout: 5 seconds");
        log.info("Read timeout: 10 seconds");
        log.info("Write timeout: 10 seconds");
        log.info("Response timeout: 30 seconds");
    }

    public void corsConfigurationExample() {
        log.info("=== CORS Configuration Example ===");

        log.info("Allowed origins: *");
        log.info("Allowed methods: GET, POST, PUT, DELETE, OPTIONS");
        log.info("Allowed headers: *");
        log.info("Exposed headers: X-Response-From");
        log.info("Max age: 3600 seconds");
    }

    public void loggingConfigurationExample() {
        log.info("=== Logging Configuration Example ===");

        log.info("Request logging enabled");
        log.info("Response logging enabled");
        log.info("Error logging enabled");
        log.info("Performance logging enabled");
        log.info("Log level: INFO");
    }

    public void metricsConfigurationExample() {
        log.info("=== Metrics Configuration Example ===");

        log.info("Request metrics enabled");
        log.info("Response time metrics enabled");
        log.info("Error rate metrics enabled");
        log.info("Circuit breaker metrics enabled");
        log.info("Rate limiter metrics enabled");
    }

    public void runAllExamples() {
        log.info("Starting Gateway Configuration Examples...");

        routingConfigurationExample();
        rateLimitingConfigurationExample();
        circuitBreakerConfigurationExample();
        loadBalancingConfigurationExample();
        securityConfigurationExample();
        retryConfigurationExample();
        timeoutConfigurationExample();
        corsConfigurationExample();
        loggingConfigurationExample();
        metricsConfigurationExample();

        log.info("Gateway Configuration Examples Completed!");
    }
}
