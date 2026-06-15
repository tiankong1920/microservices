package com.inventory.common.core.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class NoOpKafkaMessageService implements IKafkaMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpKafkaMessageService.class);

    @Override
    public void sendMessage(final String topic, final String message) {
        LOGGER.debug("Kafka not configured, skipping message to topic: {}", topic);
    }

    @Override
    public void sendMessage(final String topic, final String key, final String message) {
        LOGGER.debug("Kafka not configured, skipping message to topic: {} with key: {}", topic, key);
    }

    @Override
    public void sendMessage(final String topic, final Integer partition,
            final String key, final String message) {
        LOGGER.debug("Kafka not configured, skipping message to topic: {}, partition: {}", topic, partition);
    }
}
