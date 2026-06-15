package com.inventory.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.inventory.common.core.ApiResponse;

@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {
    
    @GetMapping("/api/orders/product/{productId}")
    ApiResponse<?> getOrdersByProduct(@PathVariable("productId") Long productId);
}
