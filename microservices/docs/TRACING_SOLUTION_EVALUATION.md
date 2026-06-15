# 链路追踪方案评估

## 推荐方案: OpenTelemetry + Jaeger

理由：
1. CNCF 官方标准
2. Spring Cloud 2024 官方集成
3. 多语言支持
4. 统一 traces/metrics/logs

## 已实施

### 1. Docker Compose 监控栈 (✅ 已完成)
在 `docker-compose.monitoring.yml` 中添加了：
- `jaeger-collector` - 接收 OTLP 协议的 trace 数据
- `jaeger-query` - Jaeger UI (http://localhost:16686)
- `elasticsearch` - Jaeger trace 存储后端

启动命令：
```bash
docker-compose -f docker-compose.monitoring.yml up -d jaeger-collector jaeger-query elasticsearch
```

### 2. Java Agent 方式（推荐，无需代码改动）

下载 Agent：
```powershell
.\scripts\download-otel-agent.ps1 -Version 2.1.0
```

启动服务时添加 JVM 参数：
```bash
java -javaagent:agents/opentelemetry-javaagent-2.1.0.jar \
     -Dotel.service.name=inventory-service \
     -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
     -Dotel.resource.attributes="service.version=1.0.0" \
     -jar app.jar
```

### 3. Docker 环境变量配置
```yaml
environment:
  OTEL_SERVICE_NAME: inventory-service
  OTEL_EXPORTER_OTLP_ENDPOINT: http://jaeger-collector:4317
  OTEL_EXPORTER_OTLP_PROTOCOL: grpc
  OTEL_TRACES_SAMPLER: parentbased_traceidratio
  OTEL_TRACES_SAMPLER_ARG: "0.1"
```

## 环境要求
- Jaeger Collector: localhost:4317 (gRPC), 4318 (HTTP)
- Jaeger UI: http://localhost:16686
- Elasticsearch: http://localhost:9200

## 验证
1. 启动监控栈后访问 http://localhost:16686
2. 调用服务API产生一些请求
3. 在Jaeger UI中搜索服务名查看链路

## 替代方案
如需更简捷方案，可选 **SkyWalking** Java Agent 模式，无需代码改动。
