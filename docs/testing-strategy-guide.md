# 测试策略指南

本文档定义了进销存管理系统的测试策略，包括单元测试、集成测试、端到端测试和性能测试等方面的实现规范和最佳实践。

## 1. 测试策略概述

### 1.1 测试金字塔
```
        ┌─────────────┐
        │   E2E测试   │  10%
        ├─────────────┤
        │ 集成测试    │  20%
        ├─────────────┤
        │ 单元测试    │  70%
        └─────────────┘
```

### 1.2 测试类型分类
- **单元测试**: 验证单个类或方法的功能正确性
- **集成测试**: 验证多个组件协同工作的正确性
- **端到端测试**: 验证完整业务流程的正确性
- **性能测试**: 验证系统在负载下的性能表现
- **安全测试**: 验证系统的安全性

### 1.3 测试覆盖率目标
- **总体覆盖率**: 80%以上
- **核心业务逻辑**: 100%覆盖
- **异常处理路径**: 100%覆盖
- **边界条件**: 100%覆盖

## 2. 单元测试

### 2.1 测试框架配置
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.2 单元测试示例
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CacheService cacheService;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product testProduct;
    private ProductDTO testProductDTO;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("测试商品")
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("测试商品")
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
    }
    
    @Test
    @DisplayName("根据ID获取商品 - 成功场景")
    void testGetProductById_Success() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        
        // When
        ProductDTO result = productService.getProductById(productId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("测试商品");
        
        // 验证方法调用
        verify(productRepository).findById(productId);
    }
    
    @Test
    @DisplayName("根据ID获取商品 - 商品不存在")
    void testGetProductById_ProductNotFound() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("商品不存在");
                
        verify(productRepository).findById(productId);
    }
    
    @Test
    @DisplayName("创建商品 - 成功场景")
    void testCreateProduct_Success() {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("新商品")
                .productCode("NEW001")
                .purchasePrice(new BigDecimal("80.00"))
                .sellingPrice(new BigDecimal("120.00"))
                .build();
                
        Product savedProduct = Product.builder()
                .id(2L)
                .name("新商品")
                .productCode("NEW001")
                .purchasePrice(new BigDecimal("80.00"))
                .sellingPrice(new BigDecimal("120.00"))
                .build();
                
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // When
        ProductDTO result = productService.createProduct(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("新商品");
        
        verify(productRepository).save(any(Product.class));
        verify(cacheService).putProduct(result);
    }
    
    @Test
    @DisplayName("更新商品 - 成功场景")
    void testUpdateProduct_Success() {
        // Given
        Long productId = 1L;
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name("更新后的商品")
                .sellingPrice(new BigDecimal("180.00"))
                .build();
                
        Product existingProduct = testProduct.toBuilder().build();
        Product updatedProduct = existingProduct.toBuilder()
                .name("更新后的商品")
                .sellingPrice(new BigDecimal("180.00"))
                .build();
                
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
        
        // When
        ProductDTO result = productService.updateProduct(productId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("更新后的商品");
        assertThat(result.getSellingPrice()).isEqualByComparingTo(new BigDecimal("180.00"));
        
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
        verify(cacheService).putProduct(result);
    }
    
    @Test
    @DisplayName("删除商品 - 成功场景")
    void testDeleteProduct_Success() {
        // Given
        Long productId = 1L;
        Product existingProduct = testProduct.toBuilder().build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        
        // When
        productService.deleteProduct(productId);
        
        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
        assertThat(existingProduct.getDeletedAt()).isNotNull();
        verify(cacheService).evictProduct(productId);
    }
}
```

### 2.3 参数验证测试
```java
@ExtendWith(MockitoExtension.class)
class ProductValidationTest {
    
    @Test
    @DisplayName("商品创建请求参数验证 - 名称为空")
    void testProductCreateRequestValidation_NameEmpty() {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("") // 空名称
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
        
        // When
        Set<ConstraintViolation<ProductCreateRequest>> violations = 
                Validation.buildDefaultValidatorFactory().getValidator().validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductCreateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("商品名称不能为空");
    }
    
    @Test
    @DisplayName("商品创建请求参数验证 - 价格为负数")
    void testProductCreateRequestValidation_NegativePrice() {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("测试商品")
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("-10.00")) // 负数价格
                .sellingPrice(new BigDecimal("150.00"))
                .build();
        
        // When
        Set<ConstraintViolation<ProductCreateRequest>> violations = 
                Validation.buildDefaultValidatorFactory().getValidator().validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProductCreateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("purchasePrice");
        assertThat(violation.getMessage()).isEqualTo("采购价格必须大于等于0");
    }
}
```

## 3. 集成测试

### 3.1 Spring Boot集成测试配置
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class ProductControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        productRepository.deleteAll();
    }
    
    @Test
    @DisplayName("创建商品 - 集成测试")
    void testCreateProduct_Integration() {
        // Given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("集成测试商品")
                .productCode("INT001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
        
        // When
        ResponseEntity<ProductDTO> response = restTemplate.postForEntity(
                "/api/products", request, ProductDTO.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("集成测试商品");
        assertThat(response.getBody().getProductCode()).isEqualTo("INT001");
        
        // 验证数据库状态
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("集成测试商品");
    }
    
    @Test
    @DisplayName("获取商品列表 - 集成测试")
    void testGetProducts_Integration() {
        // Given
        Product product1 = Product.builder()
                .name("商品1")
                .productCode("PROD001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        Product product2 = Product.builder()
                .name("商品2")
                .productCode("PROD002")
                .purchasePrice(new BigDecimal("80.00"))
                .sellingPrice(new BigDecimal("120.00"))
                .build();
                
        productRepository.saveAll(Arrays.asList(product1, product2));
        
        // When
        ResponseEntity<PagedResponse<ProductDTO>> response = restTemplate.getForEntity(
                "/api/products?page=0&size=10", 
                new ParameterizedTypeReference<PagedResponse<ProductDTO>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getPagination().getTotal()).isEqualTo(2);
    }
}
```

### 3.2 数据库集成测试
```java
@DataJpaTest
class ProductRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("根据商品编码查找商品 - 成功")
    void testFindByProductCode_Success() {
        // Given
        Product product = Product.builder()
                .name("测试商品")
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        entityManager.persistAndFlush(product);
        
        // When
        Optional<Product> found = productRepository.findByProductCode("TEST001");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("测试商品");
        assertThat(found.get().getProductCode()).isEqualTo("TEST001");
    }
    
    @Test
    @DisplayName("检查商品编码是否存在 - 存在")
    void testExistsByProductCode_Exists() {
        // Given
        Product product = Product.builder()
                .name("测试商品")
                .productCode("TEST001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        entityManager.persistAndFlush(product);
        
        // When
        boolean exists = productRepository.existsByProductCode("TEST001");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("检查商品编码是否存在 - 不存在")
    void testExistsByProductCode_NotExists() {
        // When
        boolean exists = productRepository.existsByProductCode("NONEXISTENT");
        
        // Then
        assertThat(exists).isFalse();
    }
}
```

## 4. 端到端测试

### 4.1 使用Testcontainers进行数据库测试
```java
@SpringBootTest
@Testcontainers
class ProductServiceEndToEndTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("inventory_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("完整的商品生命周期 - 端到端测试")
    void testProductLifecycle_EndToEnd() {
        // 1. 创建商品
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .name("端到端测试商品")
                .productCode("E2E001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        ProductDTO createdProduct = productService.createProduct(createRequest);
        assertThat(createdProduct.getId()).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("端到端测试商品");
        
        // 2. 查询商品
        ProductDTO retrievedProduct = productService.getProductById(createdProduct.getId());
        assertThat(retrievedProduct).isEqualTo(createdProduct);
        
        // 3. 更新商品
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name("更新后的端到端测试商品")
                .sellingPrice(new BigDecimal("180.00"))
                .build();
                
        ProductDTO updatedProduct = productService.updateProduct(createdProduct.getId(), updateRequest);
        assertThat(updatedProduct.getName()).isEqualTo("更新后的端到端测试商品");
        assertThat(updatedProduct.getSellingPrice()).isEqualByComparingTo(new BigDecimal("180.00"));
        
        // 4. 删除商品
        productService.deleteProduct(createdProduct.getId());
        
        // 5. 验证商品已被软删除
        assertThat(productRepository.findById(createdProduct.getId()))
                .isPresent()
                .get()
                .extracting(Product::getDeletedAt)
                .isNotNull();
    }
}
```

### 4.2 API端到端测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class ProductApiEndToEndTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    @DisplayName("商品API完整流程 - 端到端测试")
    void testProductApiFlow_EndToEnd() {
        // 1. 创建商品
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .name("API测试商品")
                .productCode("API001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
        
        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(
                "/api/products", createRequest, ProductDTO.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDTO createdProduct = createResponse.getBody();
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("API测试商品");
        
        // 2. 获取商品
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(
                "/api/products/" + createdProduct.getId(), ProductDTO.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualTo(createdProduct);
        
        // 3. 更新商品
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name("更新后的API测试商品")
                .build();
                
        restTemplate.put("/api/products/" + createdProduct.getId(), updateRequest);
        
        // 4. 验证更新
        ResponseEntity<ProductDTO> updatedResponse = restTemplate.getForEntity(
                "/api/products/" + createdProduct.getId(), ProductDTO.class);
                
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedResponse.getBody().getName()).isEqualTo("更新后的API测试商品");
        
        // 5. 删除商品
        restTemplate.delete("/api/products/" + createdProduct.getId());
        
        // 6. 验证删除
        ResponseEntity<ProductDTO> deletedResponse = restTemplate.getForEntity(
                "/api/products/" + createdProduct.getId(), ProductDTO.class);
                
        assertThat(deletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

## 5. 性能测试

### 5.1 JMH基准测试
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 10)
public class ProductServiceBenchmark {
    
    private ProductService productService;
    private ProductRepository productRepository;
    
    @Setup(Level.Trial)
    public void setup() {
        // 初始化测试环境
        productRepository = mock(ProductRepository.class);
        CacheService cacheService = mock(CacheService.class);
        productService = new ProductServiceImpl(productRepository, cacheService);
    }
    
    @Benchmark
    public ProductDTO getProductById_Benchmark() {
        // 模拟商品查找场景
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("基准测试商品")
                .productCode("BENCH001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        return productService.getProductById(productId);
    }
}
```

### 5.2 负载测试配置
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiLoadTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("高并发商品查询测试")
    void testHighConcurrencyProductQuery() throws InterruptedException {
        int threadCount = 10;
        int requestCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // 创建测试商品
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .name("负载测试商品")
                .productCode("LOAD001")
                .purchasePrice(new BigDecimal("100.00"))
                .sellingPrice(new BigDecimal("150.00"))
                .build();
                
        ResponseEntity<ProductDTO> createResponse = restTemplate.postForEntity(
                "/api/products", createRequest, ProductDTO.class);
                
        Long productId = createResponse.getBody().getId();
        
        // 并发查询测试
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < requestCount; j++) {
                        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(
                                "/api/products/" + productId, ProductDTO.class);
                                
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // 等待所有线程完成
        latch.await(30, TimeUnit.SECONDS);
        
        // 验证结果
        assertThat(successCount.get()).isEqualTo(threadCount * requestCount);
        assertThat(errorCount.get()).isEqualTo(0);
    }
}
```

## 6. 测试覆盖率和质量保证

### 6.1 JaCoCo配置
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 6.2 测试质量检查
```java
@ExtendWith(MockitoExtension.class)
class TestQualityCheck {
    
    @Test
    @DisplayName("确保所有核心业务方法都有测试覆盖")
    void testAllCoreMethodsHaveCoverage() {
        // 使用反射检查ProductService接口的所有方法是否都有对应的测试
        Method[] methods = ProductService.class.getDeclaredMethods();
        
        for (Method method : methods) {
            // 检查是否在测试类中有对应的测试方法
            boolean hasTest = hasTestMethod(method);
            assertThat(hasTest)
                .withFailMessage("方法 %s 没有对应的测试", method.getName())
                .isTrue();
        }
    }
    
    private boolean hasTestMethod(Method method) {
        // 实现检查逻辑
        return true; // 简化示例
    }
}
```

## 7. 测试最佳实践

### 7.1 测试命名规范
```java
class ProductServiceImplTest {
    
    // 好的命名：描述测试场景和期望结果
    @Test
    void testGetProductById_ReturnsProduct_WhenProductExists() { }
    
    @Test
    void testGetProductById_ThrowsException_WhenProductNotFound() { }
    
    @Test
    void testCreateProduct_SavesProduct_WhenValidRequest() { }
    
    // 不好的命名：过于简单或模糊
    @Test
    void testGetProduct() { } // 不清楚测试什么场景
    
    @Test
    void test1() { } // 无意义的命名
}
```

### 7.2 测试数据管理
```java
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TestDataManagementTest {
    
    @BeforeEach
    void setUpTestData() {
        // 在每个测试前设置测试数据
    }
    
    @AfterEach
    void cleanUpTestData() {
        // 在每个测试后清理测试数据
    }
    
    @Test
    @Sql(scripts = "/test-data.sql") // 使用SQL脚本初始化数据
    void testWithSqlScript() {
        // 测试逻辑
    }
}
```

本指南为进销存管理系统的测试策略提供了全面的规范和实现指导，所有开发团队应严格遵循这些规范进行测试开发，确保系统质量和稳定性。