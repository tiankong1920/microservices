package com.inventory.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.core.ErrorResponse;
import com.inventory.gatewayservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthorizationFilter implements GatewayFilter, Ordered {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
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
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "AUTH-ERR-001",
                    "Missing or invalid authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!authService.validateToken(token)) {
            log.warn("Invalid token for request: {}", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "AUTH-ERR-002",
                    "Invalid token");
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
        return WHITELISTED_PATHS.contains(path);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
                                           String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ErrorResponse error = ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(Map.of("path", exchange.getRequest().getPath().value()))
                .timestamp(LocalDateTime.now())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(error);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
