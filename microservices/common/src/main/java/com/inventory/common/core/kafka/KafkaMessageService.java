package com.inventory.common.core.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

/**
 * Kafka消息服务，用于发送和处理Kafka消息.
 *
 * <p>该服务提供了Kafka消息发送的核心功能，支持同步和异步消息发送，
 * 支持指定主题、分区和消息键的消息发送方式。所有消息发送操作都是异步的，
 * 通过CompletableFuture返回发送结果。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 * @see KafkaTemplate
 * @see SendResult
 */
@Service
@SuppressWarnings("null")
public class KafkaMessageService {

    /** 日志记录器. */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageService.class);

    /** 发送成功日志模板. */
    private static final String SEND_SUCCESS_TEMPLATE =
            "Message sent successfully to topic: {}, partition: {}, offset: {}";

    /** 发送失败日志模板. */
    private static final String SEND_FAILURE_TEMPLATE =
            "Failed to send message to topic: {}";

    /** Kafka模板. */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 构造函数.
     *
     * @param kafkaTemplate Kafka模板
     */
    public KafkaMessageService(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送消息到指定主题.
     *
     * @param topic 主题
     * @param message 消息内容
     */
    public void sendMessage(final String topic, final String message) {
        sendMessage(topic, null, message);
    }

    /**
     * 发送带键的消息到指定主题.
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     */
    public void sendMessage(final String topic, final String key, final String message) {
        LOGGER.debug("Sending message to topic: {}, key: {}, message: {}", topic, key, message);

        final CompletableFuture<SendResult<String, String>> future;
        if (key != null) {
            future = kafkaTemplate.send(topic, key, message);
        } else {
            future = kafkaTemplate.send(topic, message);
        }

        handleSendResult(future, topic);
    }

    /**
     * 发送消息到指定主题和分区.
     *
     * @param topic 主题
     * @param partition 分区
     * @param key 消息键
     * @param message 消息内容
     */
    public void sendMessage(final String topic, final Integer partition,
            final String key, final String message) {
        LOGGER.debug("Sending message to topic: {}, partition: {}, key: {}, message: {}",
                topic, partition, key, message);

        final CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, partition, key, message);

        handleSendResult(future, topic);
    }

    /**
     * 处理消息发送结果.
     *
     * @param future 发送结果Future
     * @param topic 主题
     */
    private void handleSendResult(final CompletableFuture<SendResult<String, String>> future,
            final String topic) {
        future.thenAccept(result -> {
            LOGGER.debug(SEND_SUCCESS_TEMPLATE,
                    topic, result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        }).exceptionally(ex -> {
            LOGGER.error(SEND_FAILURE_TEMPLATE, topic, ex);
            return null;
        });
    }
}
