# 认证授权机制指南

本文档定义了进销存管理系统的认证授权机制，包括JWT Token管理、OAuth2集成、RBAC权限模型和API安全等方面。

## 1. 认证机制

### 1.1 JWT Token管理

#### 1.1.1 Token生成
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", userDetails.getAuthorities())
                .claim("userId", getUserID(userDetails.getUsername()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 6); // 6倍于访问令牌过期时间
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    private Long getUserID(String username) {
        // 从数据库获取用户ID
        return userService.getUserIdByUsername(username);
    }
}
```

#### 1.1.2 Token验证
```java
@Component
public class JwtTokenProvider {
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        
        List<String> authorities = (List<String>) claims.get("authorities");
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```

#### 1.1.3 Token刷新
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        try {
            // 验证刷新令牌
            if (!tokenProvider.validateToken(request.getRefreshToken())) {
                return ResponseEntity.badRequest().body("Invalid refresh token");
            }
            
            // 获取用户名
            String username = tokenProvider.getUsernameFromToken(request.getRefreshToken());
            
            // 验证用户是否存在且未被禁用
            UserDetails userDetails = userService.loadUserByUsername(username);
            if (!userDetails.isEnabled()) {
                return ResponseEntity.badRequest().body("User is disabled");
            }
            
            // 生成新的访问令牌
            String newAccessToken = tokenProvider.generateToken(userDetails);
            
            return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, request.getRefreshToken()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token refresh failed");
        }
    }
}
```

### 1.2 OAuth2集成

#### 1.2.1 第三方认证配置
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    
    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
            .oauth2Login()
                .authorizationEndpoint()
                    .baseUri("/oauth2/authorize")
                    .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                    .and()
                .redirectionEndpoint()
                    .baseUri("/oauth2/callback/*")
                    .and()
                .userInfoEndpoint()
                    .userService(customOAuth2UserService)
                    .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);
    }
    
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }
}
```

#### 1.2.2 OAuth2用户服务
```java
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(), 
                oAuth2User.getAttributes());
        
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
        
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 更新用户信息
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // 注册新用户
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }
        
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
    
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }
    
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
```

## 2. 授权机制

### 2.1 RBAC权限模型

#### 2.1.1 权限实体设计
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    // Getters and setters...
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    // Getters and setters...
}

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    // Getters and setters...
}
```

#### 2.1.2 权限枚举定义
```java
public enum PermissionName {
    // 商品管理权限
    PRODUCT_CREATE("PRODUCT_CREATE", "创建商品"),
    PRODUCT_READ("PRODUCT_READ", "查看商品"),
    PRODUCT_UPDATE("PRODUCT_UPDATE", "修改商品"),
    PRODUCT_DELETE("PRODUCT_DELETE", "删除商品"),
    
    // 库存管理权限
    INVENTORY_READ("INVENTORY_READ", "查看库存"),
    INVENTORY_UPDATE("INVENTORY_UPDATE", "修改库存"),
    STOCK_TAKING("STOCK_TAKING", "库存盘点"),
    
    // 订单管理权限
    ORDER_CREATE("ORDER_CREATE", "创建订单"),
    ORDER_READ("ORDER_READ", "查看订单"),
    ORDER_UPDATE("ORDER_UPDATE", "修改订单"),
    ORDER_DELETE("ORDER_DELETE", "删除订单"),
    ORDER_APPROVE("ORDER_APPROVE", "审批订单"),
    
    // 用户管理权限
    USER_CREATE("USER_CREATE", "创建用户"),
    USER_READ("USER_READ", "查看用户"),
    USER_UPDATE("USER_UPDATE", "修改用户"),
    USER_DELETE("USER_DELETE", "删除用户"),
    USER_ROLE_ASSIGN("USER_ROLE_ASSIGN", "分配用户角色"),
    
    // 系统管理权限
    SYSTEM_CONFIG_READ("SYSTEM_CONFIG_READ", "查看系统配置"),
    SYSTEM_CONFIG_UPDATE("SYSTEM_CONFIG_UPDATE", "修改系统配置"),
    SYSTEM_LOG_READ("SYSTEM_LOG_READ", "查看系统日志");
    
    private final String code;
    private final String description;
    
    PermissionName(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### 2.2 权限验证

#### 2.2.1 方法级权限控制
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @PreAuthorize("hasPermission('PRODUCT_CREATE')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }
    
    @PreAuthorize("hasPermission('PRODUCT_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @PreAuthorize("hasPermission('PRODUCT_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductUpdateRequest request) {
        ProductDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }
    
    @PreAuthorize("hasPermission('PRODUCT_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
```

#### 2.2.2 自定义权限验证
```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ((authentication == null) || (permission == null)) {
            return false;
        }
        
        String permissionName = (String) permission;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 检查用户是否具有指定权限
        return hasPrivilege(userPrincipal, permissionName);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ((authentication == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }
        
        String permissionName = (String) permission;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 检查用户是否具有指定权限
        return hasPrivilege(userPrincipal, permissionName);
    }
    
    private boolean hasPrivilege(UserPrincipal userPrincipal, String permissionName) {
        // 检查用户角色是否具有权限
        Set<Permission> rolePermissions = userPrincipal.getRole().getPermissions();
        boolean hasRolePermission = rolePermissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
        
        if (hasRolePermission) {
            return true;
        }
        
        // 检查用户个人权限
        Set<Permission> userPermissions = userPrincipal.getPermissions();
        return userPermissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}
```

## 3. API安全

### 3.1 请求频率限制
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${rate.limit.max-requests:100}")
    private int maxRequests;
    
    @Value("${rate.limit.window-seconds:60}")
    private int windowSeconds;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;
        
        String currentCountStr = redisTemplate.opsForValue().get(key);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        if (currentCount >= maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        
        return true;
    }
    
    private String getClientId(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null) {
            clientId = request.getRemoteAddr();
        }
        return clientId;
    }
}
```

### 3.2 输入参数验证
```java
@Data
public class ProductCreateRequest {
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    private String name;
    
    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "商品编码只能包含字母、数字、下划线和横线")
    private String productCode;
    
    @NotNull(message = "采购价格不能为空")
    @DecimalMin(value = "0.00", message = "采购价格必须大于等于0")
    @Digits(integer = 10, fraction = 2, message = "采购价格最多10位整数，2位小数")
    private BigDecimal purchasePrice;
    
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "销售价格必须大于等于0")
    @Digits(integer = 10, fraction = 2, message = "销售价格最多10位整数，2位小数")
    private BigDecimal sellingPrice;
    
    @Min(value = 0, message = "保质期天数必须大于等于0")
    private Integer shelfLifeDays;
    
    @Valid
    private List<@Valid ProductSpecificationRequest> specifications;
}

@Data
public class ProductSpecificationRequest {
    
    @NotBlank(message = "规格名称不能为空")
    @Size(max = 50, message = "规格名称长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "规格值不能为空")
    @Size(max = 100, message = "规格值长度不能超过100个字符")
    private String value;
}
```

### 3.3 CSRF保护
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .headers()
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                    .preload(true))
                .and()
            .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");
    }
}
```

## 4. 安全最佳实践

### 4.1 密码安全
```java
@Component
public class PasswordEncoderService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public boolean isPasswordStrong(String password) {
        // 密码强度检查：至少8位，包含大小写字母、数字和特殊字符
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }
}
```

### 4.2 敏感信息保护
```java
@Entity
@Table(name = "users")
public class User {
    
    // 敏感信息加密存储
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone")
    private String phone;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "id_card")
    private String idCard;
    
    // Getters and setters...
}

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return decrypt(dbData);
    }
    
    private String encrypt(String plainText) {
        // 实现加密逻辑
        return EncryptionUtils.encrypt(plainText);
    }
    
    private String decrypt(String encryptedText) {
        // 实现解密逻辑
        return EncryptionUtils.decrypt(encryptedText);
    }
}
```

### 4.3 安全日志记录
```java
@Component
public class SecurityAuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditLogger.class);
    
    public void logAuthenticationSuccess(String username, String ip) {
        logger.info("Authentication successful - User: {}, IP: {}", username, ip);
    }
    
    public void logAuthenticationFailure(String username, String ip, String reason) {
        logger.warn("Authentication failed - User: {}, IP: {}, Reason: {}", username, ip, reason);
    }
    
    public void logAuthorizationFailure(String username, String resource, String permission) {
        logger.warn("Authorization failed - User: {}, Resource: {}, Permission: {}", username, resource, permission);
    }
    
    public void logSecurityViolation(String username, String action, String details) {
        logger.error("Security violation - User: {}, Action: {}, Details: {}", username, action, details);
    }
}
```

本指南为进销存管理系统的认证授权机制提供了全面的规范和实现指导，所有开发团队应严格遵循这些规范进行安全开发。