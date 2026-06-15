# 跨服务集成测试

## 1. 概述

跨服务集成测试模块用于测试库存管理系统中多个服务之间的协作和集成，确保整个系统能够正常工作。

## 2. 测试范围

当前实现了以下跨服务测试：

- **OrderCreationIntegrationTest**: 测试订单创建流程，包括销售订单创建、订单项目添加、库存更新、支付处理等完整流程。

## 3. 测试架构

### 3.1 测试基础类

- **CrossServiceIntegrationTestBase**: 提供了跨服务测试的基础功能，包括：
  - 测试客户端初始化
  - HTTP头设置
  - 测试数据初始化和清理
  - 服务等待功能

### 3.2 测试配置

- **application-test.properties**: 定义了测试服务URL和其他测试配置
- **CrossServiceTestApplication**: 测试应用程序启动类

## 4. 测试配置

### 4.1 服务URL配置

在`src/test/resources/application-test.properties`中配置了所有服务的URL：

```properties
# Test services URLs
test.services.sales-service.url=http://localhost:8081
test.services.order-service.url=http://localhost:8082
test.services.inventory-service.url=http://localhost:8083
test.services.finance-service.url=http://localhost:8084
test.services.product-service.url=http://localhost:8085
test.services.customer-service.url=http://localhost:8086
test.services.config-service.url=http://localhost:8888
test.services.registry-service.url=http://localhost:8761
test.services.gateway-service.url=http://localhost:8080
```

### 4.2 数据库配置

测试使用内存数据库H2，无需外部数据库支持：

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

## 5. 运行测试

### 5.1 前提条件

在运行跨服务测试之前，需要确保以下服务已经启动：

1. registry-service (端口: 8761)
2. config-service (端口: 8888)
3. gateway-service (端口: 8080)
4. sales-service (端口: 8081)
5. order-service (端口: 8082)
6. inventory-service (端口: 8083)
7. finance-service (端口: 8084)
8. product-service (端口: 8085)
9. customer-service (端口: 8086)

### 5.2 启动服务

可以使用以下命令启动所有服务：

```bash
# 在每个服务目录下执行
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 5.3 运行测试

在跨服务测试模块目录下执行以下命令：

```bash
cd e:\101\microservices\cross-service-tests
mvn test
```

### 5.4 运行特定测试

```bash
mvn test -Dtest=OrderCreationIntegrationTest
```

## 6. 扩展测试

### 6.1 创建新的跨服务测试

1. **创建测试类**：继承`CrossServiceIntegrationTestBase`类
2. **注入服务URL**：使用`@Value`注解注入服务URL
3. **编写测试方法**：使用`testRestTemplate`调用服务API
4. **验证结果**：验证服务响应和数据状态

### 6.2 示例测试类

```java
public class NewIntegrationTest extends CrossServiceIntegrationTestBase {

    @Value("${test.services.service-name.url}")
    private String serviceUrl;

    @Test
    public void testIntegration() {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("field1", "value1");
        request.put("field2", "value2");

        // 调用服务API
        ResponseEntity<JsonNode> response = testRestTemplate.postForEntity(
                serviceUrl + "/api/endpoint",
                createHttpEntity(request),
                JsonNode.class
        );

        // 验证结果
        Assert.isTrue(response.getStatusCode() == HttpStatus.OK, "Expected 200 OK");
        Assert.notNull(response.getBody(), "Response body should not be null");
        Assert.isTrue(response.getBody().get("success").asBoolean(), "Expected success to be true");
    }
}
```

## 7. 测试最佳实践

1. **测试数据隔离**：每个测试应该有独立的测试数据，避免测试之间的干扰
2. **测试清理**：测试完成后清理测试数据，保持测试环境的干净
3. **服务依赖管理**：确保测试只依赖必要的服务，减少测试的复杂性
4. **测试断言**：使用明确的断言来验证测试结果
5. **测试命名**：使用清晰的测试方法名，描述测试的目的和场景
6. **测试文档**：为复杂的测试编写文档，说明测试的流程和预期结果

## 8. 故障排除

### 8.1 服务连接失败

- 确保所有依赖的服务都已经启动
- 检查服务URL配置是否正确
- 检查服务端口是否正确
- 检查服务是否注册到Eureka

### 8.2 测试数据问题

- 确保测试数据初始化正确
- 检查测试数据清理是否彻底
- 避免测试之间的数据依赖

### 8.3 测试超时

- 增加服务等待时间
- 优化测试数据初始化
- 检查服务性能问题

## 9. 持续集成

跨服务测试可以集成到CI/CD流程中，确保每次代码变更都不会破坏系统的集成。建议在以下阶段运行跨服务测试：

1. **代码合并前**：确保新代码不会破坏现有功能
2. **每日构建**：确保系统每天都能正常工作
3. **发布前**：确保发布版本的质量

## 10. 未来计划

- 增加更多的跨服务测试场景
- 实现自动化的服务启动和停止
- 增加性能测试和压力测试
- 实现服务模拟，支持单独运行某个服务的测试
- 增加测试覆盖率报告

## 11. 联系方式

如有问题或建议，请联系库存管理系统开发团队。
