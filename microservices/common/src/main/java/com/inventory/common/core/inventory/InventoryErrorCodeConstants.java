package com.inventory.common.core.inventory;

import com.inventory.common.core.ErrorCodeConstants;

/**
 * 库存服务错误码常量类.
 * 模块前缀: INV.
 */
public final class InventoryErrorCodeConstants {

    /** 模块前缀. */
    private static final String MODULE_PREFIX = "INV";

    /** 库存不存在错误码. */
    public static final String INVENTORY_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-001";

    /** 库存不足错误码. */
    public static final String INVENTORY_INSUFFICIENT = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-001";

    /** 库存锁定失败错误码. */
    public static final String INVENTORY_LOCK_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-002";

    /** 库存解锁失败错误码. */
    public static final String INVENTORY_UNLOCK_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-003";

    /** 库存调整失败错误码. */
    public static final String INVENTORY_ADJUST_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-004";

    /** 批次不存在错误码. */
    public static final String BATCH_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-002";

    /** 批次已过期错误码. */
    public static final String BATCH_EXPIRED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-005";

    /** 批次库存不足错误码. */
    public static final String BATCH_INSUFFICIENT = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-006";

    /** 仓库不存在错误码. */
    public static final String WAREHOUSE_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-003";

    /** 仓库已满错误码. */
    public static final String WAREHOUSE_FULL = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-007";

    /**
     * 私有构造函数，防止实例化.
     */
    private InventoryErrorCodeConstants() {
        // Utility class - prevent instantiation
    }
}
