package com.inventory.crossservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.inventory")
public class CrossServiceTestApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CrossServiceTestApplication.class, args);
    }
}
