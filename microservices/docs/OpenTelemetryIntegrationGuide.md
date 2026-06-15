# OpenTelemetry集成指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本指南提供了将库存管理系统与OpenTelemetry集成的详细步骤，包括SDK配置、Tracer初始化、Span配置、Metrics配置和Exporter配置。通过本指南，您可以实现完整的分布式追踪和指标采集能力，提升系统的可观测性。

### 1.2 适用范围
- 分布式追踪集成
- 指标采集集成
- 性能监控集成
- 上下文传播配置
- 采样策略配置

### 1.3 目标读者
- 系统架构师
- 后端开发工程师
- DevOps工程师
- 监控工程师

### 1.4 前置条件
- 已部署Prometheus监控系统
- 已部署Jaeger分布式追踪系统
- 具备Java应用配置经验
- 了解OpenTelemetry基本概念

## 2. OpenTelemetry概述

### 2.1 OpenTelemetry简介
OpenTelemetry是一个可观测性框架，提供一组标准化的工具、API和SDK，用于生成、收集、分析和导出遥测数据（追踪、指标、日志）。

### 2.2 核心概念
- **Tracer**：用于创建和管理Span
- **Span**：表示分布式追踪中的一个操作单元
- **TraceContext**：包含追踪上下文信息
- **Meter**：用于记录指标
- **Counter**：单调递增的计数器
- **Gauge**：可以上下浮动的值
- **Histogram**：记录值的分布情况
- **Timer**：记录操作的持续时间

### 2.3 架构组件
```
┌─────────────────────────────────────────────────┐
│                        应用服务层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  订单服务     │  │  库存服务     │  │  用户服务     │          │
│  │  OTel SDK    │  │  OTel SDK    │  │  OTel SDK    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                 │
│         ▼                 ▼                 ▼                 ▼
│                            │                 │
│  ┌─────────────────────────────────────────┐   │
│  │        OpenTelemetry Collector       │   │
│  │  - 接收和聚合遥测数据              │   │
│  └─────────────────────────────────────────┘   │
└───────────────────────────────────────────────────┘
                            │                 │
                            ▼                 ▼
┌─────────────────────────────────────────────────┐
│                 Jaeger集群                          │
│  ┌─────────────────────────────────────────┐   │
│  │  - 接收和存储追踪数据              │   │
│  │  - 提供查询和可视化              │   │
│  └─────────────────────────────────────────┘   │
└───────────────────────────────────────────────────┘
```

## 3. Maven依赖配置

### 3.1 添加OpenTelemetry依赖

在`pom.xml`中添加OpenTelemetry依赖：

```xml
<dependencies>
    <!-- OpenTelemetry API -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- OpenTelemetry Spring Boot Starter -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-spring-boot-starter</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- OpenTelemetry Exporter for Prometheus -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-prometheus</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- OpenTelemetry Exporter for Jaeger -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-jaeger</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation for Spring MVC -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-instrumentation-spring-webmvc</artifactId>
        <version>1.38.0-alpha</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation for Spring WebFlux -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-instrumentation-spring-webflux</artifactId>
        <version>1.38.0-alpha</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation for JDBC -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-jdbc</artifactId>
        <version>1.38.0-alpha</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation for Kafka -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-kafka-clients</artifactId>
        <version>1.38.0-alpha</version>
    </dependency>
    
    <!-- OpenTelemetry Instrumentation for HTTP Client -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-instrumentation-apache-httpclient</artifactId>
        <version>1.38.0-alpha</version>
    </dependency>
    
    <!-- Micrometer Integration -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-opentelemetry</artifactId>
        <version>1.3.2</version>
    </dependency>
</dependencies>
```

## 4. 应用配置文件

### 4.1 application.yml配置

在`application.yml`中添加OpenTelemetry配置：

```yaml
spring:
  application:
    name: ${spring.application.name:inventory-service}

opentelemetry:
  traces:
    exporter:
      jaeger:
        enabled: true
        endpoint: ${JAEGER_ENDPOINT:http://localhost:14268/api/traces}
        timeout: 10000
        compression: gzip
        headers:
          X-Jaeger-Auth-Token: ${JAEGER_AUTH_TOKEN:your-token}
    sampler:
      type: traceidratio
      ratio: 0.1
    propagators:
      - type: baggage
      - type: tracecontext
      - type: jaeger
    span:
      processor:
        type: batch
        batch-size: 512
        queue-size: 2048
        export-timeout: 30000
      limits:
        count: 1000
        attribute-count: 128
        attribute-value-length: 256
    resource:
      service.name: ${spring.application.name}
      service.namespace: inventory-management
      deployment.environment: ${spring.profiles.active:prod}
      host.name: ${HOSTNAME:localhost}
      telemetry.sdk.language: java
      telemetry.sdk.version: 1.38.0
  
  metrics:
    exporter:
      prometheus:
        enabled: true
        host: ${PROMETHEUS_HOST:localhost}
        port: ${PROMETHEUS_PORT:9090}
        resource-attributes:
          service.name: ${spring.application.name}
          service.namespace: inventory-management
    readers:
      prometheus:
        enabled: true
        preferred-temporality: cumulative
    exporters:
      prometheus:
        enabled: true
        prometheus:
          description: "Prometheus metrics exporter"
          host: ${PROMETHEUS_HOST:localhost}
          port: ${PROMETHEUS_PORT:9090}
          protocol: http
          interval: 15000
          resource-attributes:
            service.name: ${spring.application.name}
            service.namespace: inventory-management
    instrumentation:
      http:
        enabled: true
        capture-request-headers: true
        capture-request-parameters: true
        capture-experimental-request-headers: false
      jdbc:
        enabled: true
      kafka:
        enabled: true
      micrometer:
        enabled: true
        enabled: true
  
  logs:
    exporter:
      otlp:
        enabled: false
        endpoint: ${OTLP_ENDPOINT:http://localhost:4318}
        compression: gzip
        batch-size: 512
        timeout: 10000
    instrumentation:
      logback:
        enabled: false
      spring:
        enabled: false
```

### 4.2 application-tracing.yml配置

创建专门的追踪配置文件：

```yaml
opentelemetry:
  traces:
    exporter:
      jaeger:
        enabled: true
        endpoint: ${JAEGER_ENDPOINT:http://localhost:14268/api/traces}
        timeout: 10000
        compression: gzip
        agent-host: ${JAEGER_AGENT_HOST:localhost}
        agent-port: ${JAEGER_AGENT_PORT:6831}
    sampler:
      type: parentbased
      root-spans-per-minute: 10
    propagators:
      - type: w3c
      - type: b3
      - type: jaeger
    span:
      kind: server
      remote-parent-sampled: true
      attributes:
        enabled: true
        max-attribute-count: 64
        max-attribute-value-length: 128
    resource:
      service.name: ${spring.application.name}
      service.version: ${project.version:3.0.0}
      deployment.environment: ${spring.profiles.active:prod}
      host.name: ${HOSTNAME:localhost}
      process.pid: ${PID:${}}
      process.command: ${PROCESS_COMMAND:}
      process.runtime.name: ${PROCESS_RUNTIME_NAME:}
      process.runtime.version: ${PROCESS_RUNTIME_VERSION:}
      process.owner: ${PROCESS_OWNER:}
      container.id: ${CONTAINER_ID:}
      k8s.namespace: ${K8S_NAMESPACE:}
      k8s.pod.name: ${K8S_POD_NAME:}
      k8s.node.name: ${K8S_NODE_NAME:}
    baggage:
      enabled: true
      limit: 32
```

## 5. Java配置类

### 5.1 OpenTelemetry配置类

创建OpenTelemetry配置类：

```java
package com.inventory.monitoring.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.api.trace.propagation.JaegerTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {
    
    @Value("${opentelemetry.traces.sampler.ratio:0.1}")
    private double samplingRatio;
    
    @Value("${opentelemetry.traces.exporter.jaeger.endpoint:http://localhost:14268/api/traces}")
    private String jaegerEndpoint;
    
    @Value("${opentelemetry.traces.exporter.jaeger.timeout:10000}")
    private int jaegerTimeout;
    
    @Value("${opentelemetry.traces.span.processor.type:batch}")
    private String spanProcessorType;
    
    @Value("${opentelemetry.traces.span.processor.batch-size:512}")
    private int spanBatchSize;
    
    @Value("${opentelemetry.traces.span.limits.count:1000}")
    private int maxSpanCount;
    
    @Value("${opentelemetry.traces.span.limits.attribute-count:128}")
    private int maxAttributeCount;
    
    @Value("${opentelemetry.traces.resource.service.name:inventory-service}")
    private String serviceName;
    
    @Value("${opentelemetry.traces.resource.service.namespace:inventory-management}")
    private String serviceNamespace;
    
    @Value("${opentelemetry.traces.resource.deployment.environment:prod}")
    private String deploymentEnvironment;
    
    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetry.builder()
                .setTracerProvider(tracerProvider())
                .setPropagators(propagators())
                .build();
    }
    
    private io.opentelemetry.api.trace.TracerProvider tracerProvider() {
        return (builder, spanName, spanKind, parentContext) -> {
            Span span = builder.spanBuilder(spanName, spanKind)
                    .setParent(parentContext)
                    .startSpan();
            
            return span;
        };
    }
    
    private List<TextMapPropagator> propagators() {
        List<TextMapPropagator> propagators = new ArrayList<>();
        propagators.add(new W3CTraceContextPropagator());
        propagators.add(new JaegerTraceContextPropagator());
        return propagators;
    }
    
    public double getSamplingRatio() {
        return samplingRatio;
    }
    
    public void setSamplingRatio(double samplingRatio) {
        this.samplingRatio = samplingRatio;
    }
    
    public String getJaegerEndpoint() {
        return jaegerEndpoint;
    }
    
    public void setJaegerEndpoint(String jaegerEndpoint) {
        this.jaegerEndpoint = jaegerEndpoint;
    }
    
    public int getJaegerTimeout() {
        return jaegerTimeout;
    }
    
    public void setJaegerTimeout(int jaegerTimeout) {
        this.jaegerTimeout = jaegerTimeout;
    }
    
    public String getSpanProcessorType() {
        return spanProcessorType;
    }
    
    public void setSpanProcessorType(String spanProcessorType) {
        this.spanProcessorType = spanProcessorType;
    }
    
    public int getSpanBatchSize() {
        return spanBatchSize;
    }
    
    public void setSpanBatchSize(int spanBatchSize) {
        this.spanBatchSize = spanBatchSize;
    }
    
    public int getMaxSpanCount() {
        return maxSpanCount;
    }
    
    public void setMaxSpanCount(int maxSpanCount) {
        this.maxSpanCount = maxSpanCount;
    }
    
    public int getMaxAttributeCount() {
        return maxAttributeCount;
    }
    
    public void setMaxAttributeCount(int maxAttributeCount) {
        this.maxAttributeCount = maxAttributeCount;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceNamespace() {
        return serviceNamespace;
    }
    
    public void setServiceNamespace(String serviceNamespace) {
        this.serviceNamespace = serviceNamespace;
    }
    
    public String getDeploymentEnvironment() {
        return deploymentEnvironment;
    }
    
    public void setDeploymentEnvironment(String deploymentEnvironment) {
        this.deploymentEnvironment = deploymentEnvironment;
    }
}
```

### 5.2 Tracer包装类

创建Tracer包装类：

```java
package com.inventory.monitoring.tracing;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TracingHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(TracingHelper.class);
    
    private final Tracer tracer;
    
    @Autowired
    public TracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Span createSpan(String operationName) {
        return tracer.spanBuilder(operationName)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }
    
    public Span createSpan(String operationName, SpanKind kind) {
        return tracer.spanBuilder(operationName)
                .setSpanKind(kind)
                .startSpan();
    }
    
    public Span createSpan(String operationName, Context context) {
        return tracer.spanBuilder(operationName)
                .setParent(context)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }
    
    public Span createSpan(String operationName, SpanKind kind, Context context, 
                   Map<String, String> attributes) {
        SpanBuilder spanBuilder = tracer.spanBuilder(operationName)
                .setSpanKind(kind);
        
        if (context != null) {
            spanBuilder.setParent(context);
        }
        
        if (attributes != null && !attributes.isEmpty()) {
            spanBuilder.setAllAttributes(attributes);
        }
        
        return spanBuilder.startSpan();
    }
    
    public void recordException(Span span, Throwable exception) {
        span.recordException(exception);
        span.setStatus(StatusCode.ERROR, exception.getMessage());
        
        logger.error("Exception in span {}: {}", span.getName(), exception.getMessage(), exception);
    }
    
    public void recordSuccess(Span span) {
        span.setStatus(StatusCode.OK);
    }
    
    public void recordSuccess(Span span, Map<String, String> attributes) {
        span.setStatus(StatusCode.OK);
        
        if (attributes != null && !attributes.isEmpty()) {
            span.setAllAttributes(attributes);
        }
    }
    
    public void addAttribute(Span span, String key, String value) {
        span.setAttribute(key, value);
    }
    
    public void addEvent(Span span, String name, Map<String, String> attributes) {
        span.addEvent(name, attributes);
    }
}
```

### 5.3 自定义Span注解

创建自定义Span注解：

```java
package com.inventory.monitoring.tracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @TraceOperation {
    
    String value() default "";
    
    String operationName() default "";
    
    String kind() default "server";
    
    String[] attributes() default {};
    
    boolean recordException() default true;
    
    boolean recordParameters() default false;
    
    boolean recordReturnValue() default false;
}
```

创建Span切面：

```java
package com.inventory.monitoring.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TracingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(TracingAspect.class);
    
    private final TracingHelper tracingHelper;
    
    @Autowired
    public TracingAspect(TracingHelper tracingHelper) {
        this.tracingHelper = tracingHelper;
    }
    
    @Around("@annotation(com.inventory.monitoring.tracing.TraceOperation)")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceOperation traceOperation = getTraceOperationAnnotation(joinPoint);
        String operationName = traceOperation.operationName().isEmpty() ? 
                joinPoint.getSignature().getName() : traceOperation.operationName();
        
        SpanKind kind = SpanKind.valueOf(traceOperation.kind().toUpperCase());
        
        Span span = tracingHelper.createSpan(operationName, kind);
        
        try {
            Object result = joinPoint.proceed();
            
            if (traceOperation.recordParameters()) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    for (int i = 0; i < args.length; i++) {
                        span.setAttribute("param." + i, String.valueOf(args[i]));
                    }
                }
            }
            
            if (traceOperation.recordReturnValue() && result != null) {
                span.setAttribute("returnValue", String.valueOf(result));
            }
            
            tracingHelper.recordSuccess(span);
            return result;
            
        } catch (Throwable e) {
            if (traceOperation.recordException()) {
                tracingHelper.recordException(span, e);
            } else {
                throw e;
            }
        }
    }
    
    private TraceOperation getTraceOperationAnnotation(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature()
                .getMethod()
                .getAnnotation(TraceOperation.class);
    }
}
```

## 6. 使用示例

### 6.1 HTTP请求追踪

使用`@WithSpan`注解自动追踪HTTP请求：

```java
package com.inventory.monitoring.controller;

import com.inventory.monitoring.tracing.TraceOperation;
import io.opentelemetry.api.trace.SpanKind;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping("/{id}")
    @TraceOperation(operationName = "getOrderById", kind = "server")
    public Order getOrderById(@PathVariable String id) {
        Order order = orderService.findById(id);
        return order;
    }
    
    @GetMapping
    @TraceOperation(operationName = "getAllOrders", kind = "server", 
                attributes = {"operation": "list"})
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }
}
```

### 6.2 数据库操作追踪

使用手动创建Span追踪数据库操作：

```java
package com.inventory.monitoring.repository;

import com.inventory.monitoring.tracing.TracingHelper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    
    private final TracingHelper tracingHelper;
    
    @Autowired
    public OrderRepository(TracingHelper tracingHelper) {
        this.tracingHelper = tracingHelper;
    }
    
    public Order findById(String id) {
        Span span = tracingHelper.createSpan("OrderRepository.findById", SpanKind.CLIENT);
        
        try {
            Order order = findByIdInternal(id);
            tracingHelper.recordSuccess(span);
            return order;
        } catch (Exception e) {
            tracingHelper.recordException(span, e);
            throw e;
        }
    }
    
    public List<Order> findAll() {
        Span span = tracingHelper.createSpan("OrderRepository.findAll", SpanKind.CLIENT);
        
        try {
            List<Order> orders = findAllInternal();
            tracingHelper.addEvent(span, "query", Map.of(
                    "result.count", String.valueOf(orders.size())
            ));
            tracingHelper.recordSuccess(span);
            return orders;
        } catch (Exception e) {
            tracingHelper.recordException(span, e);
            throw e;
        }
    }
    
    private Order findByIdInternal(String id) {
        return entityManager.find(Order.class, id);
    }
    
    private List<Order> findAllInternal() {
        return entityManager.createQuery("SELECT o FROM Order o", Order.class)
                .getResultList();
    }
}
```

### 6.3 外部服务调用追踪

追踪外部服务调用：

```java
package com.inventory.monitoring.service;

import com.inventory.monitoring.tracing.TracingHelper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalService.class);
    
    private final TracingHelper tracingHelper;
    private final RestTemplate restTemplate;
    
    @Autowired
    public ExternalService(TracingHelper tracingHelper, RestTemplate restTemplate) {
        this.tracingHelper = tracingHelper;
        this.restTemplate = restTemplate;
    }
    
    public String callExternalAPI(String endpoint) {
        Span span = tracingHelper.createSpan("ExternalService.callExternalAPI", SpanKind.CLIENT);
        
        try {
            String response = restTemplate.getForObject(endpoint, String.class);
            
            tracingHelper.addAttribute(span, "endpoint", endpoint);
            tracingHelper.addAttribute(span, "response.length", 
                    String.valueOf(response != null ? response.length() : 0));
            
            tracingHelper.recordSuccess(span);
            return response;
            
        } catch (Exception e) {
            tracingHelper.recordException(span, e);
            logger.error("External API call failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### 6.4 异步任务追踪

追踪异步任务：

```java
package com.inventory.monitoring.service;

import com.inventory.monitoring.tracing.TracingHelper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);
    
    private final TracingHelper tracingHelper;
    
    @Autowired
    public AsyncTaskService(TracingHelper tracingHelper) {
        this.tracingHelper = tracingHelper;
    }
    
    @Async
    public void processOrderAsync(String orderId) {
        Span span = tracingHelper.createSpan("AsyncTaskService.processOrderAsync", 
                SpanKind.INTERNAL);
        
        try {
            Order order = orderService.findById(orderId);
            orderService.process(order);
            
            tracingHelper.addEvent(span, "order.processed", Map.of(
                    "orderId", orderId,
                    "status", order.getStatus()
            ));
            
            tracingHelper.recordSuccess(span);
            
        } catch (Exception e) {
            tracingHelper.recordException(span, e);
            logger.error("Async order processing failed: {}", e.getMessage(), e);
        }
    }
}
```

## 7. 性能优化

### 7.1 采样策略

配置合理的采样率：

```yaml
opentelemetry:
  traces:
    sampler:
      type: parentbased
      root-spans-per-minute: 10
      # 只采样10%的请求，在高负载时减少采样率
```

### 7.2 批处理优化

配置Span批处理：

```yaml
opentelemetry:
  traces:
    span:
      processor:
        type: batch
        batch-size: 512
        queue-size: 2048
        export-timeout: 30000
```

### 7.3 资源优化

优化资源使用：

```java
@Configuration
public class ResourceOptimizationConfig {
    
    @Bean
    public io.opentelemetry.api.trace.TracerProvider tracerProvider() {
        return (builder, spanName, spanKind, parentContext) -> {
            Span span = builder.spanBuilder(spanName, spanKind)
                    .setParent(parentContext)
                    .setAttribute("cpu.usage", getCpuUsage())
                    .setAttribute("memory.usage", getMemoryUsage())
                    .startSpan();
            
            return span;
        };
    }
    
    private double getCpuUsage() {
        return ManagementFactory.getOperatingSystemMXBean()
                .getSystemLoadAverage();
    }
    
    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return (double) usedMemory / runtime.totalMemory();
    }
}
```

## 8. 验证和测试

### 8.1 验证追踪数据

验证追踪数据是否正确上报：

```bash
# 查询Jaeger中的追踪数据
curl http://localhost:16686/api/traces?service=inventory-service

# 验证追踪数据完整性
curl http://localhost:16686/api/traces/{trace-id}
```

### 8.2 性能测试

进行性能测试：

```bash
# 使用JMeter进行性能测试
jmeter -n -t inventory-service.jmx -l 1000 -c 10

# 监控OpenTelemetry性能指标
curl http://localhost:9464/metrics
```

### 8.3 集成测试

测试与现有监控系统的集成：

```java
@SpringBootTest
public class OpenTelemetryIntegrationTest {
    
    @Autowired
    private TracingHelper tracingHelper;
    
    @Test
    public void testTracingIntegration() {
        Span span = tracingHelper.createSpan("OpenTelemetryIntegrationTest", 
                SpanKind.INTERNAL);
        
        try {
            Thread.sleep(100);
            tracingHelper.addEvent(span, "test.event", Map.of("test", "integration"));
            tracingHelper.recordSuccess(span);
            
        } catch (Exception e) {
            tracingHelper.recordException(span, e);
            fail(e);
        }
    }
}
```

## 9. 故障排查

### 9.1 常见问题

#### 9.1.1 追踪数据未上报

**问题**：追踪数据未上报到Jaeger

**排查步骤**：
1. 检查Jaeger连接
   ```bash
   curl http://localhost:14268/api/traces?service=inventory-service
   ```
   
2. 检查OpenTelemetry配置
   ```bash
   # 查看配置文件
   cat application-tracing.yml
   ```
   
3. 检查应用日志
   ```bash
   tail -f /var/log/inventory-service/application.log | grep -i "opentelemetry"
   ```

**解决方案**：
- 验证Jaeger端点配置
- 检查网络连接
- 验证认证配置
- 重启应用服务

#### 9.1.2 Span丢失

**问题**：部分Span丢失或未完成

**排查步骤**：
1. 检查Span批处理配置
2. 检查应用日志中的错误信息
3. 检查Jaeger中的Span状态

**解决方案**：
- 调整批处理配置
- 增加超时时间
- 实现Span完成回调

#### 9.1.3 性能影响

**问题**：OpenTelemetry集成影响应用性能

**排查步骤**：
1. 进行性能基准测试
2. 监控应用响应时间
3. 分析OpenTelemetry资源使用

**解决方案**：
- 调整采样率
- 优化Span批处理
- 使用异步导出
- 限制Span属性数量

## 10. 最佳实践

### 10.1 命名规范

使用一致的Span命名：
- 使用操作名称而非方法名
- 使用小写字母和下划线
- 避免使用特殊字符

### 10.2 属性规范

使用标准化的Span属性：
- 使用语义化属性名
- 避免使用过长的属性值
- 使用标准的数据类型

### 10.3 错误处理

正确处理和记录错误：
- 使用`recordException`记录异常
- 设置正确的Span状态
- 添加错误详情属性

### 10.4 上下文传播

确保上下文正确传播：
- 使用HTTP头传播上下文
- 使用消息队列传播上下文
- 在异步任务中传播上下文

### 10.5 资源管理

合理管理OpenTelemetry资源：
- 使用批处理减少开销
- 配置合理的采样率
- 监控内存和CPU使用
- 定期清理过期的Span

## 11. 附录

### 11.1 配置参数参考
| 参数 | 默认值 | 说明 | 可选范围 |
|------|---------|------|---------|
| opentelemetry.traces.sampler.ratio | 0.1 | 采样率 | 0.0-1.0 |
| opentelemetry.traces.exporter.jaeger.endpoint | http://localhost:14268/api/traces | Jaeger端点 | - |
| opentelemetry.traces.exporter.jaeger.timeout | 10000 | 超时时间（毫秒） | 5000-30000 |
| opentelemetry.traces.span.processor.type | batch | Span处理器类型 | batch/simple |
| opentelemetry.traces.span.processor.batch-size | 512 | 批处理大小 | 256-1024 |
| opentelemetry.traces.span.limits.count | 1000 | 最大Span数 | 100-10000 |
| opentelemetry.traces.resource.service.name | inventory-service | 服务名称 | - |
| opentelemetry.metrics.exporter.prometheus.host | localhost | Prometheus主机 | - |
| opentelemetry.metrics.exporter.prometheus.port | 9090 | Prometheus端口 | - |

### 11.2 支持和资源
- OpenTelemetry官方文档：https://opentelemetry.io/docs/
- OpenTelemetry Java SDK：https://github.com/open-telemetry/opentelemetry-java
- OpenTelemetry Spring Boot：https://github.com/open-telemetry/opentelemetry-java-instrumentation
- Jaeger文档：https://www.jaegertracing.io/docs/
- Prometheus文档：https://prometheus.io/docs/
- Micrometer文档：https://micrometer.io/docs/

### 11.3 更新日志
| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |
