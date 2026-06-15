package com.inventory.gatewayservice.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.inventory.gatewayservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class AuthorizationFilterTest {

    @Mock
    private AuthService authService;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private ServerWebExchange serverWebExchange;
    private ServerHttpRequest serverHttpRequest;
    private ServerHttpResponse serverHttpResponse;
    private AuthorizationFilter authorizationFilter;

    @BeforeEach
    void setUp() {
        serverHttpRequest = mock(ServerHttpRequest.class);
        serverHttpResponse = mock(ServerHttpResponse.class);
        serverWebExchange = mock(ServerWebExchange.class);
        DataBufferFactory dataBufferFactory = mock(DataBufferFactory.class);
        DataBuffer dataBuffer = mock(DataBuffer.class);

        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());
        when(serverHttpResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpResponse.bufferFactory()).thenReturn(dataBufferFactory);
        when(dataBufferFactory.wrap(any(byte[].class))).thenReturn(dataBuffer);
        when(serverHttpResponse.writeWith(any(Publisher.class))).thenReturn(Mono.empty());
        when(serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(serverHttpResponse.setComplete()).thenReturn(Mono.empty());

        authorizationFilter = new AuthorizationFilter(authService);
    }

    @Test
    void testFilterWithWhitelistedPath() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/health"), ""));

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testFilterWithMissingAuthorizationHeader() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/product/list"), ""));

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithNonBearerAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dXNlcjpwYXNz");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/product/list"), ""));

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithInvalidBearerToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer invalid-token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/product/list"), ""));
        when(authService.validateToken("invalid-token")).thenReturn(false);

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithOAuthPath() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/oauth2/token"), ""));

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testFilterWithPublicPath() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getPath()).thenReturn(RequestPath.parse(URI.create("/api/public/data"), ""));

        authorizationFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testGetOrder() {
        assert authorizationFilter.getOrder() == 150;
    }
}
