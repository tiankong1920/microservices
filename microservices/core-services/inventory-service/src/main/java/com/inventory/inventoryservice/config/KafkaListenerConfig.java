package com.inventory.inventoryservice.config;

import com.inventory.common.core.kafka.event.InventoryEvent;
import com.inventory.common.core.kafka.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka消息监听器配置，用于接收来自其他服务的消息.
 */
@Component
public class KafkaListenerConfig {

    /**
 * 日志记录器.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerConfig.class);

    /**
 * 监听库存事件.
     *
 * @param inventoryEvent 库存事件
     */
    @KafkaListener(topics = "inventory-events", groupId = "inventory-service-group")
    public void listenInventoryEvents(InventoryEvent inventoryEvent) {
        LOGGER.info("Received inventory event: {}", inventoryEvent);
        // 处理库存事件
        // 这里可以根据事件类型执行相应的业务逻辑
    }

    /**
 * 监听订单事件.
     *
 * @param orderEvent 订单事件
     */
    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    public void listenOrderEvents(OrderEvent orderEvent) {
        LOGGER.info("Received order event: {}", orderEvent);
        // 处理订单事件
        // 这里可以根据订单状态变化执行相应的库存操作
        // 例如，当订单创建时，预留库存
        // 当订单取消时，释放预留库存
    }
}
