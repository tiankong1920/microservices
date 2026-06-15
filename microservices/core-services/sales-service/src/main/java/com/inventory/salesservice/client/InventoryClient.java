package com.inventory.salesservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @PostMapping("/api/inventory/check")
    Boolean checkInventoryAvailable(
            @RequestParam("productId") Long productId,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("quantity") Integer quantity);
}
