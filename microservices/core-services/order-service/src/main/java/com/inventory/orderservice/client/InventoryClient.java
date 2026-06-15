package com.inventory.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Inventory Service Feign Client.
 * 用于订单服务调用库存服务.
 */
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    /**
 * 检查库存是否足够.
 * @param productId 产品ID
 * @param quantity 需要的数量
 * @return 库存是否足够
     */
    @GetMapping("/check/{productId}/{quantity}")
    boolean checkStock(@PathVariable("productId") Long productId, 
                       @PathVariable("quantity") Integer quantity);

    /**
 * 扣减库存.
 * @param productId 产品ID
 * @param quantity 扣减数量
 * @return 扣减结果
     */
    @PostMapping("/deduct/{productId}/{quantity}")
    boolean deductStock(@PathVariable("productId") Long productId, 
                        @PathVariable("quantity") Integer quantity);

    /**
 * 恢复库存.
 * @param productId 产品ID
 * @param quantity 恢复数量
 * @return 恢复结果
     */
    @PostMapping("/restore/{productId}/{quantity}")
    boolean restoreStock(@PathVariable("productId") Long productId, 
                         @PathVariable("quantity") Integer quantity);
}
