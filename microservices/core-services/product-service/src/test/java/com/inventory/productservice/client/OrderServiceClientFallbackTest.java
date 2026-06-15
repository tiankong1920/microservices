package com.inventory.productservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.inventory.common.core.ApiResponse;

class OrderServiceClientFallbackTest {

    @Test
    void testGetOrdersByProduct_fallbackReturnsError() {
        OrderServiceClientFallback fallback = new OrderServiceClientFallback();
        ApiResponse<?> response = fallback.getOrdersByProduct(1L);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
    }
}
