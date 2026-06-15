package com.inventory.common.core;

/**
 * 基础错误码常量类.
 * 定义系统级和通用错误码.
 */
public final class ErrorCodeConstants {

    /** 系统错误类型. */
    public static final String ERROR_TYPE_SYSTEM = "01";

    /** 参数错误类型. */
    public static final String ERROR_TYPE_PARAM = "02";

    /** 资源不存在错误类型. */
    public static final String ERROR_TYPE_RESOURCE = "03";

    /** 业务逻辑错误类型. */
    public static final String ERROR_TYPE_BUSINESS = "04";

    /** 认证授权错误类型. */
    public static final String ERROR_TYPE_AUTH = "05";

    /** 第三方服务错误类型. */
    public static final String ERROR_TYPE_THIRD_PARTY = "06";

    /** 参数无效错误码. */
    public static final String COMMON_PARAM_INVALID = "COM-02-001";

    /** 参数缺失错误码. */
    public static final String COMMON_PARAM_MISSING = "COM-02-002";

    /** 资源不存在错误码. */
    public static final String COMMON_RESOURCE_NOT_FOUND = "COM-03-001";

    /** 操作失败错误码. */
    public static final String COMMON_OPERATION_FAILED = "COM-04-001";

    /** 系统错误错误码. */
    public static final String COMMON_SYSTEM_ERROR = "COM-01-001";

    /** 令牌过期错误码. */
    public static final String AUTH_TOKEN_EXPIRED = "COM-05-001";

    /** 令牌无效错误码. */
    public static final String AUTH_TOKEN_INVALID = "COM-05-002";

    /** 权限不足错误码. */
    public static final String AUTH_PERMISSION_DENIED = "COM-05-003";

    /**
     * 私有构造函数，防止实例化.
     */
    private ErrorCodeConstants() {
        // Utility class - prevent instantiation
    }
}
