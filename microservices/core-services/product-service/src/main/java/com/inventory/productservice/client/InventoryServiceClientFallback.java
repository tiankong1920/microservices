package com.inventory.productservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.inventory.common.core.ApiResponse;

@Component
public class InventoryServiceClientFallback implements InventoryServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceClientFallback.class);

    @Override
    public ApiResponse<?> getInventoryForProduct(Long productId) {
        LOG.warn("Fallback triggered for inventory-service, productId: {}", productId);
        return ApiResponse.error("BIZ-SERVICE-UNAVAILABLE", "Inventory service temporarily unavailable");
    }
}
