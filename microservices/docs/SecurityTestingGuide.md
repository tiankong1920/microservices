# 安全测试指南

## 文档概述

本文档提供企业级分布式应用安全测试的完整指南，涵盖安全测试策略、OWASP Top 10测试、认证和授权测试、API安全测试、数据安全测试和安全漏洞扫描工具使用。

## 目录

1. [安全测试概述](#安全测试概述)
2. [安全测试策略](#安全测试策略)
3. [OWASP Top 10测试](#owasp-top-10测试)
4. [认证和授权测试](#认证和授权测试)
5. [API安全测试](#api安全测试)
6. [数据安全测试](#数据安全测试)
7. [安全漏洞扫描工具使用](#安全漏洞扫描工具使用)
8. [安全测试最佳实践](#安全测试最佳实践)

## 安全测试概述

### 安全测试定义

安全测试是通过模拟攻击者的行为，评估系统安全性的测试类型，旨在发现系统中的安全漏洞和风险。

### 安全测试目标

- **发现安全漏洞**：识别系统中的安全漏洞和弱点
- **评估安全风险**：评估安全漏洞的风险等级和影响范围
- **验证安全控制**：验证安全控制措施的有效性
- **符合合规要求**：确保系统符合安全合规要求
- **提升安全意识**：提高开发团队的安全意识

### 安全测试类型

#### 1. 静态应用安全测试（SAST）

在不运行应用程序的情况下，分析源代码、字节码或二进制代码，发现安全漏洞。

**工具**：
- SonarQube
- Checkmarx
- Fortify
- FindSecBugs

**优势**：
- 早期发现漏洞
- 覆盖率高
- 自动化程度高

**劣势**：
- 误报率高
- 无法检测运行时问题
- 需要访问源代码

#### 2. 动态应用安全测试（DAST）

在应用程序运行时，通过模拟攻击来发现安全漏洞。

**工具**：
- OWASP ZAP
- Burp Suite
- Nessus
- Acunetix

**优势**：
- 模拟真实攻击
- 检测运行时问题
- 不需要源代码

**劣势**：
- 覆盖率有限
- 需要应用运行
- 可能影响生产环境

#### 3. 交互式应用安全测试（IAST）

在应用程序运行时，通过插桩技术监控应用程序的行为，发现安全漏洞。

**工具**：
- Contrast Security
- Seeker
- HCL AppScan

**优势**：
- 结合SAST和DAST优势
- 实时监控
- 精确度高

**劣势**：
- 需要应用插桩
- 性能开销
- 部署复杂

#### 4. 软件成分分析（SCA）

分析应用程序使用的第三方库和组件，发现已知的安全漏洞。

**工具**：
- OWASP Dependency-Check
- Snyk
- Black Duck
- WhiteSource

**优势**：
- 快速发现依赖漏洞
- 自动化程度高
- 支持多种语言

**劣势**：
- 只能检测已知漏洞
- 无法检测自定义代码
- 误报率高

#### 5. 渗透测试

模拟真实的攻击，对系统进行全面的安全测试。

**工具**：
- Metasploit
- Nmap
- Wireshark
- SQLMap

**优势**：
- 模拟真实攻击
- 全面覆盖
- 发现深层次问题

**劣势**：
- 成本高
- 需要专业技能
- 时间消耗大

### 安全测试指标

| 指标 | 说明 | 目标值 |
|------|------|--------|
| 漏洞发现率 | 发现的漏洞数量 | 最大化 |
| 严重漏洞数 | 严重级别漏洞数量 | 0 |
| 高危漏洞数 | 高危级别漏洞数量 | 0 |
| 中危漏洞数 | 中危级别漏洞数量 | < 5 |
| 低危漏洞数 | 低危级别漏洞数量 | < 10 |
| 漏洞修复率 | 已修复漏洞占比 | 100% |
| 漏洞修复时间 | 漏洞平均修复时间 | < 7天 |
| 安全覆盖率 | 安全测试覆盖率 | > 90% |

## 安全测试策略

### 测试策略制定

#### 1. 确定测试范围

```yaml
security_test_scope:
  applications:
    - name: "用户服务"
      endpoints:
        - "/api/auth/login"
        - "/api/auth/register"
        - "/api/users/*"
    - name: "订单服务"
      endpoints:
        - "/api/orders/*"
        - "/api/payments/*"
    - name: "商品服务"
      endpoints:
        - "/api/products/*"
        - "/api/categories/*"
  
  components:
    - "认证授权"
    - "API接口"
    - "数据存储"
    - "数据传输"
    - "第三方集成"
  
  security_controls:
    - "身份认证"
    - "访问控制"
    - "数据加密"
    - "输入验证"
    - "日志审计"
```

#### 2. 选择测试类型

```yaml
test_types:
  sast:
    enabled: true
    frequency: "每次构建"
    tools:
      - "SonarQube"
      - "FindSecBugs"
  
  dast:
    enabled: true
    frequency: "每周"
    tools:
      - "OWASP ZAP"
      - "Burp Suite"
  
  iast:
    enabled: true
    frequency: "每次部署"
    tools:
      - "Contrast Security"
  
  sca:
    enabled: true
    frequency: "每次构建"
    tools:
      - "OWASP Dependency-Check"
      - "Snyk"
  
  penetration_test:
    enabled: true
    frequency: "每季度"
    tools:
      - "Metasploit"
      - "Nmap"
```

#### 3. 定义测试场景

```yaml
test_scenarios:
  authentication:
    - "弱密码测试"
    - "暴力破解测试"
    - "会话劫持测试"
    - "会话固定测试"
    - "多因素认证测试"
  
  authorization:
    - "越权访问测试"
    - "水平越权测试"
    - "垂直越权测试"
    - "权限提升测试"
    - "资源访问测试"
  
  input_validation:
    - "SQL注入测试"
    - "XSS攻击测试"
    - "CSRF攻击测试"
    - "命令注入测试"
    - "文件上传测试"
  
  data_security:
    - "敏感数据泄露测试"
    - "数据加密测试"
    - "数据完整性测试"
    - "数据备份测试"
    - "数据销毁测试"
```

### 测试环境准备

#### 1. 环境配置

```yaml
test_environment:
  infrastructure:
    servers:
      - name: "test-server-1"
        os: "Ubuntu 22.04 LTS"
        security_tools:
          - "OWASP ZAP"
          - "Burp Suite"
          - "Nmap"
          - "Wireshark"
  
  network:
    isolation: true
    firewall: enabled
    monitoring: enabled
  
  data:
    type: "anonymized"
    volume: "100GB"
    backup: true
  
  access_control:
    authentication: true
    authorization: true
    audit_logging: true
```

#### 2. 测试数据准备

```sql
-- 准备测试用户数据
INSERT INTO users (email, password, name, role) 
VALUES 
    ('test_user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test User', 'USER'),
    ('test_admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test Admin', 'ADMIN'),
    ('test_hacker@example.com', 'password', 'Test Hacker', 'USER');

-- 准备测试订单数据
INSERT INTO orders (user_id, status, total_amount) 
VALUES 
    (1, 'COMPLETED', 100.00),
    (1, 'PENDING', 200.00),
    (2, 'COMPLETED', 300.00);
```

## OWASP Top 10测试

### OWASP Top 10 2021

#### 1. A01:2021 - 访问控制失效（Broken Access Control）

**问题描述**：访问控制失效是指用户可以访问超出其权限范围的数据或功能。

**测试方法**：

```java
@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldNotAllowUserToAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldNotAllowUserToAccessOtherUsersData() throws Exception {
        Long otherUserId = 999L;
        
        mockMvc.perform(get("/api/users/" + otherUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldNotAllowHorizontalPrivilegeEscalation() throws Exception {
        Long otherUserId = 999L;
        
        mockMvc.perform(put("/api/users/" + otherUserId + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }
}
```

**防护措施**：

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

#### 2. A02:2021 - 加密失效（Cryptographic Failures）

**问题描述**：加密失效是指敏感数据未加密或使用了弱加密算法。

**测试方法**：

```java
@SpringBootTest
class EncryptionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldEncryptPasswords() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void shouldNotStoreSensitiveDataInPlaintext() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setCreditCard("4111111111111111");

        User savedUser = userService.save(user);

        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(savedUser.getCreditCard()).isNull();
    }

    @Test
    void shouldUseStrongEncryptionAlgorithm() {
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        assertThat(encodedPassword).startsWith("$2a$");
    }
}
```

**防护措施**：

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public EncryptionService encryptionService() {
        return new AesEncryptionService();
    }
}

@Service
public class AesEncryptionService implements EncryptionService {

    private static final String SECRET_KEY = "my-secret-key-256-bits";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @Override
    public String encrypt(String plaintext) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public String decrypt(String ciphertext) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decryptedBytes);
    }
}
```

#### 3. A03:2021 - 注入（Injection）

**问题描述**：注入是指攻击者通过输入恶意数据来执行非预期的命令或查询。

**测试方法**：

```java
@SpringBootTest
@AutoConfigureMockMvc
class InjectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPreventSQLInjection() throws Exception {
        String maliciousInput = "' OR '1'='1";
        
        mockMvc.perform(get("/api/products")
                .param("name", maliciousInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldPreventXSS() throws Exception {
        String maliciousInput = "<script>alert('XSS')</script>";
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + maliciousInput + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventCommandInjection() throws Exception {
        String maliciousInput = "; cat /etc/passwd";
        
        mockMvc.perform(post("/api/files")
                .param("filename", maliciousInput))
                .andExpect(status().isBadRequest());
    }
}
```

**防护措施**：

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        
        Specification<Product> spec = Specification.where(null);
        
        if (StringUtils.hasText(name)) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        
        if (StringUtils.hasText(category)) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("category"), category));
        }
        
        Page<Product> products = productRepository.findAll(spec, PageRequest.of(0, 20));
        return ResponseEntity.ok(products);
    }
}

@Component
public class XSSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        XssHttpServletRequestWrapper wrappedRequest = 
            new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return cleanXSS(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] cleanValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanValues[i] = cleanXSS(values[i]);
        }
        return cleanValues;
    }

    private String cleanXSS(String value) {
        if (value == null) {
            return null;
        }
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        value = value.replaceAll("'", "&#39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        return value;
    }
}
```

#### 4. A04:2021 - 不安全设计（Insecure Design）

**问题描述**：不安全设计是指系统在设计阶段就存在安全缺陷。

**测试方法**：

```java
@SpringBootTest
class InsecureDesignTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Test
    void shouldValidateBusinessRules() {
        User user = userService.findById(1L);
        Product product = new Product();
        product.setPrice(-100.00);
        product.setStock(10);

        assertThatThrownBy(() -> orderService.createOrder(user, product, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid price");
    }

    @Test
    void shouldPreventRaceConditions() {
        Product product = new Product();
        product.setStock(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Order>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> 
                orderService.createOrder(userService.findById(1L), product, 1)));
        }

        int successCount = 0;
        for (Future<Order> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (Exception e) {
            }
        }

        assertThat(successCount).isEqualTo(1);
        executor.shutdown();
    }
}
```

**防护措施**：

```java
@Service
@Transactional
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Order createOrder(User user, Product product, int quantity) {
        validateOrder(product, quantity);
        
        inventoryService.decreaseStock(product.getId(), quantity);
        
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalAmount(product.getPrice() * quantity);
        order.setStatus(OrderStatus.CREATED);
        
        return orderRepository.save(order);
    }

    private void validateOrder(Product product, int quantity) {
        if (product.getPrice() <= 0) {
            throw new BusinessException("Invalid price");
        }
        
        if (quantity <= 0) {
            throw new BusinessException("Invalid quantity");
        }
        
        if (product.getStock() < quantity) {
            throw new BusinessException("Insufficient stock");
        }
    }
}
```

#### 5. A05:2021 - 安全配置错误（Security Misconfiguration）

**问题描述**：安全配置错误是指系统配置不当导致的安全漏洞。

**测试方法**：

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotExposeErrorDetails() throws Exception {
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void shouldHaveSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }

    @Test
    void shouldNotExposeSensitiveEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }
}
```

**防护措施**：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers()
                .contentSecurityPolicy("default-src 'self'")
                .and()
                .xssProtection()
                .and()
                .frameOptions()
                .deny()
                .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/actuator/**").authenticated()
                .anyRequest().authenticated()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/h2-console/**");
    }
}

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "Internal Server Error",
            LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
```

#### 6. A06:2021 - 易受攻击和过时的组件（Vulnerable and Outdated Components）

**问题描述**：易受攻击和过时的组件是指使用了存在已知漏洞的第三方库。

**测试方法**：

```bash
# 使用OWASP Dependency-Check扫描依赖
dependency-check --scan ./pom.xml --out ./dependency-check-report.html

# 使用Snyk扫描依赖
snyk test

# 使用Maven插件扫描依赖
mvn org.owasp:dependency-check-maven:check
```

**防护措施**：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.owasp</groupId>
            <artifactId>dependency-check-maven</artifactId>
            <version>8.4.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <failBuildOnCVSS>7</failBuildOnCVSS>
                <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 7. A07:2021 - 身份识别和身份验证失败（Identification and Authentication Failures）

**问题描述**：身份识别和身份验证失败是指认证机制存在缺陷。

**测试方法**：

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void shouldRejectWeakPassword() throws Exception {
        String weakPassword = "123456";
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"" + weakPassword + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is too weak"));
    }

    @Test
    void shouldPreventBruteForce() throws Exception {
        String email = "test@example.com";
        String password = "wrongpassword";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldInvalidateSessionAfterLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

**防护措施**：

```java
@Service
public class AuthenticationService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public String login(String email, String password) {
        if (loginAttemptService.isBlocked(email)) {
            throw new AuthenticationException("Account is locked due to too many failed attempts");
        }

        User user = userService.findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.loginFailed(email);
            throw new AuthenticationException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(email);
        return jwtTokenProvider.generateToken(user);
    }

    public void register(String email, String password, String name) {
        if (!isPasswordStrong(password)) {
            throw new ValidationException("Password is too weak");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(Role.USER);
        userService.save(user);
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[!@#$%^&*].*");
    }
}

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME_MINUTES = 30;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void loginFailed(String email) {
        String key = "login_attempts:" + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, BLOCK_TIME_MINUTES, TimeUnit.MINUTES);

        if (attempts >= MAX_ATTEMPTS) {
            String blockKey = "login_blocked:" + email;
            redisTemplate.opsForValue().set(blockKey, "true", BLOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void loginSucceeded(String email) {
        String key = "login_attempts:" + email;
        redisTemplate.delete(key);
        String blockKey = "login_blocked:" + email;
        redisTemplate.delete(blockKey);
    }

    public boolean isBlocked(String email) {
        String blockKey = "login_blocked:" + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }
}
```

#### 8. A08:2021 - 软件和数据完整性失效（Software and Data Integrity Failures）

**问题描述**：软件和数据完整性失效是指代码或数据被篡改。

**测试方法**：

```java
@SpringBootTest
class IntegrityTest {

    @Autowired
    private OrderService orderService;

    @Test
    void shouldDetectDataTampering() {
        Order order = orderService.findById(1L);
        String originalHash = calculateHash(order);

        order.setTotalAmount(order.getTotalAmount().multiply(BigDecimal.TEN));
        String tamperedHash = calculateHash(order);

        assertThat(tamperedHash).isNotEqualTo(originalHash);
    }

    private String calculateHash(Order order) {
        String data = order.getId() + "|" + 
                     order.getUserId() + "|" + 
                     order.getTotalAmount() + "|" + 
                     order.getStatus();
        return DigestUtils.md5DigestAsHex(data.getBytes());
    }
}
```

**防护措施**：

```java
@Service
public class DigitalSignatureService {

    private static final String PRIVATE_KEY = "private-key";
    private static final String PUBLIC_KEY = "public-key";

    public String sign(String data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivateKey());
        signature.update(data.getBytes());
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    public boolean verify(String data, String signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublicKey());
        sig.update(data.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return sig.verify(signatureBytes);
    }

    private PrivateKey getPrivateKey() throws Exception {
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(PRIVATE_KEY.getBytes()));
    }

    private PublicKey getPublicKey() throws Exception {
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(PUBLIC_KEY.getBytes()));
    }
}
```

#### 9. A09:2021 - 安全日志和监控失效（Security Logging and Monitoring Failures）

**问题描述**：安全日志和监控失效是指缺乏足够的日志记录和监控。

**测试方法**：

```java
@SpringBootTest
class SecurityLoggingTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldLogAuthenticationAttempts() {
        try {
            userService.login("test@example.com", "wrongpassword");
        } catch (AuthenticationException e) {
        }

        List<ILoggingEvent> logs = getAppender().getLog();
        assertThat(logs).anyMatch(log -> 
            log.getFormattedMessage().contains("Authentication failed"));
    }

    @Test
    void shouldLogAuthorizationFailures() {
        try {
            userService.deleteUser(1L);
        } catch (AccessDeniedException e) {
        }

        List<ILoggingEvent> logs = getAppender().getLog();
        assertThat(logs).anyMatch(log -> 
            log.getFormattedMessage().contains("Access denied"));
    }
}
```

**防护措施**：

```java
@Aspect
@Component
public class SecurityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLoggingAspect.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @AfterReturning(pointcut = "execution(* com.inventory..*.*(..)) && @annotation(auditable)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String action = auditable.action();
        String details = buildDetails(joinPoint, result);

        logger.info("User {} performed action {}: {}", username, action, details);

        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @AfterThrowing(pointcut = "execution(* com.inventory..*.*(..)) && @annotation(auditable)", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String action = auditable.action();
        String details = buildDetails(joinPoint, null);

        logger.error("User {} failed to perform action {}: {}", username, action, exception.getMessage());

        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setStatus("FAILED");
        auditLog.setErrorMessage(exception.getMessage());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    private String buildDetails(JoinPoint joinPoint, Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method: ").append(joinPoint.getSignature().getName());
        sb.append(", Args: ").append(Arrays.toString(joinPoint.getArgs()));
        if (result != null) {
            sb.append(", Result: ").append(result.toString());
        }
        return sb.toString();
    }
}
```

#### 10. A10:2021 - 服务器端请求伪造（SSRF）

**问题描述**：服务器端请求伪造是指攻击者可以控制服务器发起的请求。

**测试方法**：

```java
@SpringBootTest
@AutoConfigureMockMvc
class SSRFTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPreventSSRF() throws Exception {
        String maliciousUrl = "http://localhost:6379";
        
        mockMvc.perform(post("/api/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + maliciousUrl + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid URL"));
    }

    @Test
    void shouldPreventInternalNetworkAccess() throws Exception {
        String internalUrl = "http://192.168.1.1/admin";
        
        mockMvc.perform(post("/api/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + internalUrl + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access to internal network is not allowed"));
    }
}
```

**防护措施**：

```java
@Service
public class UrlFetchService {

    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "example.com",
            "api.example.com"
    );

    public String fetchUrl(String url) throws Exception {
        if (!isValidUrl(url)) {
            throw new ValidationException("Invalid URL");
        }

        URL urlObj = new URL(url);
        String host = urlObj.getHost();

        if (!isAllowedDomain(host)) {
            throw new ValidationException("Access to this domain is not allowed");
        }

        if (isInternalNetwork(host)) {
            throw new ValidationException("Access to internal network is not allowed");
        }

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAllowedDomain(String host) {
        return ALLOWED_DOMAINS.stream().anyMatch(host::endsWith);
    }

    private boolean isInternalNetwork(String host) {
        return host.startsWith("192.168.") ||
               host.startsWith("10.") ||
               host.startsWith("172.16.") ||
               host.equals("localhost") ||
               host.equals("127.0.0.1");
    }
}
```

## 认证和授权测试

### 认证测试

#### 1. 密码策略测试

```java
@SpringBootTest
class PasswordPolicyTest {

    @Autowired
    private PasswordValidator passwordValidator;

    @Test
    void shouldRejectShortPassword() {
        String password = "short";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Password must be at least 8 characters");
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        String password = "lowercase123";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Password must contain at least one uppercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        String password = "UPPERCASE123";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Password must contain at least one lowercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutNumber() {
        String password = "NoNumbers";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Password must contain at least one number");
    }

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        String password = "NoSpecial123";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Password must contain at least one special character");
    }

    @Test
    void shouldAcceptStrongPassword() {
        String password = "Strong@Pass123";
        ValidationResult result = passwordValidator.validate(password);
        assertThat(result.isValid()).isTrue();
    }
}
```

#### 2. 会话管理测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class SessionManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateSessionOnLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andReturn();

        String sessionId = result.getResponse().getCookie("JSESSIONID").getValue();
        assertThat(sessionId).isNotNull();
    }

    @Test
    void shouldInvalidateSessionOnLogout() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String sessionId = loginResult.getResponse().getCookie("JSESSIONID").getValue();

        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("JSESSIONID", sessionId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me")
                .cookie(new Cookie("JSESSIONID", sessionId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExpireSessionAfterTimeout() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String sessionId = loginResult.getResponse().getCookie("JSESSIONID").getValue();

        Thread.sleep(3600000);

        mockMvc.perform(get("/api/users/me")
                .cookie(new Cookie("JSESSIONID", sessionId)))
                .andExpect(status().isUnauthorized());
    }
}
```

### 授权测试

#### 1. 基于角色的访问控制测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class RBACTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldAllowUserToAccessUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldNotAllowUserToAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldAllowAdminToAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}
```

#### 2. 基于资源的访问控制测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ResourceBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldAllowUserToAccessOwnData() throws Exception {
        Long userId = 1L;
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void shouldNotAllowUserToAccessOtherUsersData() throws Exception {
        Long otherUserId = 999L;
        mockMvc.perform(get("/api/users/" + otherUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldAllowAdminToAccessAnyUsersData() throws Exception {
        Long userId = 999L;
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk());
    }
}
```

## API安全测试

### API认证测试

#### 1. JWT令牌测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() throws Exception {
        String invalidToken = "invalid.token.here";
        
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithExpiredToken() throws Exception {
        String expiredToken = jwtTokenProvider.generateToken(
                new User("test@example.com", "Test User", Role.USER),
                -1);

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptRequestWithValidToken() throws Exception {
        User user = new User("test@example.com", "Test User", Role.USER);
        String validToken = jwtTokenProvider.generateToken(user);

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}
```

### API输入验证测试

#### 1. 输入验证测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class InputValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectEmptyEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"name\":\"Test User\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email is required"));
    }

    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid-email\",\"name\":\"Test User\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
    }

    @Test
    void shouldRejectTooLongName() throws Exception {
        String longName = "A".repeat(256);
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"name\":\"" + longName + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name must be less than 255 characters"));
    }

    @Test
    void shouldAcceptValidInput() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"name\":\"Test User\"}"))
                .andExpect(status().isCreated());
    }
}
```

### API速率限制测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowRequestsWithinRateLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void shouldRejectRequestsExceedingRateLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isTooManyRequests());
    }
}
```

## 数据安全测试

### 数据加密测试

#### 1. 敏感数据加密测试

```java
@SpringBootTest
class DataEncryptionTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    void shouldEncryptSensitiveData() throws Exception {
        String plaintext = "sensitive-data-123";
        String ciphertext = encryptionService.encrypt(plaintext);

        assertThat(ciphertext).isNotEqualTo(plaintext);
    }

    @Test
    void shouldDecryptEncryptedData() throws Exception {
        String plaintext = "sensitive-data-123";
        String ciphertext = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(ciphertext);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldNotDecryptWithWrongKey() throws Exception {
        String plaintext = "sensitive-data-123";
        String ciphertext = encryptionService.encrypt(plaintext);

        assertThatThrownBy(() -> encryptionService.decrypt(ciphertext + "tampered"))
                .isInstanceOf(DecryptionException.class);
    }
}
```

### 数据传输安全测试

#### 1. HTTPS强制测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class HttpsEnforcementTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectHttpToHttps() throws Exception {
        mockMvc.perform(get("/api/products")
                .secure(false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://localhost/api/products"));
    }

    @Test
    void shouldAcceptHttpsRequests() throws Exception {
        mockMvc.perform(get("/api/products")
                .secure(true))
                .andExpect(status().isOk());
    }
}
```

## 安全漏洞扫描工具使用

### OWASP ZAP

#### 1. 安装配置

```bash
# 下载OWASP ZAP
wget https://github.com/zaproxy/zaproxy/releases/download/v2.14.0/ZAP_2.14.0_Linux.tar.gz

# 解压
tar -xzf ZAP_2.14.0_Linux.tar.gz

# 启动ZAP
./zap.sh -daemon -port 8080
```

#### 2. 自动化扫描

```python
#!/usr/bin/env python3
import zapv2
import time

zap = zapv2.ZAPv2(proxies={'http': 'http://localhost:8080', 'https': 'http://localhost:8080'})

target = 'http://localhost:8080/api'

print('Spidering target...')
scan_id = zap.spider.scan(target)
while int(zap.spider.status(scan_id)) < 100:
    print('Spider progress: {}%'.format(zap.spider.status(scan_id)))
    time.sleep(2)

print('Active scanning...')
scan_id = zap.ascan.scan(target)
while int(zap.ascan.status(scan_id)) < 100:
    print('Scan progress: {}%'.format(zap.ascan.status(scan_id)))
    time.sleep(2)

print('Scan completed')
print('Alerts:')
alerts = zap.core.alerts()
for alert in alerts:
    print(f'  - {alert["alert"]}: {alert["risk"]}')
```

### Burp Suite

#### 1. 安装配置

```bash
# 下载Burp Suite
wget https://portswigger.net/burp/releases/download?product=community&type=Linux

# 启动Burp Suite
java -jar burpsuite_community.jar
```

#### 2. 自动化扫描

```python
#!/usr/bin/env python3
from burp import IBurpExtender, IScanner
import json

class BurpExtender(IBurpExtender, IScanner):

    def registerExtenderCallbacks(self, callbacks):
        self._callbacks = callbacks
        self._helpers = callbacks.getHelpers()
        callbacks.setExtensionName("Security Scanner")
        callbacks.registerScanner(self)

    def doPassiveScan(self, requestResponse):
        return None

    def doActiveScan(self, baseRequestResponse, insertionPoint):
        return None
```

### OWASP Dependency-Check

#### 1. Maven配置

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
    </configuration>
</plugin>
```

#### 2. 执行扫描

```bash
# 执行Maven扫描
mvn dependency-check:check

# 执行命令行扫描
dependency-check --scan ./pom.xml --out ./dependency-check-report.html
```

## 安全测试最佳实践

### 测试设计最佳实践

```yaml
best_practices:
  test_design:
    - "遵循OWASP Top 10"
    - "覆盖所有认证授权场景"
    - "测试所有输入验证"
    - "验证所有安全控制"
    - "模拟真实攻击"
    - "使用自动化工具"
```

### 测试执行最佳实践

```yaml
best_practices:
  test_execution:
    - "在测试环境执行"
    - "使用隔离环境"
    - "定期执行测试"
    - "集成CI/CD流程"
    - "记录测试结果"
    - "跟踪漏洞修复"
```

### 漏洞管理最佳实践

```yaml
best_practices:
  vulnerability_management:
    - "及时修复严重漏洞"
    - "评估漏洞风险"
    - "优先处理高危漏洞"
    - "跟踪修复进度"
    - "验证修复效果"
    - "定期审计漏洞"
```

## 相关文档

- [集成测试指南](IntegrationTestingGuide.md)
- [性能测试指南](PerformanceTestingGuide.md)
- [测试自动化指南](TestAutomationGuide.md)
- [测试最佳实践指南](TestBestPracticesGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |