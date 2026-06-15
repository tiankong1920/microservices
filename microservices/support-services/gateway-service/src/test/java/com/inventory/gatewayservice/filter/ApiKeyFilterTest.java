package com.inventory.gatewayservice.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.inventory.gatewayservice.service.ApiKeyService;
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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class ApiKeyFilterTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private ServerWebExchange serverWebExchange;
    private ServerHttpRequest serverHttpRequest;
    private ServerHttpResponse serverHttpResponse;
    private ApiKeyFilter apiKeyFilter;

    @BeforeEach
    void setUp() {
        serverHttpRequest = mock(ServerHttpRequest.class);
        serverHttpResponse = mock(ServerHttpResponse.class);
        serverWebExchange = mock(ServerWebExchange.class);
        DataBufferFactory dataBufferFactory = mock(DataBufferFactory.class);
        DataBuffer dataBuffer = mock(DataBuffer.class);
        URI uri = URI.create("https://localhost:8080/api/test");

        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(serverHttpRequest.getURI()).thenReturn(uri);
        when(serverHttpRequest.getRemoteAddress()).thenReturn(null);
        when(gatewayFilterChain.filter(serverWebExchange)).thenReturn(Mono.empty());
        when(serverHttpResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpResponse.bufferFactory()).thenReturn(dataBufferFactory);
        when(dataBufferFactory.wrap(any(byte[].class))).thenReturn(dataBuffer);
        when(serverHttpResponse.writeWith(any(Publisher.class))).thenReturn(Mono.empty());
        when(serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED)).thenReturn(true);
        when(serverHttpResponse.setStatusCode(HttpStatus.FORBIDDEN)).thenReturn(true);
        when(serverHttpResponse.setComplete()).thenReturn(Mono.empty());

        apiKeyFilter = new ApiKeyFilter(apiKeyService);
    }

    @Test
    void testFilterWithValidApiKeyInHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", "valid-api-key");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getQueryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());
        when(apiKeyService.isValidApiKey("valid-api-key")).thenReturn(true);

        apiKeyFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testFilterWithInvalidApiKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", "invalid-key");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getQueryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());
        when(apiKeyService.isValidApiKey("invalid-key")).thenReturn(false);

        apiKeyFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.FORBIDDEN);
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithMissingApiKey() {
        HttpHeaders headers = new HttpHeaders();
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getQueryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());

        apiKeyFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(serverHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(gatewayFilterChain, never()).filter(serverWebExchange);
    }

    @Test
    void testFilterWithApiKeyInQueryParam() {
        HttpHeaders headers = new HttpHeaders();
        org.springframework.util.LinkedMultiValueMap<String, String> queryParams =
                new org.springframework.util.LinkedMultiValueMap<>();
        queryParams.add("api_key", "query-api-key");

        when(serverHttpRequest.getHeaders()).thenReturn(headers);
        when(serverHttpRequest.getQueryParams()).thenReturn(queryParams);
        when(apiKeyService.isValidApiKey("query-api-key")).thenReturn(true);

        apiKeyFilter.filter(serverWebExchange, gatewayFilterChain);

        verify(gatewayFilterChain).filter(serverWebExchange);
    }

    @Test
    void testGetOrder() {
        assert apiKeyFilter.getOrder() == 100;
    }
}
