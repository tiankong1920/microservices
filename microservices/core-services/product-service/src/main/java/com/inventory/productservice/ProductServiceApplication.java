package com.inventory.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 产品服务主应用类.
 *
 * <p>产品服务微服务的入口类，负责启动和管理产品相关的所有业务功能。
 * 该服务提供产品的增删改查功能，包括产品信息管理、SKU管理、BOM管理等核心业务逻辑。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>产品信息管理：产品的创建、查询、更新、删除</li>
 *   <li>产品SKU管理：产品规格、库存单位、价格等SKU信息管理</li>
 *   <li>产品BOM管理：产品物料清单配置，支持复杂产品的组装</li>
 *   <li>产品分类管理：产品分类、品牌、规格等维度管理</li>
 * </ul>
 *
 * <p>技术特性：</p>
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>启用缓存支持，提升查询性能</li>
 *   <li>支持RESTful API接口</li>
 * </ul>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 * @see SpringApplication
 * @see SpringBootApplication
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableCaching
public class ProductServiceApplication {

    public ProductServiceApplication() {
        // Default constructor for Spring Boot application
    }

    public static void main(final String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}

