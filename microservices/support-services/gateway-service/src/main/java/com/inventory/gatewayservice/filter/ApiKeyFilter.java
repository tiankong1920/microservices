package com.inventory.gatewayservice.filter;

import com.inventory.gatewayservice.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ApiKeyFilter implements GatewayFilter, Ordered {

    private final ApiKeyService apiKeyService;
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_QUERY_PARAM = "api_key";
    private static final int ORDER = 100;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean isHttps = "https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme());

        if (!isHttps) {
            log.warn("API key request over non-HTTPS connection from: {} - blocking for security", exchange.getRequest().getRemoteAddress());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            byte[] bytes = "{\"error\": \"API key must be transmitted over HTTPS\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        String apiKey = extractApiKey(exchange);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API key missing from request: {}", exchange.getRequest().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            byte[] bytes = "{\"error\": \"API key is required\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        if (!apiKeyService.isValidApiKey(apiKey)) {
            log.warn("Invalid API key from request: {}", exchange.getRequest().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            byte[] bytes = "{\"error\": \"Invalid API key\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        log.info("API key validated successfully for request: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }

    private String extractApiKey(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String apiKey = headers.getFirst(API_KEY_HEADER);

        if (apiKey == null) {
            apiKey = exchange.getRequest().getQueryParams().getFirst(API_KEY_QUERY_PARAM);
        }

        return apiKey;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
