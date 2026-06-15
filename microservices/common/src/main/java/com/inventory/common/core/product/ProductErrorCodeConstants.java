package com.inventory.common.core.product;

import com.inventory.common.core.ErrorCodeConstants;

/**
 * 产品服务错误码常量类.
 * 模块前缀: PROD.
 */
public final class ProductErrorCodeConstants {

    /** 模块前缀. */
    private static final String MODULE_PREFIX = "PROD";

    /** 产品不存在错误码. */
    public static final String PRODUCT_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-001";

    /** 产品创建失败错误码. */
    public static final String PRODUCT_CREATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-001";

    /** 产品更新失败错误码. */
    public static final String PRODUCT_UPDATE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-002";

    /** 产品删除失败错误码. */
    public static final String PRODUCT_DELETE_FAILED = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-003";

    /** 产品状态无效错误码. */
    public static final String PRODUCT_STATUS_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-004";

    /** 产品重复错误码. */
    public static final String PRODUCT_DUPLICATE = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_BUSINESS + "-005";

    /** 产品分类不存在错误码. */
    public static final String PRODUCT_CATEGORY_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-002";

    /** 产品分类无效错误码. */
    public static final String PRODUCT_CATEGORY_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-001";

    /** 产品属性不存在错误码. */
    public static final String PRODUCT_ATTR_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-003";

    /** 产品属性无效错误码. */
    public static final String PRODUCT_ATTR_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-002";

    /** BOM不存在错误码. */
    public static final String BOM_NOT_FOUND = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_RESOURCE + "-004";

    /** BOM无效错误码. */
    public static final String BOM_INVALID = MODULE_PREFIX + "-"
            + ErrorCodeConstants.ERROR_TYPE_PARAM + "-003";

    /**
     * 私有构造函数，防止实例化.
     */
    private ProductErrorCodeConstants() {
        // Utility class - prevent instantiation
    }
}
