package com.inventory.gatewayservice.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.gatewayservice.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuditLogFilterTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private ServerWebExchange serverWebExchange;
    private ServerHttpRequest serverHttpRequest;
    private ServerHttpResponse serverHttpResponse;
    private AuditLogFilter auditLogFilter;

    @BeforeEach
    void setUp() {
        serverHttpRequest = mock(ServerHttpRequest.class);
        serverHttpResponse = mock(ServerHttpResponse.class);
        serverWebExchange = mock(ServerWebExchange.class);

        lenient().when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        lenient().when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        lenient().when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());

        auditLogFilter = new AuditLogFilter(auditLogService);
    }

    @Test
    void testFilterLogsSuccessfulRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, "TestAgent");
        headers.add("X-API-Key", "test-key");

        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getMethod()).thenReturn(HttpMethod.GET);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/test"), ""));
        when(serverHttpRequest.getRemoteAddress())
                .thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());

        auditLogFilter.filter(serverWebExchange, gatewayFilterChain).block();

        verify(auditLogService).log(any(AuditLogService.AuditLogEntry.class));
    }

    @Test
    void testFilterLogsWithNullRemoteAddress() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getMethod()).thenReturn(HttpMethod.POST);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/data"), ""));
        when(serverHttpRequest.getRemoteAddress()).thenReturn(null);
        when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());

        auditLogFilter.filter(serverWebExchange, gatewayFilterChain).block();

        verify(auditLogService).log(any(AuditLogService.AuditLogEntry.class));
    }

    @Test
    void testGetOrder() {
        assert auditLogFilter.getOrder() == 200;
    }
}
