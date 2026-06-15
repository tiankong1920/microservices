# 集成测试指南

## 文档概述

本文档提供企业级分布式应用集成测试的完整指南，涵盖集成测试策略、Testcontainers使用、测试环境隔离、测试数据管理和最佳实践。

## 目录

1. [集成测试概述](#集成测试概述)
2. [集成测试策略](#集成测试策略)
3. [Testcontainers使用指南](#testcontainers使用指南)
4. [测试环境隔离](#测试环境隔离)
5. [测试数据管理](#测试数据管理)
6. [Spring Boot测试配置](#spring-boot测试配置)
7. [集成测试最佳实践](#集成测试最佳实践)
8. [常见问题与解决方案](#常见问题与解决方案)

## 集成测试概述

### 集成测试定义

集成测试是验证多个组件或服务协同工作的测试类型，主要关注：
- 组件之间的接口和交互
- 数据流和控制流
- 外部依赖集成（数据库、缓存、消息队列等）
- 系统级功能和性能

### 集成测试目标

- **验证集成点**：确保组件之间的接口正确工作
- **检测集成问题**：发现组件集成时出现的问题
- **验证端到端流程**：测试完整的业务流程
- **确保系统稳定性**：验证系统在各种条件下的稳定性

### 集成测试范围

集成测试应覆盖以下范围：
- 数据库集成测试
- 缓存集成测试
- 消息队列集成测试
- 外部API集成测试
- 服务间调用测试
- 事务管理测试

## 集成测试策略

### 测试金字塔原则

```
        /\
       /  \
      / E2E \        少量端到端测试
     /--------\
    / 集成测试 \      适量集成测试
   /------------\
  /   单元测试    \   大量单元测试
 /----------------\
```

### 集成测试分层

#### 1. 组件级集成测试

测试单个组件与外部依赖的集成：

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("test@example.com", "John Doe");
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

#### 2. 服务级集成测试

测试服务层与多个依赖的集成：

```java
@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void shouldCreateOrderAndCacheResult() {
        OrderRequest request = new OrderRequest("product-123", 2);
        Order order = orderService.createOrder(request);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        Order cachedOrder = orderService.getOrder(order.getId());
        assertThat(cachedOrder).isNotNull();
        assertThat(cachedOrder.getId()).isEqualTo(order.getId());
    }
}
```

#### 3. 系统级集成测试

测试完整的应用程序和外部系统：

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SystemIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldProcessCompleteOrderFlow() {
        CreateOrderRequest request = new CreateOrderRequest("product-123", 2);
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/orders", request, OrderResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderResponse order = createResponse.getBody();
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();

        ResponseEntity<OrderResponse> getResponse = restTemplate.getForEntity(
                "/api/orders/" + order.getId(), OrderResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
```

### 测试策略选择

| 测试类型 | 使用场景 | 优势 | 劣势 |
|---------|---------|------|------|
| 组件级集成 | 单个组件与依赖集成 | 快速、隔离性好 | 覆盖范围有限 |
| 服务级集成 | 多个服务协同工作 | 覆盖面广 | 执行较慢 |
| 系统级集成 | 完整业务流程 | 最接近生产环境 | 执行最慢、维护成本高 |

## Testcontainers使用指南

### Testcontainers简介

Testcontainers是一个Java库，支持JUnit测试，提供轻量级的、一次性的数据库、消息队列、浏览器等容器的实例。

### Maven依赖配置

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
```

### PostgreSQL容器配置

#### 基础配置

```java
@Testcontainers
class PostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldConnectToPostgreSQL() {
        assertThat(postgres.isRunning()).isTrue();
    }
}
```

#### 高级配置

```java
@Testcontainers
class AdvancedPostgreSQLTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("data.sql"),
                "/docker-entrypoint-initdb.d/data.sql"
            )
            .withCommand("postgres", "-c", "max_connections=200")
            .withSharedMemorySize(1024 * 1024 * 256L)
            .withReuse(false)
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void setup() {
        postgres.start();
    }

    @AfterAll
    static void cleanup() {
        postgres.stop();
    }

    @Test
    void shouldExecuteComplexQuery() {
        try (Connection connection = postgres.createConnection("")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isGreaterThan(0);
        } catch (SQLException e) {
            fail("Failed to execute query", e);
        }
    }
}
```

### Redis容器配置

```java
@Testcontainers
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--maxmemory", "256mb", "--maxmemory-policy", "allkeys-lru");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void shouldConnectToRedis() {
        assertThat(redis.isRunning()).isTrue();
        assertThat(redis.getMappedPort(6379)).isNotNull();
    }
}
```

### Kafka容器配置

```java
@Testcontainers
class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Container
    static GenericContainer<?> zookeeper = new GenericContainer<>(
            DockerImageName.parse("confluentinc/cp-zookeeper:7.5.0")
    )
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
            .withExposedPorts(2181);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldProduceAndConsumeMessage() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(producerProps);
        producer.send(new ProducerRecord<>("test-topic", "key", "value"));
        producer.close();

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("test-topic"));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(1);

        consumer.close();
    }
}
```

### 多容器组合

```java
@Testcontainers
class MultiContainerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .dependsOn(postgres);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    )
            .dependsOn(redis);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldStartAllContainers() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
    }
}
```

### 自定义容器

```java
@Testcontainers
class CustomContainerTest {

    @Container
    static GenericContainer<?> customService = new GenericContainer<>(
            DockerImageName.parse("my-custom-service:1.0.0")
    )
            .withExposedPorts(8080)
            .withEnv("SERVICE_ENV", "test")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("config/test-config.yml"),
                "/app/config/config.yml"
            )
            .waitingFor(Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    @Test
    void shouldConnectToCustomService() {
        assertThat(customService.isRunning()).isTrue();
        String baseUrl = String.format("http://%s:%d",
                customService.getHost(),
                customService.getMappedPort(8080));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 测试环境隔离

### 测试环境隔离原则

1. **独立容器**：每个测试使用独立的容器实例
2. **独立数据库**：每个测试使用独立的数据库schema
3. **独立缓存**：每个测试使用独立的缓存namespace
4. **独立队列**：每个测试使用独立的topic或queue
5. **资源清理**：测试完成后清理所有资源

### 数据库Schema隔离

```java
@Testcontainers
class SchemaIsolationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @BeforeEach
    void setupSchema() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS test_" + UUID.randomUUID());
        jdbcTemplate.execute("SET search_path TO test_" + UUID.randomUUID());
    }

    @AfterEach
    void cleanupSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS test_" + UUID.randomUUID() + " CASCADE");
    }

    @Test
    void shouldUseIsolatedSchema() {
        jdbcTemplate.execute("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(100))");
        jdbcTemplate.update("INSERT INTO test_table (name) VALUES (?)", "test");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_table", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
```

### Redis Namespace隔离

```java
@SpringBootTest
@Testcontainers
class RedisIsolationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    private String getTestNamespace() {
        return "test:" + UUID.randomUUID().toString();
    }

    @BeforeEach
    void setupNamespace() {
        String namespace = getTestNamespace();
        redisTemplate.opsForValue().set(namespace + ":key", "value");
    }

    @AfterEach
    void cleanupNamespace() {
        String namespace = getTestNamespace();
        Set<String> keys = redisTemplate.keys(namespace + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldUseIsolatedNamespace() {
        String namespace = getTestNamespace();
        String value = redisTemplate.opsForValue().get(namespace + ":key");
        assertThat(value).isEqualTo("value");
    }
}
```

### Kafka Topic隔离

```java
@SpringBootTest
@Testcontainers
class KafkaIsolationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private String getTestTopic() {
        return "test-topic-" + UUID.randomUUID().toString();
    }

    @BeforeEach
    void setupTopic() {
        AdminClient adminClient = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()
        ));
        adminClient.createTopics(Collections.singletonList(
                new NewTopic(getTestTopic(), 1, (short) 1)
        ));
        adminClient.close();
    }

    @AfterEach
    void cleanupTopic() {
        AdminClient adminClient = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()
        ));
        adminClient.deleteTopics(Collections.singletonList(getTestTopic()));
        adminClient.close();
    }

    @Test
    void shouldUseIsolatedTopic() {
        String topic = getTestTopic();
        kafkaTemplate.send(topic, "key", "value");

        Consumer<String, String> consumer = createConsumer(topic);
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(1);
        consumer.close();
    }

    private Consumer<String, String> createConsumer(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }
}
```

### 测试并行执行隔离

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParallelTestIsolationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    private ThreadLocal<String> testId = new ThreadLocal<>();

    @BeforeEach
    void setupTestId() {
        testId.set(UUID.randomUUID().toString());
    }

    @AfterEach
    void cleanupTestId() {
        testId.remove();
    }

    @Test
    @Order(1)
    void test1() {
        String id = testId.get();
        assertThat(id).isNotNull();
    }

    @Test
    @Order(2)
    void test2() {
        String id = testId.get();
        assertThat(id).isNotNull();
        assertThat(id).isNotEqualTo(testId.get());
    }
}
```

## 测试数据管理

### 测试数据准备策略

#### 1. SQL脚本初始化

```java
@Testcontainers
class SqlScriptDataTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("test-data.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    void shouldLoadTestDataFromScript() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(postgres.getJdbcUrl(),
                        postgres.getUsername(), postgres.getPassword())
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(count).isGreaterThan(0);
    }
}
```

#### 2. TestDataSetup注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDataSetup {
    String[] scripts();
}

public class TestDataSetupExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method method = context.getRequiredTestMethod();
        TestDataSetup annotation = method.getAnnotation(TestDataSetup.class);

        if (annotation != null) {
            DataSource dataSource = getDataSource(context);
            for (String script : annotation.scripts()) {
                Resource resource = new ClassPathResource(script);
                ScriptUtils.executeSqlScript(dataSource.getConnection(), resource);
            }
        }
    }

    private DataSource getDataSource(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context)
                .getBean(DataSource.class);
    }
}

@SpringBootTest
@Testcontainers
class AnnotationDataTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @TestDataSetup(scripts = {"test-data/users.sql", "test-data/products.sql"})
    void shouldLoadTestDataFromAnnotation() {
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class);
        assertThat(userCount).isGreaterThan(0);

        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        assertThat(productCount).isGreaterThan(0);
    }
}
```

#### 3. Builder模式数据构建

```java
public class TestDataBuilder {

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static ProductBuilder product() {
        return new ProductBuilder();
    }

    public static OrderBuilder order() {
        return new OrderBuilder();
    }

    public static class UserBuilder {
        private String email = "test@example.com";
        private String name = "Test User";
        private String phone = "1234567890";

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public User build() {
            return new User(email, name, phone);
        }
    }

    public static class ProductBuilder {
        private String name = "Test Product";
        private BigDecimal price = BigDecimal.valueOf(99.99);
        private Integer stock = 100;

        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductBuilder withStock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public Product build() {
            return new Product(name, price, stock);
        }
    }

    public static class OrderBuilder {
        private Long userId;
        private List<OrderItem> items = new ArrayList<>();
        private OrderStatus status = OrderStatus.CREATED;

        public OrderBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public OrderBuilder withItem(Long productId, Integer quantity) {
            this.items.add(new OrderItem(productId, quantity));
            return this;
        }

        public OrderBuilder withStatus(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Order build() {
            return new Order(userId, items, status);
        }
    }
}

@SpringBootTest
@Testcontainers
class BuilderDataTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateTestDataWithBuilder() {
        User user = TestDataBuilder.user()
                .withEmail("user@example.com")
                .withName("John Doe")
                .build();
        User savedUser = userRepository.save(user);

        Product product = TestDataBuilder.product()
                .withName("Laptop")
                .withPrice(BigDecimal.valueOf(999.99))
                .withStock(50)
                .build();
        Product savedProduct = productRepository.save(product);

        Order order = TestDataBuilder.order()
                .withUserId(savedUser.getId())
                .withItem(savedProduct.getId(), 2)
                .build();
        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(1);
    }
}
```

### 测试数据清理

#### 1. @Transactional清理

```java
@SpringBootTest
@Testcontainers
class TransactionalCleanupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void shouldRollbackAfterTest() {
        User user = new User("test@example.com", "Test User");
        userRepository.save(user);

        assertThat(userRepository.count()).isGreaterThan(0);
    }

    @Test
    void shouldNotSeePreviousTestData() {
        assertThat(userRepository.count()).isEqualTo(0);
    }
}
```

#### 2. @DirtiesContext清理

```java
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DirtiesContextCleanupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldCreateData() {
        User user = new User("test@example.com", "Test User");
        userRepository.save(user);

        assertThat(userRepository.count()).isGreaterThan(0);
    }

    @Test
    void shouldHaveCleanContext() {
        assertThat(userRepository.count()).isEqualTo(0);
    }
}
```

#### 3. 手动清理

```java
@SpringBootTest
@Testcontainers
class ManualCleanupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE products CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders CASCADE");

        Set<String> keys = redisTemplate.keys("test:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldCreateAndCleanupData() {
        jdbcTemplate.update("INSERT INTO users (email, name) VALUES (?, ?)",
                "test@example.com", "Test User");
        redisTemplate.opsForValue().set("test:key", "value");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(1);
        assertThat(redisTemplate.hasKey("test:key")).isTrue();
    }

    @Test
    void shouldHaveCleanDatabase() {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(0);
        assertThat(redisTemplate.hasKey("test:key")).isFalse();
    }
}
```

### 测试数据管理最佳实践

1. **使用Builder模式**：使用Builder模式构建测试数据，提高可读性和可维护性
2. **数据隔离**：每个测试使用独立的数据集，避免测试间相互影响
3. **及时清理**：测试完成后及时清理测试数据，避免数据污染
4. **使用事务**：对于数据库测试，使用@Transactional自动回滚
5. **避免硬编码**：使用配置文件或Builder模式，避免硬编码测试数据
6. **数据复用**：提取公共测试数据到共享方法或类中

## Spring Boot测试配置

### 测试配置类

```java
@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public DataSource testDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> testRedisTemplate(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory(host, port));
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(String host, int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> testKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        ProducerFactory<String, String> factory =
                new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }
}
```

### 测试配置文件

#### application-test.yml

```yaml
spring:
  datasource:
    url: ${TEST_DB_URL:jdbc:h2:mem:testdb}
    username: ${TEST_DB_USER:sa}
    password: ${TEST_DB_PASSWORD:}
    driver-class-name: org.h2.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect

  redis:
    host: ${TEST_REDIS_HOST:localhost}
    port: ${TEST_REDIS_PORT:6379}
    database: 15
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  kafka:
    bootstrap-servers: ${TEST_KAFKA_SERVERS:localhost:9092}
    consumer:
      group-id: test-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3

  cache:
    type: redis
    redis:
      time-to-live: 60000

logging:
  level:
    root: INFO
    com.inventory: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

testcontainers:
  reuse: false
  docker:
    image-prefix: testcontainers/
```

### 测试配置使用

```java
@SpringBootTest
@Import(IntegrationTestConfig.class)
@TestPropertySource(locations = "classpath:application-test.yml")
class IntegrationTestConfigExample {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldUseTestConfiguration() {
        assertThat(dataSource).isNotNull();
        assertThat(redisTemplate).isNotNull();
        assertThat(kafkaTemplate).isNotNull();
    }
}
```

### 测试切片配置

#### JPA测试

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(IntegrationTestConfig.class)
class JpaSliceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("test@example.com", "Test User");
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
    }
}
```

#### Redis测试

```java
@DataRedisTest
@Import(IntegrationTestConfig.class)
class RedisSliceTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void shouldSaveAndRetrieveValue() {
        redisTemplate.opsForValue().set("test:key", "test-value");

        String value = redisTemplate.opsForValue().get("test:key");
        assertThat(value).isEqualTo("test-value");
    }
}
```

#### Kafka测试

```java
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
})
class KafkaSliceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Test
    void shouldSendAndReceiveMessage() throws Exception {
        kafkaTemplate.send("test-topic", "key", "value");

        CountDownLatch latch = new CountDownLatch(1);
        kafkaTemplate.executeInTransaction(template -> {
            template.send("test-topic", "key", "value");
            return null;
        });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
```

## 集成测试最佳实践

### 1. 测试设计原则

#### FIRST原则

- **F**ast（快速）：测试应该快速执行
- **I**ndependent（独立）：测试之间应该相互独立
- **R**epeatable（可重复）：测试应该可以重复执行
- **S**elf-Validating（自验证）：测试应该自动验证结果
- **T**imely（及时）：测试应该及时编写

#### AAA原则

- **A**rrange（准备）：准备测试数据和测试环境
- **A**ct（执行）：执行被测试的代码
- **A**ssert（断言）：验证执行结果

```java
@Test
void shouldCreateOrder() {
    Arrange
    User user = TestDataBuilder.user().build();
    Product product = TestDataBuilder.product().build();
    OrderRequest request = new OrderRequest(user.getId(), product.getId(), 2);

    Act
    Order order = orderService.createOrder(request);

    Assert
    assertThat(order.getId()).isNotNull();
    assertThat(order.getUserId()).isEqualTo(user.getId());
    assertThat(order.getItems()).hasSize(1);
}
```

### 2. 测试命名规范

#### 测试类命名

```java
// 好的命名
class OrderServiceIntegrationTest { }
class UserRepositoryIntegrationTest { }
class PaymentControllerIntegrationTest { }

// 不好的命名
class OrderTest { }
class Test1 { }
class IntegrationTests { }
```

#### 测试方法命名

```java
@Test
void shouldCreateOrderWhenRequestIsValid() { }

@Test
void shouldThrowExceptionWhenProductNotFound() { }

@Test
void shouldReturnEmptyListWhenNoOrdersExist() { }

@Test
void shouldUpdateOrderStatusWhenPaymentCompleted() { }
```

### 3. 测试断言原则

#### 使用AssertJ

```java
@Test
void shouldUseAssertJAssertions() {
    Order order = orderService.getOrder(1L);

    assertThat(order)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("status", OrderStatus.CREATED);

    assertThat(order.getItems())
            .isNotEmpty()
            .hasSize(2)
            .allMatch(item -> item.getQuantity() > 0);

    assertThat(order.getTotalAmount())
            .isGreaterThan(BigDecimal.ZERO)
            .isLessThan(BigDecimal.valueOf(10000));
}
```

#### 断言消息

```java
@Test
void shouldProvideMeaningfulAssertionMessages() {
    Order order = orderService.getOrder(1L);

    assertThat(order)
            .as("Order should not be null")
            .isNotNull();

    assertThat(order.getStatus())
            .as("Order status should be CREATED, but was %s", order.getStatus())
            .isEqualTo(OrderStatus.CREATED);
}
```

### 4. 测试数据管理

#### 使用测试数据构建器

```java
@Test
void shouldUseTestDataBuilder() {
    User user = TestDataBuilder.user()
            .withEmail("user@example.com")
            .withName("John Doe")
            .build();

    assertThat(user.getEmail()).isEqualTo("user@example.com");
    assertThat(user.getName()).isEqualTo("John Doe");
}
```

#### 避免硬编码

```java
// 不好的做法
@Test
void shouldNotHardcodeData() {
    User user = new User("hardcoded@example.com", "Hardcoded Name");
    userRepository.save(user);
}

// 好的做法
@Test
void shouldUseTestData() {
    User user = TestDataBuilder.user()
            .withEmail("user@example.com")
            .build();
    userRepository.save(user);
}
```

### 5. 测试隔离

#### 使用@Transactional

```java
@Test
@Transactional
void shouldRollbackAfterTest() {
    User user = new User("test@example.com", "Test User");
    userRepository.save(user);

    assertThat(userRepository.count()).isGreaterThan(0);
}
```

#### 使用@DirtiesContext

```java
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IsolatedTest {
    @Test
    void test1() { }
    @Test
    void test2() { }
}
```

### 6. 测试性能优化

#### 使用Testcontainers重用

```java
@Testcontainers
class ReusableContainerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withReuse(true);

    @Test
    void test1() { }
    @Test
    void test2() { }
}
```

#### 并行执行测试

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParallelTest {

    @Test
    @Order(1)
    void test1() { }

    @Test
    @Order(2)
    void test2() { }
}
```

### 7. 测试维护

#### 定期更新测试

```java
@Test
void shouldKeepTestsUpToDate() {
    Order order = orderService.createOrder(request);

    assertThat(order)
            .as("Order should have all required fields")
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrProperty("status")
            .hasFieldOrProperty("createdAt")
            .hasFieldOrProperty("updatedAt");
}
```

#### 移除过时测试

```java
@Disabled("This feature has been deprecated")
@Deprecated
class DeprecatedFeatureTest {
    @Test
    void shouldNotTestDeprecatedFeature() {
    }
}
```

## 常见问题与解决方案

### 问题1：Testcontainers启动失败

**问题描述**：Testcontainers容器启动超时或失败

**解决方案**：

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withStartupTimeout(Duration.ofMinutes(2))
        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                new HostConfig()
                        .withMemory(512 * 1024 * 1024L)
                        .withCpuShares(512L)
        ));
```

### 问题2：数据库连接池耗尽

**问题描述**：集成测试时数据库连接池耗尽

**解决方案**：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 问题3：测试数据污染

**问题描述**：测试之间数据相互影响

**解决方案**：

```java
@AfterEach
void cleanup() {
    jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    jdbcTemplate.execute("TRUNCATE TABLE orders CASCADE");

    Set<String> keys = redisTemplate.keys("test:*");
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
    }
}
```

### 问题4：Kafka消息消费超时

**问题描述**：Kafka消息发送成功但消费超时

**解决方案**：

```java
@Test
void shouldConsumeKafkaMessage() throws Exception {
    kafkaTemplate.send("test-topic", "key", "value");

    CountDownLatch latch = new CountDownLatch(1);
    Consumer<String, String> consumer = createConsumer();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
        while (latch.getCount() > 0) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                latch.countDown();
            }
        }
    });

    assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    executor.shutdown();
    consumer.close();
}
```

### 问题5：Redis连接失败

**问题描述**：Redis容器启动但连接失败

**解决方案**：

```java
@Container
static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    registry.add("spring.redis.timeout", () -> "5000ms");
}
```

### 问题6：测试执行缓慢

**问题描述**：集成测试执行时间过长

**解决方案**：

```java
@Testcontainers
class PerformanceOptimizedTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withReuse(true)
            .withStartupTimeout(Duration.ofMinutes(1));

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void shouldSkipInCI() {
    }
}
```

### 问题7：端口冲突

**问题描述**：多个测试使用相同端口导致冲突

**解决方案**：

```java
@Container
static GenericContainer<?> service = new GenericContainer<>("service:1.0.0")
        .withExposedPorts(8080)
        .withRandomExposedPorts();

@Test
void shouldUseRandomPort() {
    int port = service.getMappedPort(8080);
    assertThat(port).isNotEqualTo(8080);
}
```

### 问题8：内存不足

**问题描述**：测试执行时内存不足

**解决方案**：

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withSharedMemorySize(1024 * 1024 * 256L)
        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                new HostConfig()
                        .withMemory(1024 * 1024 * 1024L)
                        .withMemorySwap(2048 * 1024 * 1024L)
        ));
```

## 相关文档

- [单元测试指南](UnitTestingGuide.md)
- [性能测试指南](PerformanceTestingGuide.md)
- [测试自动化指南](TestAutomationGuide.md)
- [测试最佳实践指南](TestBestPracticesGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |