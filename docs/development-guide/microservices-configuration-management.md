# 微服务配置管理指南

## 1. 概述

本文档详细说明了进销存管理系统微服务架构中的配置管理策略和最佳实践。配置管理是微服务架构中的关键组件，确保应用在不同环境中的正确运行和灵活配置。

## 2. 配置管理架构

### 2.1 组件构成

微服务配置管理由以下核心组件构成：

1. **配置服务器** (`ConfigServiceApplication`)
   - 基于 Spring Cloud Config Server
   - 集中管理所有微服务的配置
   - 支持 Git 作为配置存储后端

2. **配置客户端**
   - 每个微服务作为配置客户端
   - 启动时从配置服务器获取配置
   - 支持动态刷新配置

3. **服务注册中心** (`RegistryServiceApplication`)
   - Eureka Server 提供服务发现
   - 配置服务器和客户端通过服务发现通信

### 2.2 配置层次结构

```
配置服务器
├── application.yml (全局配置)
├── application-{profile}.yml (环境特定配置)
├── {application-name}.yml (应用特定配置)
└── {application-name}-{profile}.yml (应用环境特定配置)
```

## 3. 配置文件详解

### 3.1 全局配置文件

#### application.yml
```yaml
spring:
  application:
    name: inventory-system
  profiles:
    active: postgresql
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_system?currentSchema=public
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:inventory_user}
    password: ${DB_PASSWORD:secure_password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
```

### 3.2 应用特定配置

#### registry-service.yml
```yaml
server:
  port: 8761

spring:
  application:
    name: registry-service

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

#### config-service.yml
```yaml
server:
  port: 8888

spring:
  application:
    name: config-service
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/inventory-config-repo
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
          clone-on-start: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### gateway-service.yml
```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 4. 环境配置管理

### 4.1 环境分类

系统支持以下环境配置：

1. **开发环境** (dev)
2. **测试环境** (test)
3. **预生产环境** (staging)
4. **生产环境** (prod)

### 4.2 环境特定配置

#### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_dev
    username: dev_user
    password: dev_password
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.inventory: debug
    org.hibernate.SQL: debug
```

#### application-prod.yml
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
  jpa:
    show-sql: false

logging:
  level:
    com.inventory: info

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 5. 敏感信息管理

### 5.1 环境变量配置

敏感信息通过环境变量进行配置，避免明文存储在配置文件中：

```bash
# 数据库配置
export DB_USERNAME=inventory_user
export DB_PASSWORD=secure_password

# Git 配置
export GIT_USERNAME=config_user
export GIT_PASSWORD=config_password

# JWT 配置
export JWT_SECRET=ThisIsAStrongSecretKey
export JWT_EXPIRATION=1800
```

### 5.2 配置加密

对于需要加密存储的配置，使用 Spring Cloud Config 的加密功能：

```yaml
app:
  jwt:
    secret: '{cipher}AQAAAEAAAABAAAAA...'
```

## 6. 配置刷新机制

### 6.1 @RefreshScope 注解

在需要动态刷新配置的 Bean 上使用 `@RefreshScope` 注解：

```java
@RestController
@RefreshScope
public class ConfigController {
    
    @Value("${app.message:Hello World}")
    private String message;
    
    @GetMapping("/message")
    public String getMessage() {
        return message;
    }
}
```

### 6.2 手动刷新配置

通过调用 `/actuator/refresh` 端点手动刷新配置：

```bash
curl -X POST http://localhost:8080/actuator/refresh
```

## 7. 配置最佳实践

### 7.1 配置命名规范

1. **属性命名**
   - 使用小写字母和连字符分隔
   - 采用层次化命名结构
   - 避免过长的属性名

2. **示例**
   ```yaml
   # 推荐
   app:
     jwt:
       secret: ${JWT_SECRET:default-secret}
       expiration: ${JWT_EXPIRATION:1800}
   
   # 不推荐
   app.jwtSecret: ${JWT_SECRET:default-secret}
   app.jwtExpiration: ${JWT_EXPIRATION:1800}
   ```

### 7.2 配置分组管理

1. **按功能模块分组**
   ```yaml
   # 数据库配置
   spring:
     datasource:
       # ...
     jpa:
       # ...
   
   # 缓存配置
   spring:
     cache:
       # ...
     redis:
       # ...
   
   # 消息队列配置
   spring:
     rabbitmq:
       # ...
     kafka:
       # ...
   ```

2. **按业务领域分组**
   ```yaml
   # 用户服务配置
   user:
     service:
       url: http://user-service
       timeout: 5000
   
   # 产品服务配置
   product:
     service:
       url: http://product-service
       timeout: 5000
   ```

### 7.3 配置验证

1. **配置属性类**
   ```java
   @ConfigurationProperties(prefix = "app.jwt")
   @Validated
   @Data
   public class JwtProperties {
       @NotBlank
       private String secret;
       
       @Min(300)
       private Long expiration = 1800L;
       
       @Min(3600)
       private Long refreshExpiration = 604800L;
   }
   ```

2. **配置验证**
   ```java
   @Component
   @RequiredArgsConstructor
   public class ConfigurationValidator {
       
       private final JwtProperties jwtProperties;
       
       @PostConstruct
       public void validate() {
           if (jwtProperties.getExpiration() >= jwtProperties.getRefreshExpiration()) {
               throw new IllegalStateException("JWT expiration must be less than refresh expiration");
           }
       }
   }
   ```

## 8. 故障排除

### 8.1 常见问题

1. **配置无法加载**
   - 检查配置服务器是否正常运行
   - 验证 Git 仓库连接配置
   - 确认应用名称和服务ID匹配

2. **配置刷新失败**
   - 确认 Bean 使用了 @RefreshScope 注解
   - 检查 Actuator 端点配置
   - 验证配置属性是否支持刷新

### 8.2 日志诊断

1. **配置加载日志**
   ```properties
   logging.level.org.springframework.cloud.config=DEBUG
   logging.level.org.springframework.core.env=DEBUG
   ```

2. **服务发现日志**
   ```properties
   logging.level.org.springframework.cloud.netflix.eureka=DEBUG
   ```

## 9. 监控和运维

### 9.1 配置健康检查

通过 Actuator 端点监控配置状态：

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 配置信息
curl http://localhost:8080/actuator/env

# 配置属性
curl http://localhost:8080/actuator/configprops
```

### 9.2 配置审计

记录配置变更历史，便于问题追踪和审计：

```java
@EventListener
public void handleEnvironmentChangeEvent(EnvironmentChangeEvent event) {
    log.info("Configuration changed: {}", event.getKeys());
}
```

## 10. 总结

通过合理的配置管理策略，可以确保微服务架构的灵活性、安全性和可维护性。开发团队应遵循本文档中的最佳实践，正确使用配置管理功能，确保系统在不同环境中的稳定运行。