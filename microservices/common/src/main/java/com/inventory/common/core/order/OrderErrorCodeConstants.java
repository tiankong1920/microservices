package com.inventory.common.core.order;

import com.inventory.common.core.ErrorCodeConstants;

/**
 * 订单服务错误码常量类.
 * 模块前缀: ORD.
 */
public final class OrderErrorCodeConstants {

    /** 模块前缀. */
    private static final String MODULE_PREFIX = "ORD";

    /** 订单不存在错误码. */
    public static final String ORDER_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-001";

    /** 订单状态错误码. */
    public static final String ORDER_STATUS_ERROR = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-001";

    /** 订单创建失败错误码. */
    public static final String ORDER_CREATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-002";

    /** 订单更新失败错误码. */
    public static final String ORDER_UPDATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-003";

    /** 订单删除失败错误码. */
    public static final String ORDER_DELETE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-004";

    /** 订单取消失败错误码. */
    public static final String ORDER_CANCEL_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-005";

    /** 订单支付失败错误码. */
    public static final String ORDER_PAY_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-006";

    /** 订单项不存在错误码. */
    public static final String ORDER_ITEM_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-002";

    /** 订单项无效错误码. */
    public static final String ORDER_ITEM_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-001";

    /** 订单流程错误错误码. */
    public static final String ORDER_FLOW_ERROR = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-007";

    /** 订单超时错误码. */
    public static final String ORDER_TIMEOUT = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-008";

    /**
     * 私有构造函数，防止实例化.
     */
    private OrderErrorCodeConstants() {
        // Utility class - prevent instantiation
    }
}
