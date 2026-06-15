package com.inventory.productservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.inventory.common.core.ApiResponse;

@Component
public class OrderServiceClientFallback implements OrderServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceClientFallback.class);

    @Override
    public ApiResponse<?> getOrdersByProduct(Long productId) {
        LOG.warn("Fallback triggered for order-service, productId: {}", productId);
        return ApiResponse.error("BIZ-SERVICE-UNAVAILABLE", "Order service temporarily unavailable");
    }
}
