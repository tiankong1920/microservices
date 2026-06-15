/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ELK Stack集成类，用于实现日志和指标的集中管理.
 * <p>
 * 该类提供了与Elasticsearch、Logstash和Kibana的集成功能，.
 * 支持将应用程序的日志和监控指标发送到ELK Stack进行集中管理和分析.
 * </p>
 */
@Component
@ConditionalOnProperty(
        prefix = "inventory.monitoring.elk",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ElkStackIntegration implements InitializingBean {

    /**
 * 日志记录器，用于记录ELK集成的执行状态.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ElkStackIntegration.class);

    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_APPLICATION = "application";

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 60L;

    /**
     * 共享的 ObjectMapper 实例（Jackson 3 不可变对象）.
     */
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    /**
 * Elasticsearch Java API客户端.
     */
    private final ElasticsearchClient elasticsearchClient;

    /**
 * 指标注册表，用于收集和报告应用程序的监控指标.
     */
    private final MeterRegistry meterRegistry;

    /**
 * 定时任务执行器，用于定期执行指标收集和日志发送任务.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
 * 是否启用指标收集功能。.
     */
    @Value("${inventory.monitoring.elk.metrics.enabled:true}")
    private boolean metricsEnabled;

    /**
 * 是否启用日志收集功能。.
     */
    @Value("${inventory.monitoring.elk.logging.enabled:true}")
    private boolean loggingEnabled;

    /**
 * 指标收集间隔时间（秒）。.
     */
    @Value("${inventory.monitoring.elk.metrics.interval:60}")
    private int metricsInterval;

    /**
 * 日志索引名称前缀。.
     */
    @Value("${inventory.monitoring.elk.logging.index.prefix:application-logs}")
    private String logIndexPrefix;

    /**
 * 指标索引名称。.
     */
    @Value("${inventory.monitoring.elk.metrics.index:application-metrics}")
    private String metricsIndex;

    /**
 * 应用程序名称，用于标识日志和指标的来源。.
     */
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * 构造方法，注入Elasticsearch客户端和指标注册表。.
     *
     * @param elasticsearchClient Elasticsearch客户端
     * @param meterRegistry       指标注册表
     */
    public ElkStackIntegration(final ElasticsearchClient elasticsearchClient,
                               final MeterRegistry meterRegistry) {
        this.elasticsearchClient = elasticsearchClient;
        this.meterRegistry = meterRegistry;
    }

    /**
 * 初始化方法，在Bean初始化完成后执行。.
 * 创建必要的索引并启动定时任务。.
     *
 * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Initializing ELK Stack Integration...");

        // 创建索引
        createIndices();

        // 启动定时任务
        if (metricsEnabled) {
            scheduler.scheduleAtFixedRate(
                    this::collectAndSendMetrics,
                    0,
                    metricsInterval,
                    TimeUnit.SECONDS
            );
        }

        LOGGER.info("ELK Stack Integration initialized successfully");
    }

    /**
 * 创建Elasticsearch索引。.
 * 如果索引不存在，则创建指标索引和日志索引模板。.
     */
    private void createIndices() {
        try {
            // 检查并创建指标索引
            final ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(metricsIndex));
            final boolean metricsIndexExists = elasticsearchClient.indices().exists(existsRequest).value();

            if (!metricsIndexExists) {
                final CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
                        .index(metricsIndex)
                        .mappings(m -> m
                                .properties(FIELD_TIMESTAMP, p -> p.date(d -> d))
                                .properties(FIELD_APPLICATION, p -> p.keyword(k -> k))
                                .properties("metric_name", p -> p.keyword(k -> k))
                                .properties("metric_value", p -> p.double_(d -> d))
                                .properties("tags", p -> p.object(o -> o))
                        )
                );
                elasticsearchClient.indices().create(createRequest);
                LOGGER.info("Created metrics index: {}", metricsIndex);
            }

            LOGGER.info("Indices created successfully");
        } catch (final IOException e) {
            LOGGER.error("Failed to create indices: {}", e.getMessage(), e);
        }
    }

    /**
 * 收集并发送指标数据到Elasticsearch。.
 * 收集所有注册的指标（计数器、计时器、仪表盘等）并发送到ELK Stack。.
     */
    private void collectAndSendMetrics() {
        try {
            final Instant timestamp = Instant.now();

            // 收集所有指标
            meterRegistry.getMeters().forEach(meter -> {
                try {
                    final ObjectNode metricDoc = OBJECT_MAPPER.createObjectNode();
                    metricDoc.put(FIELD_TIMESTAMP, timestamp.toString());
                    metricDoc.put(FIELD_APPLICATION, applicationName);
                    metricDoc.put("metric_name", meter.getId().getName());
                    metricDoc.put("metric_type", meter.getId().getType().name());

                    // 添加标签
                    final ObjectNode tagsNode = OBJECT_MAPPER.createObjectNode();
                    meter.getId().getTags().forEach(tag ->
                            tagsNode.put(tag.getKey(), tag.getValue())
                    );
                    metricDoc.set("tags", tagsNode);

                    // 根据指标类型处理
                    meter.measure().forEach(measurement -> {
                        metricDoc.put("statistic", measurement.getStatistic().name());
                        metricDoc.put("metric_value", measurement.getValue());
                    });

                    // 索引文档
                    final IndexRequest<ObjectNode> indexRequest = IndexRequest.of(i -> i
                            .index(metricsIndex)
                            .document(metricDoc)
                    );

                    final IndexResponse response = elasticsearchClient.index(indexRequest);
                    LOGGER.debug("Indexed metric: {}, id: {}", meter.getId().getName(), response.id());

                } catch (final IOException e) {
                    LOGGER.error("Failed to index metric: {}", e.getMessage(), e);
                }
            });
            LOGGER.info("Metrics collected and sent successfully");
        } catch (final Exception e) {
            LOGGER.error("Failed to collect and send metrics: {}", e.getMessage(), e);
        }
    }

    /**
 * 发送日志到Elasticsearch。.
     *
 * @param level     日志级别
 * @param message   日志消息
 * @param loggerName 记录器名称
 * @param throwable 异常对象（可选）
     */
    public void sendLog(final String level, final String message, final String loggerName, final Throwable throwable) {
        if (!loggingEnabled) {
            return;
        }

        try {
            final Instant timestamp = Instant.now();
            final String indexName = logIndexPrefix + "-" + timestamp.toString().substring(0, 10);

            final ObjectNode logDoc = OBJECT_MAPPER.createObjectNode();
            logDoc.put(FIELD_TIMESTAMP, timestamp.toString());
            logDoc.put(FIELD_APPLICATION, applicationName);
            logDoc.put("level", level);
            logDoc.put("message", message);
            logDoc.put("logger", loggerName);

            if (throwable != null) {
                logDoc.put("exception_class", throwable.getClass().getName());
                logDoc.put("exception_message", throwable.getMessage());
            }

            // 添加MDC上下文信息
            if (org.slf4j.MDC.getCopyOfContextMap() != null) {
                final ObjectNode mdcNode = OBJECT_MAPPER.createObjectNode();
                org.slf4j.MDC.getCopyOfContextMap().forEach(mdcNode::put);
                logDoc.set("mdc", mdcNode);
            }

            final IndexRequest<ObjectNode> indexRequest = IndexRequest.of(i -> i
                    .index(indexName)
                    .document(logDoc)
            );

            elasticsearchClient.index(indexRequest);
            LOGGER.debug("Log sent to ELK: {}", message);

        } catch (final IOException e) {
            LOGGER.error("Failed to send log to ELK: {}", e.getMessage(), e);
        }
    }

    /**
 * 发送自定义事件到Elasticsearch。.
     *
 * @param eventType 事件类型
 * @param eventData 事件数据
     */
    public void sendEvent(final String eventType, final Map<String, Object> eventData) {
        try {
            final Instant timestamp = Instant.now();
            final String indexName = "application-events-" + timestamp.toString().substring(0, 10);

            final Map<String, Object> eventDoc = new HashMap<>();
            eventDoc.put(FIELD_TIMESTAMP, timestamp.toString());
            eventDoc.put(FIELD_APPLICATION, applicationName);
            eventDoc.put("event_type", eventType);
            eventDoc.putAll(eventData);

            final IndexRequest<ObjectNode> indexRequest = IndexRequest.of(i -> i
                    .index(indexName)
                    .document(OBJECT_MAPPER.valueToTree(eventDoc))
            );

            elasticsearchClient.index(indexRequest);
            LOGGER.debug("Event sent to ELK: {}", eventType);
        } catch (final Exception e) {
            LOGGER.error("Failed to send event to ELK: {}", e.getMessage(), e);
        }
    }

    /**
 * 关闭方法，用于释放资源。.
 * 关闭定时任务执行器和Elasticsearch客户端。.
     */
    public void shutdown() {
        LOGGER.info("Shutting down ELK Stack Integration...");

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (final InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.info("ELK Stack Integration shut down successfully");
    }
}
