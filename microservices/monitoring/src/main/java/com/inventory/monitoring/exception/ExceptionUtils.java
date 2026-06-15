/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 异常处理工具类，提供异常处理相关的工具方法。
 */
public class ExceptionUtils {

    /**
     * 日志记录器，用于记录异常信息。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * 资源包基础名称。
     */
    private static final String BUNDLE_BASE_NAME = "messages";

    /**
     * 私有构造方法，防止实例化。
     */
    private ExceptionUtils() {
        // 防止实例化
    }

    /**
     * 从资源包中获取国际化消息。
     *
     * @param messageKey 消息键
     * @return 国际化消息
     */
    public static String getMessageFromBundle(final String messageKey) {
        return getMessageFromBundle(messageKey, Locale.getDefault());
    }

    /**
     * 从资源包中获取国际化消息，支持指定语言环境。
     *
     * @param messageKey 消息键
     * @param locale     语言环境
     * @return 国际化消息
     */
    public static String getMessageFromBundle(final String messageKey, final Locale locale) {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
            return bundle.getString(messageKey);
        } catch (final Exception e) {
            LOGGER.warn("Failed to get message from bundle for key: {} in locale: {}", messageKey, locale, e);
            return messageKey;
        }
    }

    /**
     * 从资源包中获取国际化消息，支持参数化。
     *
     * @param messageKey 消息键
     * @param params     消息参数
     * @return 国际化消息
     */
    public static String getMessageFromBundle(final String messageKey, final Object... params) {
        return getMessageFromBundle(messageKey, Locale.getDefault(), params);
    }

    /**
     * 从资源包中获取国际化消息，支持指定语言环境和参数化。
     *
     * @param messageKey 消息键
     * @param locale     语言环境
     * @param params     消息参数
     * @return 国际化消息
     */
    public static String getMessageFromBundle(final String messageKey, final Locale locale, final Object... params) {
        try {
            final String message = getMessageFromBundle(messageKey, locale);
            return String.format(message, params);
        } catch (final Exception e) {
            LOGGER.warn("Failed to format message for key: {}", messageKey, e);
            return messageKey;
        }
    }

    /**
     * 根据错误码获取HTTP状态码。
     *
     * @param errorCode 错误码
     * @return HTTP状态码
     */
    public static int getHttpStatusFromErrorCode(final String errorCode) {
        final ErrorCode code = ErrorCode.getByCode(errorCode);
        if (code != null) {
            return 500;
        }
        return 500;
    }

    /**
     * 构建标准化的错误响应消息。
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @return 标准化的错误响应消息
     */
    public static String buildErrorResponseMessage(final String errorCode, final String message) {
        return String.format("[%s] %s", errorCode, message);
    }

    /**
     * 记录异常信息，包含错误码、错误消息和异常原因。
     *
     * @param logger    日志记录器
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     异常原因
     */
    public static void logException(final Logger logger, final String errorCode, final String message, final Throwable cause) {
        if (cause != null) {
            logger.error("Exception occurred: {}, error code: {}", message, errorCode, cause);
        } else {
            logger.error("Exception occurred: {}, error code: {}", message, errorCode);
        }
    }
}
