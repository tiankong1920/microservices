package com.inventory.common.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.inventory.common.core.CommonConstants;

/**
 * Kafka配置类，用于配置Kafka生产者和消费者.
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings("null")
public class KafkaConfig {

    /** Kafka服务器地址. */
    private static final String KAFKA_BOOTSTRAP_SERVERS =
            "localhost:9094,localhost:9095,localhost:9096";

    /** 消费者组ID. */
    private static final String CONSUMER_GROUP_ID = "inventory-group";

    /** 最早偏移量重置策略. */
    private static final String EARLIEST_OFFSET_RESET = "earliest";

    /**
     * Kafka生产者配置.
     *
     * @return 生产者工厂
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.RETRIES_CONFIG, CommonConstants.KAFKA_DEFAULT_RETRIES);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, CommonConstants.KAFKA_DEFAULT_BATCH_SIZE);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, CommonConstants.KAFKA_DEFAULT_LINGER_MS);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, CommonConstants.KAFKA_DEFAULT_BUFFER_MEMORY);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                CommonConstants.KAFKA_MAX_IN_FLIGHT_REQUESTS);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, CommonConstants.KAFKA_REQUEST_TIMEOUT_MS);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka模板.
     *
     * @return Kafka模板
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Kafka消费者配置.
     *
     * @return 消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST_OFFSET_RESET);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka监听器容器工厂.
     *
     * @return 监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
