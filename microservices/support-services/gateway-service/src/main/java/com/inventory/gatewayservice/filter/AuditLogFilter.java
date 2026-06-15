package com.inventory.gatewayservice.filter;

import com.inventory.gatewayservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuditLogFilter implements GatewayFilter, Ordered {

    private final AuditLogService auditLogService;
    private static final int ORDER = 200;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();

        return chain.filter(exchange).doOnSuccess(aVoid -> {
            Instant endTime = Instant.now();
            long duration = Duration.between(startTime, endTime).toMillis();

            AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .timestamp(startTime)
                .method(exchange.getRequest().getMethod().name())
                .path(exchange.getRequest().getPath().value())
                .statusCode(getStatusCode(exchange))
                .duration(duration)
                .clientIp(getClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .apiKey(exchange.getRequest().getHeaders().getFirst("X-API-Key"))
                .build();

            auditLogService.log(entry);
        }).doOnError(throwable -> {
            Instant endTime = Instant.now();
            long duration = Duration.between(startTime, endTime).toMillis();

            AuditLogService.AuditLogEntry entry = AuditLogService.AuditLogEntry.builder()
                .timestamp(startTime)
                .method(exchange.getRequest().getMethod().name())
                .path(exchange.getRequest().getPath().value())
                .statusCode(500)
                .duration(duration)
                .clientIp(getClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .apiKey(exchange.getRequest().getHeaders().getFirst("X-API-Key"))
                .error(throwable.getMessage())
                .build();

            auditLogService.log(entry);
        });
    }

    private int getStatusCode(ServerWebExchange exchange) {
        var statusCode = exchange.getResponse().getStatusCode();
        return statusCode != null ? statusCode.value() : 0;
    }

    private String getClientIp(ServerWebExchange exchange) {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
