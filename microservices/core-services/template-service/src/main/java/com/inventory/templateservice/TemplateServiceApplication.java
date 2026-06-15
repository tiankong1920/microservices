package com.inventory.templateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableScheduling
public class TemplateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateServiceApplication.class, args);
    }
}
