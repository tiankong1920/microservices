package com.inventory.productservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.inventory.common.core.ApiResponse;

class InventoryServiceClientFallbackTest {

    @Test
    void testGetInventoryForProduct_fallbackReturnsError() {
        InventoryServiceClientFallback fallback = new InventoryServiceClientFallback();
        ApiResponse<?> response = fallback.getInventoryForProduct(1L);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
    }
}
