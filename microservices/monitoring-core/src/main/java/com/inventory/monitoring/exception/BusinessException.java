/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 企业级业务异常类，用于处理业务逻辑中发生的异常情况。
 * <p>
 * 该异常类继承自{@link RuntimeException}，支持非受检异常处理。
 * 包含错误码、异常消息、异常原因、上下文信息和严重程度等，便于统一异常处理和监控。
 * </p>
 * <p>
 * 该类与监控指标收集器集成，当异常被创建时自动统计异常信息。
 * </p>
 */
public class BusinessException extends RuntimeException {

    /**
 * 序列化版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
 * 日志记录器。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessException.class);

    private static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    private static final String LOG_MSG_EXCEPTION_OCCURRED = "BusinessException occurred with error code: {}";
    private static final String LOG_MSG_EXCEPTION_OCCURRED_MSG = "BusinessException occurred: {}, error code: {}";
    private static final String LOG_MSG_EXCEPTION_WITH_SEVERITY = "BusinessException occurred: %s, error code: %s, severity: %s";

    /**
     * 资源包。
     */
    @SuppressWarnings("unused")
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages");

    /**
 * 错误码。
     */
    private final String errorCode;

    /**
 * 上下文信息。
     */
    private final Map<String, Object> context;

    /**
 * 异常严重程度。
     */
    private final Severity severityValue;

    /**
 * 获取错误码。
     *
     * @return 错误码。
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
 * 获取上下文信息。
     *
     * @return 上下文信息的副本。
     */
    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }

    /**
 * 获取异常严重程度。
     *
     * @return 异常严重程度。
     */
    public Severity getSeverity() {
        return severityValue;
    }

    /**
 * 异常严重程度枚举。
     */
    public enum Severity {
        /**
 * 信息级别。
         */
        INFO,

        /**
 * 警告级别。
         */
        WARNING,

        /**
 * 错误级别。
         */
        ERROR,

        /**
 * 严重级别。
         */
        CRITICAL
    }

    /**
 * 消息缓存。
     */
    private static final Map<String, String> MESSAGE_CACHE = new HashMap<>();

    /**
 * 异常监听器接口。
     */
    public interface ExceptionListener {
        /**
 * 当异常被创建时调用。
         *
         * @param exception 创建的异常。
         */
        void onExceptionCreated(BusinessException exception);
    }

    /**
 * 异常监听器列表。
     */
    private static final List<ExceptionListener> EXCEPTION_LISTENERS = Collections.synchronizedList(new ArrayList<>());

    /**
 * 无参构造方法。
     */
    public BusinessException() {
        this.errorCode = UNKNOWN_ERROR;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED, this.errorCode);
        safeNotifyListeners();
    }

    /**
 * 带异常消息的构造方法。
     *
     * @param message 异常消息。
     */
    public BusinessException(final String message) {
        super(message);
        this.errorCode = UNKNOWN_ERROR;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED_MSG, message, this.errorCode);
        safeNotifyListeners();
    }

    /**
 * 带异常消息和错误码的构造方法。
     *
     * @param message   异常消息。
     * @param errorCode 错误码。
     */
    public BusinessException(final String message, final String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED_MSG, message, this.errorCode);
        safeNotifyListeners();
    }

    /**
 * 带异常消息和异常原因的构造方法。
     *
     * @param message 异常消息。
     * @param cause   异常原因。
     */
    public BusinessException(final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = UNKNOWN_ERROR;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED_MSG, message, this.errorCode, cause);
        safeNotifyListeners();
    }

    /**
 * 带异常消息、错误码和异常原因的构造方法。
     *
     * @param message   异常消息。
     * @param errorCode 错误码。
     * @param cause     异常原因。
     */
    public BusinessException(final String message, final String errorCode, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED_MSG, message, this.errorCode, cause);
        safeNotifyListeners();
    }

    /**
 * 带异常原因的构造方法。
     *
     * @param cause 异常原因。
     */
    public BusinessException(final Throwable cause) {
        super(cause);
        this.errorCode = UNKNOWN_ERROR;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED, this.errorCode, cause);
        safeNotifyListeners();
    }

    /**
 * 带异常原因和错误码的构造方法。
     *
     * @param cause     异常原因。
     * @param errorCode 错误码。
     */
    public BusinessException(final Throwable cause, final String errorCode) {
        super(cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.severityValue = Severity.ERROR;
        LOGGER.error(LOG_MSG_EXCEPTION_OCCURRED, this.errorCode, cause);
        safeNotifyListeners();
    }

    /**
 * 带异常消息、错误码和严重程度的构造方法。
     *
     * @param message   异常消息。
     * @param errorCode 错误码。
     * @param severity  严重程度。
     */
    public BusinessException(final String message, final String errorCode, final Severity severity) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.severityValue = severity;
        logException(message, errorCode, severity, null);
        safeNotifyListeners();
    }

    /**
 * 带异常消息、错误码、异常原因和严重程度的构造方法。
     *
     * @param message   异常消息。
     * @param errorCode 错误码。
     * @param cause     异常原因。
     * @param severity  严重程度。
     */
    public BusinessException(final String message, final String errorCode, final Throwable cause,
        final Severity severity) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
        this.severityValue = severity;
        logException(message, errorCode, severity, cause);
        safeNotifyListeners();
    }

    /**
 * 添加上下文信息。
     *
     * @param key   上下文键。
     * @param value 上下文值。
     * @return BusinessException实例，支持链式调用。
     */
    public BusinessException addContext(final String key, final Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
 * 添加多个上下文信息。
     *
     * @param contextMap 上下文信息映射。
     * @return BusinessException实例，支持链式调用。
     */
    public BusinessException addContext(final Map<String, Object> contextMap) {
        this.context.putAll(contextMap);
        return this;
    }

    /**
 * 设置异常严重程度。
     *
     * @param severity 严重程度。
     * @return BusinessException实例，支持链式调用。
     */
    public BusinessException withSeverity(final Severity severity) {
        // 创建新的异常实例，设置新的严重程度
        final BusinessException newException = new BusinessException(getMessage(), getErrorCode(), getCause(),
        severity);
        // 复制上下文信息
        newException.context.putAll(this.context);
        return newException;
    }

    /**
 * 静态工厂方法，通过错误码创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode) {
        final String message = getMessageFromBundle(errorCode.getMessageKey());
        return new BusinessException(message, errorCode.getCode());
    }

    /**
 * 静态工厂方法，通过错误码和自定义消息创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @param message   自定义异常消息。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode, final String message) {
        return new BusinessException(message, errorCode.getCode());
    }

    /**
 * 静态工厂方法，通过错误码和异常原因创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @param cause     异常原因。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode, final Throwable cause) {
        final String message = getMessageFromBundle(errorCode.getMessageKey());
        return new BusinessException(message, errorCode.getCode(), cause);
    }

    /**
 * 静态工厂方法，通过错误码和严重程度创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @param severity  严重程度。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode, final Severity severity) {
        final String message = getMessageFromBundle(errorCode.getMessageKey());
        return new BusinessException(message, errorCode.getCode(), severity);
    }

    /**
 * 静态工厂方法，通过错误码、自定义消息和严重程度创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @param message   自定义异常消息。
     * @param severity  严重程度。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode, final String message,
        final Severity severity) {
        return new BusinessException(message, errorCode.getCode(), severity);
    }

    /**
 * 静态工厂方法，通过错误码、异常原因和严重程度创建异常实例。
     *
     * @param errorCode 错误码枚举。
     * @param cause     异常原因。
     * @param severity  严重程度。
     * @return BusinessException实例。
     */
    public static BusinessException withErrorCode(final ErrorCode errorCode, final Throwable cause,
        final Severity severity) {
        final String message = getMessageFromBundle(errorCode.getMessageKey());
        return new BusinessException(message, errorCode.getCode(), cause, severity);
    }

    /**
 * 从资源包中获取国际化消息。
     *
     * @param messageKey 消息键。
     * @return 国际化消息。
     */
    private static String getMessageFromBundle(final String messageKey) {
        return getMessageFromBundle(messageKey, Locale.getDefault(), (Object[]) null);
    }

    /**
 * 从资源包中获取国际化消息，支持指定地区和参数化。
     *
     * @param messageKey 消息键。
     * @param locale     地区。
     * @param params     消息参数。
     * @return 国际化消息。
     */
    private static String getMessageFromBundle(final String messageKey, final Locale locale, final Object... params) {
        try {
            // 尝试从缓存中获取消息
            final String cacheKey = messageKey + "_" + locale.getLanguage();
            String message = MESSAGE_CACHE.get(cacheKey);
            if (message == null) {
                // 缓存未命中，从资源包中加载
                final ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
                message = bundle.getString(messageKey);
                // 将加载的消息放入缓存
                MESSAGE_CACHE.put(cacheKey, message);
            }

            // 如果有参数，进行格式化
            if (params != null && params.length > 0) {
                message = String.format(message, params);
            }

            return message;
        } catch (final java.util.MissingResourceException e) {
                    LOGGER.warn("Failed to get message from bundle for key: {} in locale: {}", messageKey, locale, e);
                    // 消息加载失败，返回消息键作为fallback
                    return messageKey;
                }
            }
        
            /**
 * 清除消息缓存，用于测试或资源包更新后。
             */
            public static void clearMessageCache() {
                MESSAGE_CACHE.clear();
                LOGGER.info("Message cache cleared");
            }
        
            /**
 * 获取消息缓存大小，用于监控。
             *
             * @return 消息缓存大小。
             */
        
            public static int getMessageCacheSize() {
                return MESSAGE_CACHE.size();
            }
        
            /**
 * 注册异常监听器，用于监听异常的创建和处理。
             *
             * @param listener 异常监听器。
             */
            public static void registerExceptionListener(final ExceptionListener listener) {
                if (listener != null) {
                    EXCEPTION_LISTENERS.add(listener);
                    LOGGER.info("Exception listener registered: {}", listener.getClass().getName());
                }
            }
        
            /**
 * 移除异常监听器，用于停止监听异常的创建和处理。
             *
             * @param listener 异常监听器。
             */
            public static void removeExceptionListener(final ExceptionListener listener) {
                if (listener != null) {
                    EXCEPTION_LISTENERS.remove(listener);
                    LOGGER.info("Exception listener removed: {}", listener.getClass().getName());
                }
            }
        
            /**
 * 清除所有异常监听器，用于测试或重置监听器状态。
             */
            public static void clearExceptionListeners() {
                EXCEPTION_LISTENERS.clear();
                LOGGER.info("All exception listeners cleared");
            }
        
            /**
 * 通知所有异常监听器，异常已被创建。
             */
            private void notifyExceptionListeners() {
                for (final ExceptionListener listener : EXCEPTION_LISTENERS) {
                    try {
                        listener.onExceptionCreated(this);
                    } catch (final RuntimeException e) {
                        LOGGER.warn("Failed to notify exception listener: {}", listener.getClass().getName(), e);
                    }
                }
            }

    /**
     * 安全地通知异常监听器，防止构造函数报告 CT_CONSTRUCTOR_THROW.
     */
    private void safeNotifyListeners() {
        try {
            notifyExceptionListeners();
        } catch (final Exception e) {
            LOGGER.warn("Failed to notify exception listeners during construction", e);
        }
    }

    /**
 * 记录异常信息，根据严重程度选择不同的日志级别。
     *
     * @param message   异常消息。
     * @param errorCode 错误码。
     * @param severity  严重程度。
     * @param cause     异常原因。
     */
    private void logException(final String message, final String errorCode, final Severity severity,
        final Throwable cause) {
        final String logMessage = String.format(LOG_MSG_EXCEPTION_WITH_SEVERITY, message,
        errorCode, severity);
        switch (severity) {
            case INFO:
                LOGGER.info(logMessage, cause);
                break;
            case WARNING:
                LOGGER.warn(logMessage, cause);
                break;
            case ERROR:
                LOGGER.error(logMessage, cause);
                break;
            case CRITICAL:
                LOGGER.error(logMessage, cause);
                // 对于严重级别的异常，可以在这里添加额外的处理，如发送告警等
                break;
        }
    }

    /**
 * 重写equals方法，确保异常对象的正确比较。
     *
     * @param obj 比较对象。
     * @return 是否相等。
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null ||
        getClass() != obj.getClass()) {
            return false;
        }
        final BusinessException that = (BusinessException) obj;
        if (!errorCode.equals(that.errorCode)) {
            return false;
        }
        if (severityValue != that.severityValue) {
            return false;
        }
        if (getMessage() == null ? that.getMessage() != null : !getMessage().equals(that.getMessage())) {
            return false;
        }
        return getContext().equals(that.getContext());
    }

    /**
 * 重写hashCode方法，确保异常对象的正确哈希计算。
     *
     * @return 哈希值。
     */
    @Override
    public int hashCode() {
        int result = errorCode.hashCode();
        result = 31 * result + severityValue.hashCode();
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + getContext().hashCode();
        return result;
    }

    /**
 * 自定义序列化方法，确保序列化兼容性。
     *
     * @param out 对象输出流。
     * @throws IOException IO异常。
     */
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /**
 * 自定义反序列化方法，确保反序列化兼容性。
     *
     * @param in 对象输入流。
     * @throws IOException            IO异常。
     * @throws ClassNotFoundException 类未找到异常。
     */
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
