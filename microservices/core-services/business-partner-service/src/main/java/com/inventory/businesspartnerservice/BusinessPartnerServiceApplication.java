package com.inventory.businesspartnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration.class)
@EnableCaching
public final class BusinessPartnerServiceApplication {
    private BusinessPartnerServiceApplication() {
        // Prevent instantiation
    }

    public static void main(final String[] args) {
        SpringApplication.run(BusinessPartnerServiceApplication.class, args);
    }
}
