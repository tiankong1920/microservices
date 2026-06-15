package com.inventory.common.core.kafka.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件模型.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    /** 事件ID. */
    private String eventId;

    /** 事件类型. */
    private OrderEventType eventType;

    /** 订单ID. */
    private Long orderId;

    /** 订单号. */
    private String orderNumber;

    /** 客户ID. */
    private Long customerId;

    /** 订单金额. */
    private Double amount;

    /** 订单状态. */
    private String orderStatus;

    /** 操作前状态. */
    private String previousStatus;

    /** 操作后状态. */
    private String currentStatus;

    /** 事件时间. */
    private LocalDateTime eventTime;

    /** 操作人. */
    private String operator;

    /** 备注. */
    private String remark;

    /**
     * 订单事件类型枚举.
     */
    public enum OrderEventType {
        /** 订单创建. */
        ORDER_CREATED,

        /** 订单更新. */
        ORDER_UPDATED,

        /** 订单取消. */
        ORDER_CANCELLED,

        /** 订单完成. */
        ORDER_COMPLETED,

        /** 订单支付. */
        ORDER_PAID,

        /** 订单发货. */
        ORDER_SHIPPED,

        /** 订单退款. */
        ORDER_REFUNDED,

        /** 订单异常. */
        ORDER_EXCEPTION
    }
}
