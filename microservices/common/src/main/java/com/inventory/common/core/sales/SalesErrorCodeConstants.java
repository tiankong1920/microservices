package com.inventory.common.core.sales;

import com.inventory.common.core.ErrorCodeConstants;

/**
 * 销售服务错误码常量类.
 * 模块前缀: SAL.
 */
public final class SalesErrorCodeConstants {

    /** 模块前缀. */
    private static final String MODULE_PREFIX = "SAL";

    /** 销售订单不存在错误码. */
    public static final String SALES_ORDER_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-001";

    /** 销售订单创建失败错误码. */
    public static final String SALES_ORDER_CREATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-001";

    /** 销售订单更新失败错误码. */
    public static final String SALES_ORDER_UPDATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-002";

    /** 销售订单删除失败错误码. */
    public static final String SALES_ORDER_DELETE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-003";

    /** 销售订单状态错误错误码. */
    public static final String SALES_ORDER_STATUS_ERROR = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-004";

    /** 销售订单项不存在错误码. */
    public static final String SALES_ORDER_ITEM_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-002";

    /** 销售订单项无效错误码. */
    public static final String SALES_ORDER_ITEM_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-001";

    /** 客户不存在错误码. */
    public static final String CUSTOMER_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-003";

    /** 客户无效错误码. */
    public static final String CUSTOMER_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-002";

    /**
     * 私有构造函数，防止实例化.
     */
    private SalesErrorCodeConstants() {
        // Utility class - prevent instantiation
    }
}
