package com.inventory.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Product Service Feign Client.
 * 用于订单服务调用产品服务.
 */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {

    /**
 * 获取产品信息.
 * @param id 产品ID
 * @return 产品信息
     */
    @GetMapping("/{id}")
    Object getProductById(@PathVariable("id") Long id);

    /**
 * 验证产品是否存在.
 * @param id 产品ID
 * @return 产品信息
     */
    @GetMapping("/validate/{id}")
    boolean validateProduct(@PathVariable("id") Long id);
}
