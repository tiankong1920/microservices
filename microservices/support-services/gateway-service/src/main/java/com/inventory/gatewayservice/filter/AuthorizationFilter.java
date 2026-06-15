package com.inventory.gatewayservice.filter;

import com.inventory.gatewayservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthorizationFilter implements GatewayFilter, Ordered {

    private final AuthService authService;
    private static final Set<String> WHITELISTED_PATHS = Set.of(
        "/api/public",
        "/api/health",
        "/api/actuator/health",
        "/api/v1/admin/auth/login",
        "/api/v1/admin/auth/register",
        "/oauth/token",
        "/oauth2/token",
        "/oauth2/authorize",
        "/.well-known",
        "/login",
        "/register"
    );
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int ORDER = 150;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isWhitelistedPath(path)) {
            log.debug("Skipping authorization for whitelisted path: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid authorization header for request: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            byte[] bytes = "{\"error\": \"Missing or invalid authorization header\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!authService.validateToken(token)) {
            log.warn("Invalid token for request: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            byte[] bytes = "{\"error\": \"Invalid token\"}".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        String username = authService.getUsernameFromToken(token);
        var roles = authService.getRolesFromToken(token);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-ID", username)
                .header("X-User-Roles", String.join(",", roles))
                .build();

        log.info("Authorization successful for user: {} on path: {}", username, path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isWhitelistedPath(String path) {
        return WHITELISTED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
