# 安全配置指南

本文档定义了进销存管理系统的安全配置规范，包括网络安全、数据安全、应用安全和基础设施安全等方面的配置要求。

## 1. 网络安全配置

### 1.1 防火墙配置
```bash
# 允许必要的端口
iptables -A INPUT -p tcp --dport 22 -j ACCEPT    # SSH
iptables -A INPUT -p tcp --dport 80 -j ACCEPT    # HTTP
iptables -A INPUT -p tcp --dport 443 -j ACCEPT   # HTTPS
iptables -A INPUT -p tcp --dport 8080 -j ACCEPT  # 应用端口
iptables -A INPUT -p tcp --dport 8443 -j ACCEPT  # 应用HTTPS端口
iptables -A INPUT -p tcp --dport 8761 -j ACCEPT  # Eureka注册中心
iptables -A INPUT -p tcp --dport 8888 -j ACCEPT  # 配置中心
iptables -A INPUT -p tcp --dport 9090 -j ACCEPT  # Prometheus
iptables -A INPUT -p tcp --dport 3000 -j ACCEPT  # Grafana

# 拒绝其他所有连接
iptables -A INPUT -j DROP
```

### 1.2 网络隔离
```yaml
# docker-compose.yml 网络配置
version: '3.8'
services:
  database:
    networks:
      - backend-network
      
  redis:
    networks:
      - backend-network
      
  app:
    networks:
      - frontend-network
      - backend-network
      
  gateway:
    networks:
      - frontend-network
      - public-network
      
networks:
  public-network:
    driver: bridge
  frontend-network:
    driver: bridge
    internal: true
  backend-network:
    driver: bridge
    internal: true
```

### 1.3 SSL/TLS配置
```yaml
# nginx.conf HTTPS配置
server {
    listen 443 ssl http2;
    server_name inventory.example.com;
    
    # SSL证书配置
    ssl_certificate /etc/ssl/certs/inventory.crt;
    ssl_certificate_key /etc/ssl/private/inventory.key;
    
    # SSL安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # HSTS配置
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # 其他安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    
    location / {
        proxy_pass http://gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 2. 数据安全配置

### 2.1 数据库安全
```sql
-- PostgreSQL安全配置
-- 创建专用数据库用户
CREATE USER inventory_user WITH PASSWORD 'strong_password_here';
CREATE DATABASE inventory_system OWNER inventory_user;

-- 授予最小必要权限
GRANT CONNECT ON DATABASE inventory_system TO inventory_user;
GRANT USAGE ON SCHEMA public TO inventory_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO inventory_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO inventory_user;

-- 启用SSL连接
-- postgresql.conf
ssl = on
ssl_cert_file = 'server.crt'
ssl_key_file = 'server.key'

-- pg_hba.conf
hostssl all all 0.0.0.0/0 md5
```

### 2.2 数据加密配置
```yaml
# application.yml 数据库连接加密
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_system?sslmode=require
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      data-source-properties:
        ssl: true
        sslmode: require
        
# 敏感配置加密
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
    algorithm: PBEWITHHMACSHA512ANDAES_256
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
```

### 2.3 备份和恢复安全
```bash
#!/bin/bash
# 数据库备份脚本 backup.sh
BACKUP_DIR="/backup/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/inventory_backup_$DATE.sql"

# 创建备份
pg_dump -h localhost -U inventory_user -d inventory_system > $BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_FILE

# 加密备份文件
gpg --cipher-algo AES256 --compress-algo 2 --symmetric --output $BACKUP_FILE.gz.gpg $BACKUP_FILE.gz

# 删除未加密的备份文件
rm $BACKUP_FILE.gz

# 保留最近7天的备份
find $BACKUP_DIR -name "inventory_backup_*.sql.gz.gpg" -mtime +7 -delete
```

## 3. 应用安全配置

### 3.1 Spring Security配置
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/",
                    "/favicon.ico",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js")
                    .permitAll()
                .antMatchers("/api/auth/**")
                    .permitAll()
                .antMatchers("/api/public/**")
                    .permitAll()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                .antMatchers("/actuator/**")
                    .hasRole("ADMIN")
                .anyRequest()
                    .authenticated();
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

### 3.2 日志安全配置
```yaml
# logback-spring.xml
<configuration>
    <springProfile name="default">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/inventory-system.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/inventory-system.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
                <totalSizeCap>10GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <appender name="SECURITY_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/security.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/security.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>50MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>90</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <logger name="com.inventory.security" level="INFO" additivity="false">
            <appender-ref ref="SECURITY_FILE"/>
        </logger>
        
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### 3.3 敏感信息保护
```java
@Configuration
public class SensitiveDataProtectionConfig {
    
    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializerByType(String.class, new SensitiveDataSerializer());
        return builder;
    }
    
    // 敏感数据序列化器
    public static class SensitiveDataSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null && isSensitiveData(value)) {
                gen.writeString(maskSensitiveData(value));
            } else {
                gen.writeString(value);
            }
        }
        
        private boolean isSensitiveData(String value) {
            // 判断是否为敏感数据（手机号、身份证号等）
            return value.matches("^(1[3-9]\\d{9})|(\\d{17}[\\dXx])$");
        }
        
        private String maskSensitiveData(String value) {
            if (value.length() > 7) {
                return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
            }
            return "****";
        }
    }
}
```

### 3.4 API安全配置

#### 3.4.1 请求频率限制
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

#### 3.4.2 CSRF保护
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

## 4. 基础设施安全

### 4.1 Docker安全配置
```dockerfile
# Dockerfile 安全配置
FROM eclipse-temurin:22-jre-alpine

# 创建非root用户
RUN addgroup -S -g 1001 appgroup && \
    adduser -S -u 1001 -G appgroup appuser

# 设置工作目录
WORKDIR /app

# 复制应用文件
COPY --chown=appuser:appgroup target/*.jar app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 4.2 Kubernetes安全配置
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inventory-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: inventory-app
  template:
    metadata:
      labels:
        app: inventory-app
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        runAsGroup: 1001
        fsGroup: 1001
      containers:
      - name: inventory-app
        image: inventory-system:latest
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
              - ALL
        ports:
        - containerPort: 8080
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: inventory-secrets
              key: db-password
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### 4.3 监控和告警安全
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

scrape_configs:
  - job_name: 'inventory-system'
    static_configs:
      - targets: ['app:8080']
    basic_auth:
      username: prometheus
      password: ${PROMETHEUS_PASSWORD}
    tls_config:
      ca_file: /etc/prometheus/ca.crt
      cert_file: /etc/prometheus/client.crt
      key_file: /etc/prometheus/client.key

# alert_rules.yml
groups:
  - name: security-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for more than 10 minutes"
          
      - alert: BruteForceAttack
        expr: rate(authentication_failures_total[5m]) > 10
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Potential brute force attack"
          description: "More than 10 authentication failures per minute detected"
```

## 5. 安全审计和合规

### 5.1 安全审计日志
```java
@Aspect
@Component
public class SecurityAuditAspect {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    @AfterReturning(pointcut = "execution(* com.inventory.controller.*.*(..))", returning = "result")
    public void logControllerAccess(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        securityLogger.info("ACCESS - User: {}, IP: {}, Method: {}, URL: {}, Params: {}", 
            authentication != null ? authentication.getName() : "ANONYMOUS",
            getClientIP(request),
            request.getMethod(),
            request.getRequestURI(),
            Arrays.toString(joinPoint.getArgs()));
    }
    
    @AfterThrowing(pointcut = "execution(* com.inventory.service.*.*(..))", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Exception exception) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        securityLogger.warn("SERVICE_EXCEPTION - User: {}, Method: {}, Exception: {}", 
            authentication != null ? authentication.getName() : "ANONYMOUS",
            joinPoint.getSignature().toShortString(),
            exception.getMessage());
    }
    
    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 5.2 合规性检查清单
- [ ] 数据保护法规合规（如GDPR）
- [ ] 定期安全漏洞扫描
- [ ] 第三方依赖安全审查
- [ ] 安全培训和意识提升
- [ ] 应急响应计划制定
- [ ] 定期安全审计和评估

本指南为进销存管理系统的安全配置提供了全面的规范和实现指导，所有运维和开发团队应严格遵循这些规范进行安全配置。