package com.inventory.gatewayservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
@SuppressWarnings("null")
public class GatewayConfig {

    /**
 * Rate limiting strategy based on API path.
     */
    @Bean("apiKeyResolver")
    @Primary
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
 * Rate limiting strategy based on IP address.
     */
    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
 * Rate limiting strategy based on user.
     */
    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
 * Rate limiting strategy based on API path and IP address.
     */
    @Bean("apiIpKeyResolver")
    public KeyResolver apiIpKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(path + "_" + ip);
        };
    }

    /**
 * Rate limiting strategy based on API path and user.
     */
    @Bean("apiUserKeyResolver")
    public KeyResolver apiUserKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            return Mono.just(path + "_" + (userId != null ? userId : "anonymous"));
        };
    }


}
