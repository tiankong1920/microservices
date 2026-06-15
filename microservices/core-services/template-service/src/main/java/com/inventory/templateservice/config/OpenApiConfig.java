package com.inventory.templateservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:template-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI templateServiceOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(tags());
    }

    private Info apiInfo() {
        return new Info()
                .title("模板服务 API")
                .description("""
                        商业模板标准化系统 API 文档
                        
                        ## 功能模块
                        - **模板管理**: 模板的创建、更新、删除、查询
                        - **版本管理**: 语义化版本控制、版本历史、回滚
                        - **自定义字段**: 动态字段扩展、字段类型支持
                        - **校验规则**: 内置校验规则、自定义正则表达式
                        - **导入导出**: JSON/YAML/Excel 格式支持
                        - **权限控制**: RBAC 角色权限模型
                        - **审计日志**: 操作追踪、变更记录
                        
                        ## 版本说明
                        - v1.0.0: 初始版本，包含核心功能
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Inventory Management Team")
                        .email("support@inventory.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private List<Server> servers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("开发环境"),
                new Server()
                        .url("https://api-dev.inventory.com/template-service")
                        .description("测试环境"),
                new Server()
                        .url("https://api.inventory.com/template-service")
                        .description("生产环境")
        );
    }

    private List<Tag> tags() {
        return Arrays.asList(
                new Tag().name("模板管理").description("模板的增删改查接口"),
                new Tag().name("自定义字段管理").description("自定义字段和校验规则管理接口"),
                new Tag().name("模板导入导出").description("模板的导入导出接口"),
                new Tag().name("审计日志").description("模板操作审计日志接口"),
                new Tag().name("权限管理").description("模板权限管理接口")
        );
    }
}
