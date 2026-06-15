/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 错误码枚举类，用于定义系统中的业务错误码。
 * <p>
 * 该类提供了错误码的注册机制，支持动态添加自定义错误码。
 * 同时提供了默认的错误码定义。
 * </p>
 */
@Getter
public class ErrorCode {

    /**
 * 错误码映射，用于存储所有注册的错误码。
     */
    private static final Map<String, ErrorCode> ERROR_CODE_MAP = new HashMap<>();

    /**
 * 错误码，用于标识具体的业务异常类型。
     */
    private final String code;

    /**
 * 错误消息键，用于国际化消息。
     */
    private final String messageKey;

    /**
 * 默认错误消息，当国际化消息加载失败时使用。
     */
    private final String defaultMessage;

    /**
 * 构造方法，创建错误码实例并注册到全局映射中。
     *
     * @param code          错误码。
     * @param messageKey    错误消息键。
     * @param defaultMessage 默认错误消息。
     */
    public ErrorCode(final String code, final String messageKey, final String defaultMessage) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        ERROR_CODE_MAP.put(code, this);
    }

    /**
 * 通过错误码获取ErrorCode实例。
     *
     * @param code 错误码。
     * @return ErrorCode实例，如果不存在则返回null。
     */
    public static ErrorCode getByCode(final String code) {
        return ERROR_CODE_MAP.get(code);
    }

    /**
 * 注册自定义错误码。
     *
     * @param code          错误码。
     * @param messageKey    错误消息键。
     * @param defaultMessage 默认错误消息。
     * @return 注册的ErrorCode实例。
     */
    public static ErrorCode register(final String code, final String messageKey, final String defaultMessage) {
        return new ErrorCode(code, messageKey, defaultMessage);
    }

    /**
 * 获取所有注册的错误码。
     *
     * @return 错误码映射。
     */
    public static Map<String, ErrorCode> getAllErrorCodes() {
        return new HashMap<>(ERROR_CODE_MAP);
    }

    /**
 * 清除所有注册的错误码，仅用于测试。
     */
    static void clear() {
        ERROR_CODE_MAP.clear();
    }

    // 默认错误码定义

        public static final ErrorCode UNKNOWN_ERROR = new ErrorCode(

                "UNKNOWN_ERROR", "error.unknown", "Unknown error occurred");

        public static final ErrorCode INVALID_PARAMETER = new ErrorCode(

                "INVALID_PARAMETER", "error.invalid.parameter", "Invalid parameter");

        public static final ErrorCode RESOURCE_NOT_FOUND = new ErrorCode(

                "RESOURCE_NOT_FOUND", "error.resource.not.found", "Resource not found");

        public static final ErrorCode DATABASE_ERROR = new ErrorCode(

                "DATABASE_ERROR", "error.database", "Database error occurred");

        public static final ErrorCode NETWORK_ERROR = new ErrorCode(

                "NETWORK_ERROR", "error.network", "Network error occurred");

        public static final ErrorCode AUTHENTICATION_ERROR = new ErrorCode(

                "AUTHENTICATION_ERROR", "error.authentication", "Authentication failed");

        public static final ErrorCode AUTHORIZATION_ERROR = new ErrorCode(

                "AUTHORIZATION_ERROR", "error.authorization", "Authorization failed");

        public static final ErrorCode SERVICE_UNAVAILABLE = new ErrorCode(

                "SERVICE_UNAVAILABLE", "error.service.unavailable", "Service unavailable");

        public static final ErrorCode TIMEOUT_ERROR = new ErrorCode(

                "TIMEOUT_ERROR", "error.timeout", "Operation timeout");

        public static final ErrorCode VALIDATION_ERROR = new ErrorCode(

                "VALIDATION_ERROR", "error.validation", "Validation failed");

        public static final ErrorCode BUSINESS_OPERATION_FAILED = new ErrorCode(

                "BUSINESS_OPERATION_FAILED", "error.business.operation.failed", "Business operation failed");

    }
