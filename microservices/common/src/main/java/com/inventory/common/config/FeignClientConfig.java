package com.inventory.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.inventory")
public class FeignClientConfig {
    // Optional: add fallback factory, request interceptors, or custom encoders/decoders
}
