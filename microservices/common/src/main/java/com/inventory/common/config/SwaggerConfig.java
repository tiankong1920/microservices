package com.inventory.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Swagger/OpenAPI配置.
 * 提供API文档的自动生成和展示.
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(OpenAPI.class)
public class SwaggerConfig {

    /** 应用名称. */
    @Value("${spring.application.name:Inventory Management System}")
    private String applicationName;

    /** 应用版本. */
    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    /** 应用描述. */
    @Value("${spring.application.description:Inventory Management System API}")
    private String applicationDescription;

    /** 联系人邮箱. */
    @Value("${spring.application.contact.email:admin@example.com}")
    private String contactEmail;

    /** 联系人URL. */
    @Value("${spring.application.contact.url:http://example.com}")
    private String contactUrl;

    /**
     * 创建自定义OpenAPI配置.
     *
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API Documentation")
                        .description(applicationDescription)
                        .version(applicationVersion)
                        .contact(new Contact()
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8088")
                                .description("Development server")
                ))
                .tags(List.of(
                        new Tag().name("产品管理").description("产品管理相关的API"),
                        new Tag().name("订单管理").description("订单管理相关的API"),
                        new Tag().name("库存管理").description("库存管理相关的API"),
                        new Tag().name("销售管理").description("销售管理相关的API"),
                        new Tag().name("采购管理").description("采购管理相关的API"),
                        new Tag().name("客户管理").description("客户管理相关的API"),
                        new Tag().name("业务伙伴管理").description("业务伙伴管理相关的API"),
                        new Tag().name("认证授权").description("认证授权相关的API"),
                        new Tag().name("配置中心").description("配置中心相关的API"),
                        new Tag().name("服务注册").description("服务注册中心相关的API"),
                        new Tag().name("网关服务").description("网关服务相关的API"),
                        new Tag().name("报表服务").description("报表服务相关的API"),
                        new Tag().name("监控服务").description("监控服务相关的API")
                ));
    }
}
