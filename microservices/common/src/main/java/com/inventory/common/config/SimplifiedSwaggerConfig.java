package com.inventory.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * 简化的Swagger配置.
 * 使用固定版本号避免依赖冲突问题.
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(OpenAPI.class)
public class SimplifiedSwaggerConfig {

    /** SpringDoc版本号. */
    private static final String SPRING_DOC_VERSION = "v1.0.0";

    /** API标题. */
    private static final String TITLE = "Inventory Management System API";

    /** API描述. */
    private static final String DESCRIPTION = "Comprehensive inventory management API documentation";

    /** 联系人名称. */
    private static final String CONTACT_NAME = "Inventory Team";

    /** 联系人邮箱. */
    private static final String CONTACT_EMAIL = "team@inventory.com";

    /** 联系人URL. */
    private static final String CONTACT_URL = "https://inventory.com/support";

    /**
     * 创建自定义OpenAPI配置.
     *
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .description(DESCRIPTION)
                        .version(SPRING_DOC_VERSION)
                        .contact(new Contact()
                                .name(CONTACT_NAME)
                                .email(CONTACT_EMAIL)
                                .url(CONTACT_URL))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8088")
                                .description("Development server")
                ))
                .tags(List.of(
                        createTag("产品管理", "Product management related APIs"),
                        createTag("订单管理", "Order management related APIs"),
                        createTag("库存管理", "Inventory management related APIs"),
                        createTag("销售管理", "Sales management related APIs"),
                        createTag("采购管理", "Procurement management related APIs"),
                        createTag("客户管理", "Customer management related APIs"),
                        createTag("业务伙伴管理", "Business partner management related APIs"),
                        createTag("认证授权", "Authentication and authorization APIs"),
                        createTag("配置中心管理", "Configuration center management APIs"),
                        createTag("服务注册中心", "Service registry center APIs"),
                        createTag("网关服务", "API gateway service APIs"),
                        createTag("报表服务", "Report service APIs"),
                        createTag("监控服务", "Monitoring service APIs")
                ));
    }

    /**
     * 创建标签.
     *
     * @param name 标签名称
     * @param description 标签描述
     * @return Tag实例
     */
    private static Tag createTag(final String name, final String description) {
        final Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
