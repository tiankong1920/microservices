package com.inventory.common.core.kafka;

public interface IKafkaMessageService {
    
    void sendMessage(String topic, String message);
    
    void sendMessage(String topic, String key, String message);
    
    void sendMessage(String topic, Integer partition, String key, String message);
}
