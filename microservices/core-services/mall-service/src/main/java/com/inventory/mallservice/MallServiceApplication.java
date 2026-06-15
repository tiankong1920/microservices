package com.inventory.mallservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商城服务启动类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@SuppressWarnings("null")
public class MallServiceApplication {

    /**
     * 主入口.
     *
     * @param args 启动参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(MallServiceApplication.class, args);
    }
}
