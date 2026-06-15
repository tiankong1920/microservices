package com.inventory.gatewayservice.filter;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.inventory.gatewayservice.config.IpFilterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class IpFilterTest {

    private IpFilterConfig ipFilterConfig;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private ServerWebExchange serverWebExchange;
    private ServerHttpRequest serverHttpRequest;
    private ServerHttpResponse serverHttpResponse;

    @BeforeEach
    void setUp() {
        ipFilterConfig = new IpFilterConfig();
        ipFilterConfig.setEnabled(true);
        ipFilterConfig.setAllowLocalhost(true);
        ipFilterConfig.setLogEnabled(true);
        ipFilterConfig.setStrategy("blacklist");

        serverHttpRequest = mock(ServerHttpRequest.class);
        serverHttpResponse = mock(ServerHttpResponse.class);
        serverWebExchange = mock(ServerWebExchange.class);

        lenient().when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        lenient().when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        lenient().when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());
        lenient().when(serverHttpResponse.setStatusCode(HttpStatus.FORBIDDEN)).thenReturn(true);
        lenient().when(serverHttpResponse.setComplete()).thenReturn(Mono.empty());
    }

    @Test
    void testFilterWithIpFilterDisabled() {
        ipFilterConfig.setEnabled(false);
        IpFilter ipFilter = new IpFilter(ipFilterConfig);

        ipFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testFilterWithLocalhostAllowed() {
        ipFilterConfig.setStrategy("blacklist");
        ipFilterConfig.setAllowLocalhost(true);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Real-IP", "127.0.0.1");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/test"), ""));

        IpFilter ipFilter = new IpFilter(ipFilterConfig);

        ipFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testFilterWithBlacklistedIp() {
        ipFilterConfig.setStrategy("blacklist");
        ipFilterConfig.setAllowLocalhost(false);
        ipFilterConfig.setBlacklist(List.of("192.168.1.100"));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Real-IP", "192.168.1.100");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/test"), ""));

        IpFilter ipFilter = new IpFilter(ipFilterConfig);

        ipFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.FORBIDDEN);
        verify(serverHttpResponse).setComplete();
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithWhitelistedIp() {
        ipFilterConfig.setStrategy("whitelist");
        ipFilterConfig.setAllowLocalhost(false);
        ipFilterConfig.setWhitelist(List.of("192.168.1.50"));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Real-IP", "192.168.1.50");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/test"), ""));

        IpFilter ipFilter = new IpFilter(ipFilterConfig);

        ipFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testIsLocalhost() {
        IpFilter ipFilter = new IpFilter(ipFilterConfig);

        // Can'"'"'t test private methods directly, test through filter behavior
        ipFilterConfig.setAllowLocalhost(true);
        ipFilterConfig.setStrategy("blacklist");
        ipFilterConfig.setBlacklist(List.of("10.0.0.1"));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Real-IP", "127.0.0.1");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/test"), ""));

        ipFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }
}
