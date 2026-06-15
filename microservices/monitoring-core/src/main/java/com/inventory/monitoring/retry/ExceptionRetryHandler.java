/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.retry;

import com.inventory.monitoring.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 异常重试处理器，用于处理需要重试的业务操作。
 * <p>
 * 该类提供了异常重试的机制，支持配置重试次数、重试间隔和重试条件。
 * 同时提供了异常分类统计功能，便于监控和分析异常情况。
 * </p>
 */
public class ExceptionRetryHandler {

    /**
 * 日志记录器，用于记录重试操作的执行状态。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRetryHandler.class);

    /**
 * 默认重试次数。
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
 * 默认重试间隔（毫秒）。
     */
    private static final long DEFAULT_RETRY_INTERVAL_MS = 1000;

    /**
 * 最大重试次数。
     */
    private final int maxRetries;

    /**
 * 重试间隔（毫秒）。
     */
    private final long retryIntervalMs;

    /**
 * 重试策略，用于判断是否需要重试。
     */
    private final RetryStrategy retryStrategy;

    /**
 * 构造方法，使用默认的重试次数和间隔。
     */
    public ExceptionRetryHandler() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL_MS, new DefaultRetryStrategy());
    }

    /**
 * 构造方法，使用指定的重试次数和间隔。
     *
     * @param maxRetries      最大重试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     */
    public ExceptionRetryHandler(final int maxRetries, final long retryIntervalMs) {
        this(maxRetries, retryIntervalMs, new DefaultRetryStrategy());
    }

    /**
 * 构造方法，使用指定的重试次数、间隔和重试策略。
     *
     * @param maxRetries      最大重试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     * @param retryStrategy   重试策略
     */
    public ExceptionRetryHandler(final int maxRetries, final long retryIntervalMs, final RetryStrategy retryStrategy) {
        this.maxRetries = maxRetries;
        this.retryIntervalMs = retryIntervalMs;
        this.retryStrategy = retryStrategy;
    }

    /**
 * 执行带重试的操作，返回操作结果。
 * <p>
 * 当发生重试时，自动统计异常重试次数和分类。
 * </p>
     *
     * @param <T>      操作结果类型
     * @param callable 要执行的操作
     * @return 操作结果
     * @throws Exception 如果操作在最大重试次数后仍然失败
     */
    public <T> T executeWithRetry(final Callable<T> callable) throws Exception {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                if (retryCount > 0) {
                    LOGGER.info("Retrying operation (attempt {}/{})...", retryCount, maxRetries);
                    // 等待重试间隔
                    TimeUnit.MILLISECONDS.sleep(retryIntervalMs);
                }

                // 执行操作
                return callable.call();
            } catch (final RuntimeException e) {
                lastException = e;
                retryCount++;

                // 统计异常重试次数
                recordRetryAttempt(e);

                // 判断是否需要重试
                if (!retryStrategy.shouldRetry(e, retryCount, maxRetries)) {
                    LOGGER.error("Operation failed and no more retries allowed: {}", e.getMessage(), e);
                    throw e;
                }

                LOGGER.warn("Operation failed, will retry: {}", e.getMessage());
            }
        }

        // 所有重试都失败，抛出最后一个异常
        LOGGER.error("All retry attempts failed: {}", lastException.getMessage(), lastException);
        throw lastException;
    }

    /**
 * 执行带重试的操作，无返回值。
 * <p>
 * 当发生重试时，自动统计异常重试次数和分类。
 * </p>
     *
     * @param runnable 要执行的操作
     * @throws Exception 如果操作在最大重试次数后仍然失败
     */
    public void executeWithRetry(final Runnable runnable) throws Exception {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        });
    }

    /**
 * 记录异常重试尝试，自动统计异常重试次数和分类。
 * <p>
 * 使用反射机制获取MonitoringMetricsCollector实例，避免循环依赖。
 * </p>
     *
     * @param exception 发生的异常
     */
    private void recordRetryAttempt(final Exception exception) {
        try {
            // 获取异常类型
            final String exceptionType = getExceptionType(exception);

            // 尝试从Spring上下文获取MonitoringMetricsCollector实例
            final Object applicationContext = getApplicationContext();
            if (applicationContext != null) {
                // 使用反射获取MonitoringMetricsCollector实例
                final var getBeanMethod = applicationContext.getClass().getMethod("getBean", String.class);
                final Object metricsCollector = getBeanMethod.invoke(applicationContext, "monitoringMetricsCollector");
                if (metricsCollector != null) {
                    // 使用反射调用incrementRetryCount方法
                    final var incrementRetryCountMethod = metricsCollector.getClass().getMethod("incrementRetryCount",
        String.class);
                    incrementRetryCountMethod.invoke(metricsCollector, exceptionType);
                }
            }
        } catch (final RuntimeException | ReflectiveOperationException e) {
            // 统计失败时不影响重试操作的正常执行
            LOGGER.debug("Failed to record retry attempt: {}", e.getMessage());
        }
    }

    /**
 * 获取异常类型，用于统计异常重试次数。
 * <p>
 * 对于BusinessException，返回其错误码；对于其他异常，返回其类名。
 * </p>
     *
     * @param exception 发生的异常
     * @return 异常类型
     */
    private String getExceptionType(final Exception exception) {
        if (exception instanceof BusinessException) {
            return ((BusinessException) exception).getErrorCode();
        } else {
            return exception.getClass().getSimpleName();
        }
    }

    /**
 * 获取Spring应用上下文，用于获取MonitoringMetricsCollector实例。
     *
     * @return Spring应用上下文
     */
    private Object getApplicationContext() {
        try {
            // 使用反射获取Spring应用上下文
            final Class<?> contextHolderClass = Class.forName(
                    "org.springframework.web.context.request.RequestContextHolder");
            final var method = contextHolderClass.getMethod("getRequestAttributes");
            final Object attributes = method.invoke(null);
            if (attributes != null) {
                final Class<?> webAttributesClass = Class.forName(
                        "org.springframework.web.context.request.WebRequestAttributes");
                final var getServletContextMethod = webAttributesClass.getMethod("getServletContext");
                final Object servletContext = getServletContextMethod.invoke(attributes);
                final Class<?> servletContextClass = Class.forName("javax.servlet.ServletContext");
                final var getAttributeMethod = servletContextClass.getMethod("getAttribute", String.class);
                return getAttributeMethod.invoke(servletContext,
                        "org.springframework.web.context.WebApplicationContext.ROOT");
            }
        } catch (final ReflectiveOperationException e) {
            // 获取失败时返回null
            LOGGER.debug("Failed to get application context: {}", e.getMessage());
        }
        return null;
    }

    /**
 * 重试策略接口，用于判断是否需要重试。
     */
    public interface RetryStrategy {
        /**
 * 判断是否需要重试。
         *
         * @param exception  发生的异常
         * @param retryCount 当前重试次数
         * @param maxRetries 最大重试次数
         * @return 是否需要重试
         */
        boolean shouldRetry(Exception exception, int retryCount, int maxRetries);
    }

    /**
 * 默认重试策略，对网络异常、数据库异常和服务不可用异常进行重试。
     */
    public static class DefaultRetryStrategy implements RetryStrategy {

        /**
 * 判断是否需要重试。
         *
         * @param exception  发生的异常
         * @param retryCount 当前重试次数
         * @param maxRetries 最大重试次数
         * @return 是否需要重试
         */
        @Override
        public boolean shouldRetry(final Exception exception, final int retryCount, final int maxRetries) {
            if (retryCount > maxRetries) {
                return false;
            }

            if (exception instanceof BusinessException) {
                return shouldRetryBusinessException((BusinessException) exception);
            }

            return shouldRetrySystemException(exception);
        }

        /**
 * 判断业务异常是否需要重试。
         *
         * @param exception 业务异常
         * @return 是否需要重试
         */
        private boolean shouldRetryBusinessException(final BusinessException exception) {
            final String errorCode = exception.getErrorCode();
            return isRetryableErrorCode(errorCode);
        }

        /**
 * 判断错误码是否可重试。
         *
         * @param errorCode 错误码
         * @return 是否可重试
         */
        private boolean isRetryableErrorCode(final String errorCode) {
            switch (errorCode) {
                case "NETWORK_ERROR":
                case "DATABASE_ERROR":
                case "SERVICE_UNAVAILABLE":
                case "TIMEOUT_ERROR":
                    return true;
                default:
                    return false;
            }
        }

        /**
 * 判断系统异常是否需要重试。
         *
         * @param exception 系统异常
         * @return 是否需要重试
         */
        private boolean shouldRetrySystemException(final Exception exception) {
            final String exceptionName = exception.getClass().getName();
            return containsRetryableKeyword(exceptionName);
        }

        /**
 * 判断异常名称是否包含可重试关键词。
         *
         * @param exceptionName 异常名称
         * @return 是否包含可重试关键词
         */
        private boolean containsRetryableKeyword(final String exceptionName) {
            return exceptionName.contains("NetworkException") ||
        exceptionName.contains("SQLException") ||
        exceptionName.contains("TimeoutException") ||
        exceptionName.contains("ConnectException") ||
        exceptionName.contains("SocketException");
        }
    }

    /**
 * 总是重试策略，对所有异常都进行重试。
     */
    public static class AlwaysRetryStrategy implements RetryStrategy {

        /**
 * 判断是否需要重试。
         *
         * @param exception  发生的异常
         * @param retryCount 当前重试次数
         * @param maxRetries 最大重试次数
         * @return 是否需要重试
         */
        @Override
        public boolean shouldRetry(final Exception exception, final int retryCount, final int maxRetries) {
            return retryCount <= maxRetries;
        }
    }

    /**
 * 从不重试策略，对所有异常都不进行重试。
     */
    public static class NeverRetryStrategy implements RetryStrategy {

        /**
 * 判断是否需要重试。
         *
         * @param exception  发生的异常
         * @param retryCount 当前重试次数
         * @param maxRetries 最大重试次数
         * @return 是否需要重试
         */
        @Override
        public boolean shouldRetry(final Exception exception, final int retryCount, final int maxRetries) {
            return false;
        }
    }
}
