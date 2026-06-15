# Hera监控系统集成指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本指南提供了将库存管理系统与Hera监控系统集成的详细步骤，包括客户端配置、数据上报配置、告警集成配置和故障排查。通过本指南，您可以实现完整的监控能力，确保系统稳定运行、及时发现问题和快速响应故障。

### 1.2 适用范围
- 监控架构设计
- 监控指标采集
- 告警规则配置
- 监控仪表板创建
- 日志监控和分析
- 分布式追踪和性能分析
- 监控最佳实践

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 系统管理员
- 监控工程师

### 1.4 前置条件
- 已部署Prometheus监控系统
- 已部署Grafana仪表板
- 已部署Alertmanager告警管理
- 已部署Jaeger分布式追踪系统
- 已部署Loki日志聚合系统
- 具备基本的Linux系统管理能力
- 具备Java应用配置经验

## 2. Hera监控系统概述

### 2.1 Hera简介
Hera是一个企业级监控平台，提供全面的监控、告警和可视化能力。它支持多种数据源、灵活的告警规则和丰富的仪表板功能。

### 2.2 核心功能
- **指标采集**：支持Prometheus、OpenTelemetry等多种指标采集方式
- **告警管理**：支持多级告警、告警聚合、告警路由
- **数据可视化**：提供丰富的图表和仪表板
- **日志聚合**：集成Loki日志系统
- **分布式追踪**：支持Jaeger、Zipkin等追踪系统
- **API集成**：提供RESTful API和SDK

### 2.3 架构组件
```
┌─────────────────────────────────────────────────────────┐
│                        应用服务层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  订单服务     │  │  库存服务     │  │  用户服务     │          │
│  │  Prometheus  │  │  Prometheus  │  │  Prometheus  │          │
│  │  Exporter   │  │  Exporter   │  │  Exporter   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  └───────────────────────────────────────────────────────────┘
                            │                 │
                            ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│                           Hera监控系统集群                        │
│  ┌───────────────────────────────────────────────────────┐   │
│  │  - 数据采集和存储                               │   │
│  │  - 告警规则评估                                 │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
             │
┌─────────────────────────────────────────────────────────┐
│             Alertmanager集群                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │  - 告警聚合和去重                                 │   │
│  │  - 告警路由和分组                                 │   │
│  │  - 告警通知（邮件、短信、钉钉）                      │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
             │
┌─────────────────────────────────────────────────────────┐
│             Grafana集群                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │  - 监控仪表板                                     │   │
│  │  - 告警可视化                                     │   │
│  │  - 数据查询和分析                                   │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 3. 客户端配置步骤

### 3.1 Maven依赖配置

在`pom.xml`中添加Hera客户端依赖：

```xml
<dependencies>
    <!-- Hera监控客户端 -->
    <dependency>
        <groupId>com.hera.monitoring</groupId>
        <artifactId>hera-monitoring-client</artifactId>
        <version>2.5.0</version>
    </dependency>
    
    <!-- OpenTelemetry支持 -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-prometheus</artifactId>
        <version>1.38.0</version>
    </dependency>
    
    <!-- Micrometer支持 -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
        <version>1.3.2</version>
    </dependency>
    
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <version>1.3.2</version>
    </dependency>
</dependencies>
```

### 3.2 应用配置文件

在`application.yml`中添加Hera监控配置：

```yaml
spring:
  application:
    name: ${spring.application.name:inventory-service}

hera:
  monitoring:
    enabled: true
    client:
      application-name: ${spring.application.name}
      environment: ${spring.profiles.active:prod}
      server-url: ${HERA_SERVER_URL:http://localhost:8080}
      api-key: ${HERA_API_KEY:your-api-key}
      timeout: 30000
      retry:
        max-attempts: 3
        delay-millis: 1000
      compression:
        enabled: true
        type: gzip
        level: 6
    
    metrics:
      enabled: true
      export-interval-seconds: 15
      batch-size: 1000
      include-jvm: true
      include-system: true
      custom-metrics:
        - name: order_processing_time
          type: histogram
          help: Order processing time in milliseconds
          labels: [service, status]
        - name: inventory_warning_count
          type: gauge
          help: Number of inventory warnings
          labels: [warehouse, product_type]
    
    tracing:
      enabled: true
      sampling-rate: 0.1
      max-packet-size: 1024
      service-name: ${spring.application.name}
      propagation:
        enabled: true
        format: jaeger
      exporters:
        - type: jaeger
          endpoint: ${JAEGER_ENDPOINT:http://localhost:14268/api/traces}
    
    alerting:
      enabled: true
      aggregation:
        enabled: true
        window-minutes: 5
        max-alerts-per-window: 100
      deduplication:
        enabled: true
        match-type: fuzzy
        similarity-threshold: 0.8
      suppression:
        enabled: true
        time-window-minutes: 10
        frequency-threshold: 5
      correlation:
        enabled: true
        time-window-minutes: 15
        min-correlation-score: 0.6
      throttling:
        enabled: true
        rate-limit: 10
        rate-window-minutes: 1
      dependency-analysis:
        enabled: true
        analysis-window-minutes: 30
        max-depth: 5
```

### 3.3 Java配置类

创建Hera监控配置类：

```java
package com.inventory.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hera.monitoring")
public class HeraMonitoringConfig {
    
    private boolean enabled = true;
    private ClientConfig client;
    private MetricsConfig metrics;
    private TracingConfig tracing;
    private AlertingConfig alerting;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public ClientConfig getClient() {
        return client;
    }
    
    public void setClient(ClientConfig client) {
        this.client = client;
    }
    
    public MetricsConfig getMetrics() {
        return metrics;
    }
    
    public void setMetrics(MetricsConfig metrics) {
        this.metrics = metrics;
    }
    
    public TracingConfig getTracing() {
        return tracing;
    }
    
    public void setTracing(TracingConfig tracing) {
        this.tracing = tracing;
    }
    
    public AlertingConfig getAlerting() {
        return alerting;
    }
    
    public void setAlerting(AlertingConfig alerting) {
        this.alerting = alerting;
    }
    
    public static class ClientConfig {
        private String applicationName;
        private String environment;
        private String serverUrl;
        private String apiKey;
        private int timeout = 30000;
        private RetryConfig retry;
        private CompressionConfig compression;
        
        public String getApplicationName() {
            return applicationName;
        }
        
        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }
        
        public String getEnvironment() {
            return environment;
        }
        
        public void setEnvironment(String environment) {
            this.environment = environment;
        }
        
        public String getServerUrl() {
            return serverUrl;
        }
        
        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public RetryConfig getRetry() {
            return retry;
        }
        
        public void setRetry(RetryConfig retry) {
            this.retry = retry;
        }
        
        public CompressionConfig getCompression() {
            return compression;
        }
        
        public void setCompression(CompressionConfig compression) {
            this.compression = compression;
        }
    }
    
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long delayMillis = 1000;
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public long getDelayMillis() {
            return delayMillis;
        }
        
        public void setDelayMillis(long delayMillis) {
            this.delayMillis = delayMillis;
        }
    }
    
    public static class CompressionConfig {
        private boolean enabled = true;
        private String type = "gzip";
        private int level = 6;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public int getLevel() {
            return level;
        }
        
        public void setLevel(int level) {
            this.level = level;
        }
    }
    
    public static class MetricsConfig {
        private boolean enabled = true;
        private int exportIntervalSeconds = 15;
        private int batchSize = 1000;
        private boolean includeJvm = true;
        private boolean includeSystem = true;
        private List<CustomMetric> customMetrics;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getExportIntervalSeconds() {
            return exportIntervalSeconds;
        }
        
        public void setExportIntervalSeconds(int exportIntervalSeconds) {
            this.exportIntervalSeconds = exportIntervalSeconds;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public boolean isIncludeJvm() {
            return includeJvm;
        }
        
        public void setIncludeJvm(boolean includeJvm) {
            this.includeJvm = includeJvm;
        }
        
        public boolean isIncludeSystem() {
            return includeSystem;
        }
        
        public void setIncludeSystem(boolean includeSystem) {
            this.includeSystem = includeSystem;
        }
        
        public List<CustomMetric> getCustomMetrics() {
            return customMetrics;
        }
        
        public void setCustomMetrics(List<CustomMetric> customMetrics) {
            this.customMetrics = customMetrics;
        }
    }
    
    public static class CustomMetric {
        private String name;
        private String type;
        private String help;
        private List<String> labels;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getHelp() {
            return help;
        }
        
        public void setHelp(String help) {
            this.help = help;
        }
        
        public List<String> getLabels() {
            return labels;
        }
        
        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
    }
    
    public static class TracingConfig {
        private boolean enabled = true;
        private double samplingRate = 0.1;
        private int maxPacketSize = 1024;
        private String serviceName;
        private PropagationConfig propagation;
        private List<ExporterConfig> exporters;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public double getSamplingRate() {
            return samplingRate;
        }
        
        public void setSamplingRate(double samplingRate) {
            this.samplingRate = samplingRate;
        }
        
        public int getMaxPacketSize() {
            return maxPacketSize;
        }
        
        public void setMaxPacketSize(int maxPacketSize) {
            this.maxPacketSize = maxPacketSize;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public PropagationConfig getPropagation() {
            return propagation;
        }
        
        public void setPropagation(PropagationConfig propagation) {
            this.propagation = propagation;
        }
        
        public List<ExporterConfig> getExporters() {
            return exporters;
        }
        
        public void setExporters(List<ExporterConfig> exporters) {
            this.exporters = exporters;
        }
    }
    
    public static class PropagationConfig {
        private boolean enabled = true;
        private String format = "jaeger";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getFormat() {
            return format;
        }
        
        public void setFormat(String format) {
            this.format = format;
        }
    }
    
    public static class ExporterConfig {
        private String type;
        private String endpoint;
        private Map<String, String> headers;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
    
    public static class AlertingConfig {
        private boolean enabled = true;
        private AggregationConfig aggregation;
        private DeduplicationConfig deduplication;
        private SuppressionConfig suppression;
        private CorrelationConfig correlation;
        private ThrottlingConfig throttling;
        private DependencyAnalysisConfig dependencyAnalysis;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public AggregationConfig getAggregation() {
            return aggregation;
        }
        
        public void setAggregation(AggregationConfig aggregation) {
            this.aggregation = aggregation;
        }
        
        public DeduplicationConfig getDeduplication() {
            return deduplication;
        }
        
        public void setDeduplication(DeduplicationConfig deduplication) {
            this.deduplication = deduplication;
        }
        
        public SuppressionConfig getSuppression() {
            return suppression;
        }
        
        public void setSuppression(SuppressionConfig suppression) {
            this.suppression = suppression;
        }
        
        public CorrelationConfig getCorrelation() {
            return correlation;
        }
        
        public void setCorrelation(CorrelationConfig correlation) {
            this.correlation = correlation;
        }
        
        public ThrottlingConfig getThrottling() {
            return throttling;
        }
        
        public void setThrottling(ThrottlingConfig throttling) {
            this.throttling = throttling;
        }
        
        public DependencyAnalysisConfig getDependencyAnalysis() {
            return dependencyAnalysis;
        }
        
        public void setDependencyAnalysis(DependencyAnalysisConfig dependencyAnalysis) {
            this.dependencyAnalysis = dependencyAnalysis;
        }
    }
    
    public static class AggregationConfig {
        private boolean enabled = true;
        private int windowMinutes = 5;
        private int maxAlertsPerWindow = 100;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getWindowMinutes() {
            return windowMinutes;
        }
        
        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }
        
        public int getMaxAlertsPerWindow() {
            return maxAlertsPerWindow;
        }
        
        public void setMaxAlertsPerWindow(int maxAlertsPerWindow) {
            this.maxAlertsPerWindow = maxAlertsPerWindow;
        }
    }
    
    public static class DeduplicationConfig {
        private boolean enabled = true;
        private String matchType = "fuzzy";
        private double similarityThreshold = 0.8;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getMatchType() {
            return matchType;
        }
        
        public void setMatchType(String matchType) {
            this.matchType = matchType;
        }
        
        public double getSimilarityThreshold() {
            return similarityThreshold;
        }
        
        public void setSimilarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }
    }
    
    public static class SuppressionConfig {
        private boolean enabled = true;
        private int timeWindowMinutes = 10;
        private int frequencyThreshold = 5;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getTimeWindowMinutes() {
            return timeWindowMinutes;
        }
        
        public void setTimeWindowMinutes(int timeWindowMinutes) {
            this.timeWindowMinutes = timeWindowMinutes;
        }
        
        public int getFrequencyThreshold() {
            return frequencyThreshold;
        }
        
        public void setFrequencyThreshold(int frequencyThreshold) {
            this.frequencyThreshold = frequencyThreshold;
        }
    }
    
    public static class CorrelationConfig {
        private boolean enabled = true;
        private int timeWindowMinutes = 15;
        private double minCorrelationScore = 0.6;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getTimeWindowMinutes() {
            return timeWindowMinutes;
        }
        
        public void setTimeWindowMinutes(int timeWindowMinutes) {
            this.timeWindowMinutes = timeWindowMinutes;
        }
        
        public double getMinCorrelationScore() {
            return minCorrelationScore;
        }
        
        public void setMinCorrelationScore(double minCorrelationScore) {
            this.minCorrelationScore = minCorrelationScore;
        }
    }
    
    public static class ThrottlingConfig {
        private boolean enabled = true;
        private int rateLimit = 10;
        private int rateWindowMinutes = 1;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getRateLimit() {
            return rateLimit;
        }
        
        public void setRateLimit(int rateLimit) {
            this.rateLimit = rateLimit;
        }
        
        public int getRateWindowMinutes() {
            return rateWindowMinutes;
        }
        
        public void setRateWindowMinutes(int rateWindowMinutes) {
            this.rateWindowMinutes = rateWindowMinutes;
        }
    }
    
    public static class DependencyAnalysisConfig {
        private boolean enabled = true;
        private int analysisWindowMinutes = 30;
        private int maxDepth = 5;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getAnalysisWindowMinutes() {
            return analysisWindowMinutes;
        }
        
        public void setAnalysisWindowMinutes(int analysisWindowMinutes) {
            this.analysisWindowMinutes = analysisWindowMinutes;
        }
        
        public int getMaxDepth() {
            return maxDepth;
        }
        
        public void setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
        }
    }
}
```

## 4. 数据上报配置

### 4.1 指标上报

使用Micrometer和OpenTelemetry进行指标上报：

```java
package com.inventory.monitoring.exporter;

import com.inventory.monitoring.config.HeraMonitoringConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HeraMetricsExporter {
    
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;
    private final HeraMonitoringConfig config;
    
    private Counter orderCounter;
    private Gauge inventoryWarningGauge;
    private Timer processingTimer;
    
    @Autowired
    public HeraMetricsExporter(MeterRegistry meterRegistry, Tracer tracer, HeraMonitoringConfig config) {
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
        this.config = config;
        
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        orderCounter = Counter.builder("order_processing_total")
                .description("Total number of orders processed")
                .tag("service", config.getClient().getApplicationName())
                .register(meterRegistry);
        
        inventoryWarningGauge = Gauge.builder("inventory_warning_count")
                .description("Number of inventory warnings")
                .tag("service", config.getClient().getApplicationName())
                .register(meterRegistry);
        
        processingTimer = Timer.builder("order_processing_duration")
                .description("Order processing duration")
                .tag("service", config.getClient().getApplicationName())
                .register(meterRegistry);
    }
    
    public void recordOrderProcessed(String status) {
        if (config.getMetrics().isEnabled()) {
            orderCounter.increment();
            processingTimer.record(System.currentTimeMillis(), status);
        }
    }
    
    public void recordInventoryWarning(String warehouse, String productType) {
        if (config.getMetrics().isEnabled()) {
            inventoryWarningGauge.increment();
        }
    }
    
    @Scheduled(fixedRateString = "${hera.monitoring.metrics.export-interval-seconds:15}")
    public void exportMetrics() {
        if (!config.getMetrics().isEnabled()) {
            return;
        }
        
        System.out.println("Exporting metrics to Hera...");
    }
}
```

### 4.2 追踪上报

使用OpenTelemetry进行分布式追踪：

```java
package com.inventory.monitoring.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HeraTracingExporter {
    
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;
    
    @Autowired
    public HeraTracingExporter(Tracer tracer, OpenTelemetry openTelemetry) {
        this.tracer = tracer;
        this.openTelemetry = openTelemetry;
    }
    
    public Span createSpan(String operationName, SpanKind kind) {
        return tracer.spanBuilder(operationName)
                .setSpanKind(kind)
                .startSpan();
    }
    
    public Span createSpan(String operationName, SpanKind kind, Context context) {
        return tracer.spanBuilder(operationName)
                .setSpanKind(kind)
                .setParent(context)
                .startSpan();
    }
    
    public void recordException(Span span, Throwable exception) {
        span.recordException(exception);
        span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, exception.getMessage());
    }
    
    public void recordSuccess(Span span) {
        span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
    }
}
```

## 5. 告警集成配置

### 5.1 告警规则定义

在Hera中定义告警规则：

```yaml
groups:
  - name: inventory_alerts
    rules:
      - alert: HighInventoryWarning
        expr: inventory_warning_count > 10
        for: 5m
        labels:
          severity: warning
          service: inventory-service
        annotations:
          summary: "High number of inventory warnings detected"
          description: "More than 10 inventory warnings in the last 5 minutes"
      
      - alert: OrderProcessingSlow
        expr: histogram_quantile(0.95, rate(order_processing_duration_seconds[5m])) > 5
        for: 5m
        labels:
          severity: warning
          service: order-service
        annotations:
          summary: "Order processing is slow"
          description: "95th percentile of order processing time is above 5 seconds"
      
      - alert: HighErrorRate
        expr: rate(http_requests_total{status="500"}[5m]) / rate(http_requests_total[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
          service: all
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% in the last 5 minutes"
```

### 5.2 告警路由配置

配置告警路由和分组：

```yaml
route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  repeat_interval: 12h
  receiver: 'default-receiver'

receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'ops-team@company.com'
        from: 'alertmanager@company.com'
        headers:
          Subject: '[{{ .Status | toUpper }}] {{ .CommonLabels.alertname }}'
        html: |
          {{ range .Alerts.Firing }}
            <h2>Alert: {{ .Labels.alertname }}</h2>
            <p><strong>Summary:</strong> {{ .Annotations.summary }}</p>
            <p><strong>Description:</strong> {{ .Annotations.description }}</p>
            <p><strong>Time:</strong> {{ .StartsAt }}</p>
            <hr/>
          {{ end }}
    
    webhook_configs:
      - url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
        send_resolved: true
        http_config:
          proxy_url: 'http://proxy.company.com:8080'
```

## 6. 故障排查

### 6.1 常见问题

#### 6.1.1 连接问题
**问题**：无法连接到Hera服务器

**排查步骤**：
1. 检查网络连接
   ```bash
   ping hera-server.example.com
   telnet hera-server.example.com 8080
   ```

2. 检查防火墙设置
   ```bash
   sudo firewall-cmd --list-all
   sudo firewall-cmd --add-port=8080/tcp
   ```

3. 检查Hera服务状态
   ```bash
   curl http://hera-server.example.com:8080/health
   ```

4. 检查应用配置
   ```yaml
   hera:
     monitoring:
       client:
         server-url: http://hera-server.example.com:8080
   ```

**解决方案**：
- 确保网络连接正常
- 配置防火墙规则
- 验证Hera服务运行状态
- 检查配置文件中的服务器URL

#### 6.1.2 认证问题
**问题**：API密钥认证失败

**排查步骤**：
1. 验证API密钥
   ```bash
   echo $HERA_API_KEY
   ```

2. 检查密钥权限
   ```bash
   curl -H "X-API-Key: $HERA_API_KEY" http://hera-server.example.com:8080/api/validate
   ```

3. 检查密钥过期时间
   ```bash
   # 在Hera管理控制台中查看密钥状态
   ```

**解决方案**：
- 更新有效的API密钥
- 检查密钥权限配置
- 重新生成过期的密钥

#### 6.1.3 数据上报问题
**问题**：指标数据未上报

**排查步骤**：
1. 检查应用日志
   ```bash
   tail -f /var/log/inventory-service/application.log | grep -i "hera"
   ```

2. 检查指标导出器状态
   ```java
   // 在应用中添加健康检查端点
   @GetMapping("/actuator/hera-exporter")
   public String getExporterStatus() {
       return "Exporter status: " + heraMetricsExporter.getStatus();
   }
   ```

3. 检查网络延迟
   ```bash
   curl -w "@curl-format.txt" -o /dev/null -s "http://hera-server.example.com:8080/api/metrics"
   cat curl-format.txt
   ```

**解决方案**：
- 检查应用日志中的错误信息
- 验证导出器配置
- 优化网络连接
- 增加重试机制

#### 6.1.4 告警问题
**问题**：告警未触发或未收到

**排查步骤**：
1. 检查Alertmanager配置
   ```bash
   curl http://alertmanager.example.com:9093/api/v1/status
   ```

2. 检查告警规则状态
   ```bash
   amtool config query alertmanager.example.com:9093
   ```

3. 检查通知配置
   ```bash
   # 检查邮件服务器日志
   tail -f /var/log/mail.log | grep -i "alertmanager"
   ```

4. 测试告警规则
   ```bash
   # 发送测试告警
   amtool alert add alertmanager.example.com:9093 TestAlert
   ```

**解决方案**：
- 验证Alertmanager配置
- 检查告警规则语法
- 测试通知渠道
- 检查告警路由配置

### 6.2 日志收集

启用详细的日志记录：

```yaml
logging:
  level:
    com.inventory.monitoring: DEBUG
    com.hera.monitoring: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/inventory-service/hera-integration.log
    max-size: 100MB
    max-history: 30
```

### 6.3 性能优化

优化Hera集成性能：

```java
@Configuration
public class HeraPerformanceConfig {
    
    @Bean
    public MeterRegistryCustomizer meterRegistryCustomizer() {
        return registry -> registry.config()
                .commonTags(Map.of(
                        "application", "inventory-management",
                        "environment", System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "prod")
                ))
                .meterFilter(new MeterFilter() {
                    @Override
                    public boolean accept(Meter.Id id) {
                        return id.getName().startsWith("hera_");
                    }
                });
    }
    
    @Bean
    public ExecutorService executorService() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder("hera-executor")
                .setDaemon(true)
                .build();
        
        return new ThreadPoolExecutor(
                10,
                100,
                60L,
                TimeUnit.SECONDS,
                namedThreadFactory
        );
    }
}
```

## 7. 最佳实践

### 7.1 配置管理
- 使用环境变量管理敏感配置
- 为不同环境使用不同的配置文件
- 定期审查和更新配置
- 使用配置版本控制

### 7.2 监控策略
- 设置合理的告警阈值
- 使用告警聚合和降噪
- 配置告警升级策略
- 定期审查告警规则有效性

### 7.3 性能优化
- 使用批量上报减少网络开销
- 启用数据压缩
- 配置适当的采样率
- 使用异步上报避免阻塞

### 7.4 故障处理
- 实现自动重试机制
- 配置超时和熔断
- 实现降级策略
- 记录详细的错误日志

### 7.5 安全考虑
- 使用HTTPS进行数据传输
- 加密API密钥
- 实现访问控制
- 定期轮换密钥

## 8. 验证和测试

### 8.1 集成验证清单
- [ ] Maven依赖已正确添加
- [ ] 配置文件已正确设置
- [ ] Hera服务可访问
- [ ] 指标数据正常上报
- [ ] 追踪数据正常上报
- [ ] 告警规则正常触发
- [ ] 通知渠道正常工作

### 8.2 测试场景
1. **功能测试**
   - 指标上报功能
   - 追踪功能
   - 告警触发功能
   - 通知发送功能

2. **性能测试**
   - 高并发指标上报
   - 大批量数据处理
   - 长时间运行稳定性

3. **故障测试**
   - 网络中断恢复
   - Hera服务不可用处理
   - 配置错误处理

4. **集成测试**
   - 与Prometheus集成
   - 与Grafana集成
   - 与Alertmanager集成
   - 与Jaeger集成

## 9. 附录

### 9.1 配置参数参考
| 参数 | 默认值 | 说明 | 可选范围 |
|------|---------|------|---------|
| hera.monitoring.enabled | true | 是否启用Hera监控 | true/false |
| hera.monitoring.client.timeout | 30000 | 客户端超时时间（毫秒） | 5000-60000 |
| hera.monitoring.metrics.export-interval-seconds | 15 | 指标导出间隔（秒） | 10-300 |
| hera.monitoring.metrics.batch-size | 1000 | 批量大小 | 100-10000 |
| hera.monitoring.tracing.sampling-rate | 0.1 | 追踪采样率 | 0.01-1.0 |
| hera.monitoring.alerting.aggregation.window-minutes | 5 | 告警聚合窗口（分钟） | 1-60 |
| hera.monitoring.alerting.deduplication.similarity-threshold | 0.8 | 去重相似度阈值 | 0.5-1.0 |

### 9.2 支持和资源
- Hera官方文档：https://docs.hera-monitoring.com
- OpenTelemetry文档：https://opentelemetry.io/docs/
- Micrometer文档：https://micrometer.io/docs/
- Prometheus文档：https://prometheus.io/docs/
- 技术支持邮箱：support@hera-monitoring.com

### 9.3 变更日志
| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |
