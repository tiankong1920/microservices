package com.inventory.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.inventory.common.core.ApiResponse;

@FeignClient(name = "inventory-service", fallback = InventoryServiceClientFallback.class)
public interface InventoryServiceClient {
    
    @GetMapping("/api/inventory/product/{productId}")
    ApiResponse<?> getInventoryForProduct(@PathVariable("productId") Long productId);
}
