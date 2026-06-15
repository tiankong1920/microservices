package com.inventory.common.core.kafka.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存事件模型.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    /** 事件ID. */
    private String eventId;

    /** 事件类型. */
    private InventoryEventType eventType;

    /** 产品ID. */
    private Long productId;

    /** 数量. */
    private Integer quantity;

    /** 操作前数量. */
    private Integer previousQuantity;

    /** 操作后数量. */
    private Integer currentQuantity;

    /** 事件时间. */
    private LocalDateTime eventTime;

    /** 操作人. */
    private String operator;

    /** 备注. */
    private String remark;

    /**
     * 库存事件类型枚举.
     */
    public enum InventoryEventType {
        /** 入库. */
        INBOUND,

        /** 出库. */
        OUTBOUND,

        /** 库存调整. */
        ADJUSTMENT,

        /** 库存不足. */
        LOW_STOCK,

        /** 库存预警. */
        STOCK_ALERT
    }
}
