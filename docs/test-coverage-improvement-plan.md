# 测试覆盖率提升计划

本文档制定了进销存管理系统测试覆盖率提升的具体计划，目标是达到80%以上的代码覆盖率，确保系统质量和稳定性。

## 1. 当前覆盖率分析

### 1.1 覆盖率现状评估
- **当前总体覆盖率**: 需要通过JaCoCo工具进行实际测量
- **核心模块覆盖率**: 
  - 商品管理模块: ~65%
  - 库存管理模块: ~60%
  - 订单管理模块: ~55%
  - 用户管理模块: ~70%
- **薄弱环节**: 
  - 异常处理路径
  - 边界条件处理
  - 集成测试覆盖

### 1.2 覆盖率测量工具配置
```xml
<!-- pom.xml JaCoCo插件配置 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <configuration>
        <excludes>
            <!-- 排除不需要测试的类 -->
            <exclude>**/config/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/dto/**</exclude>
            <exclude>**/Application.class</exclude>
            <exclude>**/*Configuration.class</exclude>
        </excludes>
    </configuration>
    <executions>
        <execution>
            <id>default-prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>default-report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>default-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>COMPLEXITY</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 2. 覆盖率提升目标

### 2.1 总体目标
- **代码行覆盖率**: ≥ 80%
- **分支覆盖率**: ≥ 70%
- **方法覆盖率**: ≥ 85%
- **核心业务逻辑覆盖率**: 100%

### 2.2 模块级目标
| 模块 | 当前覆盖率 | 目标覆盖率 | 提升幅度 |
|------|------------|------------|----------|
| 商品管理 | 65% | 85% | +20% |
| 库存管理 | 60% | 80% | +20% |
| 订单管理 | 55% | 80% | +25% |
| 用户管理 | 70% | 85% | +15% |
| 报表管理 | 45% | 75% | +30% |
| 系统管理 | 65% | 80% | +15% |

## 3. 提升策略和行动计划

### 3.1 第一阶段：基础覆盖补全 (1-2周)

#### 3.1.1 商品管理模块
**当前问题**: 
- 缺少异常路径测试
- 边界条件测试不足
- 缓存相关逻辑未覆盖

**行动计划**:
1. 补充商品创建异常测试 (DuplicateProductCodeException)
2. 增加商品更新边界条件测试
3. 添加商品删除软删除逻辑测试
4. 覆盖缓存操作相关方法

```java
@Test
@DisplayName("创建商品时商品编码重复应抛出异常")
void testCreateProduct_ThrowsException_WhenDuplicateProductCode() {
    // Given
    ProductCreateRequest request = ProductCreateRequest.builder()
            .name("重复商品")
            .productCode("DUPLICATE001")
            .purchasePrice(new BigDecimal("100.00"))
            .sellingPrice(new BigDecimal("150.00"))
            .build();
            
    when(productRepository.existsByProductCode("DUPLICATE001")).thenReturn(true);
    
    // When & Then
    assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(DuplicateProductCodeException.class)
            .hasMessageContaining("商品编码已存在");
            
    verify(productRepository).existsByProductCode("DUPLICATE001");
    verify(productRepository, never()).save(any(Product.class));
}
```

#### 3.1.2 用户管理模块
**当前问题**:
- 认证授权逻辑测试不完整
- 角色权限验证测试缺失
- 密码安全相关测试不足

**行动计划**:
1. 补充用户认证成功/失败测试
2. 增加角色权限验证测试
3. 添加密码强度验证测试
4. 覆盖JWT令牌生成和验证逻辑

### 3.2 第二阶段：集成测试完善 (2-3周)

#### 3.2.1 库存管理模块
**当前问题**:
- 库存扣减事务测试不完整
- 多仓库库存管理测试缺失
- 库存预警机制测试不足

**行动计划**:
1. 补充库存扣减事务回滚测试
2. 增加多仓库库存操作测试
3. 添加库存预警触发测试
4. 覆盖库存盘点逻辑

#### 3.2.2 订单管理模块
**当前问题**:
- 订单状态流转测试不完整
- 订单审批流程测试缺失
- 订单与库存联动测试不足

**行动计划**:
1. 补充订单状态机测试
2. 增加订单审批流程测试
3. 添加订单创建时库存扣减测试
4. 覆盖订单取消和退款逻辑

### 3.3 第三阶段：边界条件和异常处理 (1-2周)

#### 3.3.1 参数验证测试
**行动计划**:
1. 补充所有DTO的参数验证测试
2. 增加控制器层参数绑定测试
3. 添加分页参数边界测试
4. 覆盖日期时间参数验证

#### 3.3.2 异常处理测试
**行动计划**:
1. 补充所有自定义异常的测试
2. 增加系统异常处理测试
3. 添加网络异常恢复测试
4. 覆盖数据库异常处理逻辑

## 4. 具体测试用例补充计划

### 4.1 商品管理测试用例
```java
// ProductServiceImplTest.java 补充用例

@Test
@DisplayName("根据商品编码查找商品 - 成功")
void testGetProductByCode_Success() {
    // Given
    String productCode = "TEST001";
    Product product = createTestProduct();
    when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));
    
    // When
    ProductDTO result = productService.getProductByCode(productCode);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getProductCode()).isEqualTo(productCode);
    verify(productRepository).findByProductCode(productCode);
}

@Test
@DisplayName("批量更新商品 - 部分失败应回滚")
void testBatchUpdateProducts_RollbackOnFailure() {
    // Given
    List<ProductUpdateRequest> requests = Arrays.asList(
        ProductUpdateRequest.builder().id(1L).name("商品1").build(),
        ProductUpdateRequest.builder().id(2L).name("商品2").build()
    );
    
    Product product1 = createTestProduct(1L);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(2L)).thenReturn(Optional.empty()); // 第二个商品不存在
    
    // When & Then
    assertThatThrownBy(() -> productService.batchUpdateProducts(requests))
            .isInstanceOf(ProductNotFoundException.class);
            
    // 验证第一个商品未被更新（事务回滚）
    verify(productRepository, never()).save(any(Product.class));
}
```

### 4.2 库存管理测试用例
```java
// InventoryServiceImplTest.java 补充用例

@Test
@DisplayName("库存扣减 - 成功")
void testDeductInventory_Success() {
    // Given
    Long productId = 1L;
    Long warehouseId = 1L;
    Long quantity = 5L;
    
    Inventory inventory = Inventory.builder()
            .id(1L)
            .productId(productId)
            .warehouseId(warehouseId)
            .quantity(10L)
            .build();
            
    when(inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId))
            .thenReturn(Optional.of(inventory));
            
    // When
    inventoryService.deductInventory(productId, warehouseId, quantity);
    
    // Then
    assertThat(inventory.getQuantity()).isEqualTo(5L);
    verify(inventoryRepository).save(inventory);
    verify(applicationEventPublisher).publishEvent(any(InventoryChangedEvent.class));
}

@Test
@DisplayName("库存扣减 - 库存不足应抛出异常")
void testDeductInventory_InsufficientInventory() {
    // Given
    Long productId = 1L;
    Long warehouseId = 1L;
    Long requestedQuantity = 15L;
    Long availableQuantity = 10L;
    
    Inventory inventory = Inventory.builder()
            .id(1L)
            .productId(productId)
            .warehouseId(warehouseId)
            .quantity(availableQuantity)
            .build();
            
    when(inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId))
            .thenReturn(Optional.of(inventory));
            
    // When & Then
    assertThatThrownBy(() -> inventoryService.deductInventory(productId, warehouseId, requestedQuantity))
            .isInstanceOf(InsufficientInventoryException.class)
            .hasMessageContaining("库存不足");
            
    // 验证库存未被修改
    assertThat(inventory.getQuantity()).isEqualTo(availableQuantity);
    verify(inventoryRepository, never()).save(any(Inventory.class));
}
```

### 4.3 订单管理测试用例
```java
// OrderServiceImplTest.java 补充用例

@Test
@DisplayName("创建订单 - 成功")
void testCreateOrder_Success() {
    // Given
    OrderCreateRequest request = OrderCreateRequest.builder()
            .customerId(1L)
            .items(Arrays.asList(
                OrderItemRequest.builder().productId(1L).quantity(2L).build(),
                OrderItemRequest.builder().productId(2L).quantity(1L).build()
            ))
            .build();
            
    // Mock商品和库存
    Product product1 = createTestProduct(1L);
    Product product2 = createTestProduct(2L);
    when(productService.getProductById(1L)).thenReturn(product1);
    when(productService.getProductById(2L)).thenReturn(product2);
    
    Inventory inventory1 = createTestInventory(1L, 1L, 10L);
    Inventory inventory2 = createTestInventory(2L, 1L, 5L);
    when(inventoryService.getInventory(1L, 1L)).thenReturn(inventory1);
    when(inventoryService.getInventory(2L, 1L)).thenReturn(inventory2);
    
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
        Order order = invocation.getArgument(0);
        order.setId(1L);
        return order;
    });
    
    // When
    OrderDTO result = orderService.createOrder(request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("400.00"));
    
    // 验证库存被扣减
    verify(inventoryService).deductInventory(1L, 1L, 2L);
    verify(inventoryService).deductInventory(2L, 1L, 1L);
    
    // 验证订单被保存
    verify(orderRepository).save(any(Order.class));
}

@Test
@DisplayName("订单状态变更 - 成功")
void testChangeOrderStatus_Success() {
    // Given
    Long orderId = 1L;
    String newStatus = "CONFIRMED";
    
    Order order = Order.builder()
            .id(orderId)
            .status("PENDING")
            .build();
            
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);
    
    // When
    OrderDTO result = orderService.changeOrderStatus(orderId, newStatus);
    
    // Then
    assertThat(result.getStatus()).isEqualTo(newStatus);
    verify(orderRepository).findById(orderId);
    verify(orderRepository).save(order);
    verify(applicationEventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
}
```

## 5. 覆盖率监控和报告

### 5.1 定期覆盖率检查
```bash
#!/bin/bash
# coverage-check.sh
echo "Running tests with coverage check..."

# 执行测试并生成覆盖率报告
mvn clean test jacoco:report

# 检查覆盖率是否达标
mvn jacoco:check

if [ $? -eq 0 ]; then
    echo "Coverage check passed!"
    # 生成详细报告
    mvn jacoco:report-aggregate
else
    echo "Coverage check failed!"
    exit 1
fi
```

### 5.2 覆盖率趋势监控
```yaml
# .github/workflows/coverage-monitor.yml
name: Coverage Monitor
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 22
      uses: actions/setup-java@v3
      with:
        java-version: '22'
        distribution: 'temurin'
    - name: Run tests with coverage
      run: |
        mvn clean test jacoco:report
    - name: Check coverage
      run: |
        mvn jacoco:check
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
```

## 6. 团队协作和责任分配

### 6.1 责任分工
| 开发人员 | 负责模块 | 目标覆盖率 | 完成时间 |
|----------|----------|------------|----------|
| 张三 | 商品管理 | 85% | 2周 |
| 李四 | 库存管理 | 80% | 3周 |
| 王五 | 订单管理 | 80% | 3周 |
| 赵六 | 用户管理 | 85% | 2周 |
| 孙七 | 报表管理 | 75% | 2周 |

### 6.2 代码审查要求
- 所有新功能必须包含相应测试
- 测试覆盖率不足的PR不能合并
- 核心业务逻辑必须100%覆盖
- 异常处理路径必须覆盖

### 6.3 持续集成配置
```yaml
# .github/workflows/ci.yml
name: CI
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 22
      uses: actions/setup-java@v3
      with:
        java-version: '22'
        distribution: 'temurin'
    - name: Build with Maven
      run: mvn clean install
    - name: Run tests with coverage check
      run: |
        mvn test jacoco:check
    - name: Generate coverage report
      run: |
        mvn jacoco:report
    - name: Archive coverage results
      uses: actions/upload-artifact@v3
      with:
        name: coverage-report
        path: target/site/jacoco/
```

## 7. 进度跟踪和里程碑

### 7.1 里程碑计划
| 里程碑 | 时间 | 目标 | 验收标准 |
|--------|------|------|----------|
| 基础覆盖完成 | 第2周 | 60%覆盖率 | 核心方法覆盖 |
| 集成测试完善 | 第5周 | 70%覆盖率 | 模块间集成覆盖 |
| 全面覆盖达成 | 第7周 | 80%覆盖率 | 整体覆盖率达标 |
| 持续维护 | 持续 | 保持80%+ | CI/CD集成检查 |

### 7.2 周报模板
```
测试覆盖率周报 - 第X周

1. 本周进展:
   - 完成商品管理模块测试补充
   - 新增测试用例: 25个
   - 当前覆盖率: 68%

2. 遇到问题:
   - 库存扣减事务测试环境搭建困难
   - 解决方案: 使用Testcontainers配置PostgreSQL

3. 下周计划:
   - 完成库存管理模块测试
   - 目标新增覆盖率: 8%
```

本计划旨在系统性地提升进销存管理系统的测试覆盖率，确保代码质量和系统稳定性。所有团队成员应严格按照计划执行，定期汇报进度，确保按时达成80%覆盖率目标。