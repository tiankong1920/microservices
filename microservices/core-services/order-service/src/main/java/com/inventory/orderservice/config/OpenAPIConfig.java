package com.inventory.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * Sets up API metadata, contact information, and license details.
 */
@Configuration
public class OpenAPIConfig {

    /**
 * Creates and configures the OpenAPI instance for the Order Service API.
     *
 * @return the configured OpenAPI instance
     */
    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Order Management Service API")
                        .version("3.0.0")
                        .contact(new Contact()
                                .name("Inventory Team")
                                .email("inventory@example.com")
                                .url("https://example.com/inventory"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
