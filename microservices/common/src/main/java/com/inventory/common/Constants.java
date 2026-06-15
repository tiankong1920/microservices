package com.inventory.common;

/**
 * 系统常量定义类.
 */
public final class Constants {

    /** 默认分页大小. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小. */
    public static final int MAX_PAGE_SIZE = 100;

    /** 默认页码. */
    public static final int DEFAULT_PAGE = 1;

    /** 成功消息. */
    public static final String SUCCESS_MESSAGE = "Success";

    /** 错误消息. */
    public static final String ERROR_MESSAGE = "Error";

    /**
     * 私有构造函数，防止实例化.
     */
    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
