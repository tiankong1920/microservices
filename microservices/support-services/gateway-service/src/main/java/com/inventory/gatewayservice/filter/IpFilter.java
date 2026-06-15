package com.inventory.gatewayservice.filter;

import com.inventory.gatewayservice.config.IpFilterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IpFilter implements GlobalFilter, Ordered {

    private final IpFilterConfig ipFilterConfig;

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    private boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    private boolean isInWhitelist(String ip) {
        List<String> whitelist = ipFilterConfig.getWhitelist();
        return whitelist != null && !whitelist.isEmpty() && whitelist.contains(ip);
    }

    private boolean isInBlacklist(String ip) {
        List<String> blacklist = ipFilterConfig.getBlacklist();
        return blacklist != null && !blacklist.isEmpty() && blacklist.contains(ip);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!ipFilterConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);
        String path = request.getPath().value();

        if (ipFilterConfig.isAllowLocalhost() && isLocalhost(clientIp)) {
            logIfEnabled("IP Filter: Allowed localhost IP [{}] to access path [{}]", clientIp, path);
            return chain.filter(exchange);
        }

        return processIpFilter(exchange, chain, clientIp, path);
    }

    private Mono<Void> processIpFilter(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String clientIp,
            String path
    ) {
        IpFilterResult result = evaluateIpAccess(clientIp);

        if (!result.allowed()) {
            logIfEnabled(
                "IP Filter: Blocked IP [{}] from accessing path [{}], reason: {}",
                clientIp, path, result.reason()
            );
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        logIfEnabled("IP Filter: Allowed IP [{}] to access path [{}]", clientIp, path);
        return chain.filter(exchange);
    }

    private IpFilterResult evaluateIpAccess(String clientIp) {
        String strategy = ipFilterConfig.getStrategy();

        if ("whitelist".equals(strategy)) {
            boolean inWhitelist = isInWhitelist(clientIp);
            return new IpFilterResult(inWhitelist, inWhitelist ? "" : "IP not in whitelist");
        }

        if ("blacklist".equals(strategy)) {
            boolean inBlacklist = isInBlacklist(clientIp);
            return new IpFilterResult(!inBlacklist, inBlacklist ? "IP in blacklist" : "");
        }

        return new IpFilterResult(true, "");
    }

    private void logIfEnabled(String message, Object... args) {
        if (ipFilterConfig.isLogEnabled()) {
            log.info(message, args);
        }
    }

    private record IpFilterResult(boolean allowed, String reason) { }

    @Override
    public int getOrder() {
        return -100;
    }
}
