package com.inventory.common.core;

/**
 * Common constants for Common module.
 * Contains frequently used magic numbers and constant values.
 */
public final class CommonConstants {

    // HTTP Status Codes

    /** HTTP状态码-成功. */
    public static final int HTTP_STATUS_OK = 200;

    /** HTTP状态码-已创建. */
    public static final int HTTP_STATUS_CREATED = 201;

    /** HTTP状态码-错误请求. */
    public static final int HTTP_STATUS_BAD_REQUEST = 400;

    /** HTTP状态码-未授权. */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;

    /** HTTP状态码-禁止访问. */
    public static final int HTTP_STATUS_FORBIDDEN = 403;

    /** HTTP状态码-未找到. */
    public static final int HTTP_STATUS_NOT_FOUND = 404;

    /** HTTP状态码-服务器内部错误. */
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    // Error Code Prefixes

    /** 系统错误前缀. */
    public static final String ERROR_CODE_PREFIX_SYSTEM = "SYS-01-";

    /** 参数错误前缀. */
    public static final String ERROR_CODE_PREFIX_PARAMETER = "PAR-02-";

    /** 业务错误前缀. */
    public static final String ERROR_CODE_PREFIX_BUSINESS = "BUS-03-";

    /** 认证错误前缀. */
    public static final String ERROR_CODE_PREFIX_AUTH = "AUTH-05-";

    /** 资源错误前缀. */
    public static final String ERROR_CODE_PREFIX_RESOURCE = "NOT-04-";

    // Error Code Types

    /** 系统错误类型. */
    public static final int ERROR_TYPE_SYSTEM = 1;

    /** 参数错误类型. */
    public static final int ERROR_TYPE_PARAMETER = 2;

    /** 业务错误类型. */
    public static final int ERROR_TYPE_BUSINESS = 3;

    /** 认证错误类型. */
    public static final int ERROR_TYPE_AUTH = 5;

    /** 资源错误类型. */
    public static final int ERROR_TYPE_RESOURCE = 4;

    // Error Code Sequence

    /** 错误码序列起始值. */
    public static final int ERROR_CODE_SEQUENCE_START = 1;

    /** 错误码序列结束值. */
    public static final int ERROR_CODE_SEQUENCE_END = 999;

    // Error Code Format

    /** 错误码模块前缀长度. */
    public static final int ERROR_CODE_MODULE_PREFIX_LENGTH = 4;

    /** 错误码类型长度. */
    public static final int ERROR_CODE_TYPE_LENGTH = 2;

    /** 错误码序列长度. */
    public static final int ERROR_CODE_SEQUENCE_LENGTH = 3;

    // String Lengths

    /** 最大错误码长度. */
    public static final int MAX_ERROR_CODE_LENGTH = 10;

    /** 最大错误描述长度. */
    public static final int MAX_ERROR_DESCRIPTION_LENGTH = 200;

    // Collection Sizes

    /** 默认初始容量. */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    /** 最大数组大小. */
    public static final int MAX_ARRAY_SIZE = 100;

    // Time Values

    /** 默认超时时间(毫秒). */
    public static final long DEFAULT_TIMEOUT_MS = 1000;

    /** 短超时时间(毫秒). */
    public static final long SHORT_TIMEOUT_MS = 500;

    /** 长超时时间(毫秒). */
    public static final long LONG_TIMEOUT_MS = 5000;

    // Retry Values

    /** 最大重试次数. */
    public static final int MAX_RETRY_ATTEMPTS = 3;

    /** 重试延迟(毫秒). */
    public static final long RETRY_DELAY_MS = 1000;

    // Buffer Sizes

    /** 默认缓冲区大小. */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /** 缓冲区大小512. */
    public static final int BUFFER_SIZE_512 = 512;

    /** 缓冲区大小1024. */
    public static final int BUFFER_SIZE_1024 = 1024;

    /** 缓冲区大小2048. */
    public static final int BUFFER_SIZE_2048 = 2048;

    /** 缓冲区大小4096. */
    public static final int BUFFER_SIZE_4096 = 4096;

    /** 缓冲区大小8192. */
    public static final int BUFFER_SIZE_8192 = 8192;

    // Thread Pool Sizes

    /** 核心线程池大小. */
    public static final int CORE_POOL_SIZE = 4;

    /** 最大线程池大小. */
    public static final int MAX_POOL_SIZE = 20;

    // Monitoring Values

    /** 健康检查间隔(秒). */
    public static final int HEALTH_CHECK_INTERVAL_SECONDS = 30;

    /** 健康检查超时(秒). */
    public static final int HEALTH_CHECK_TIMEOUT_SECONDS = 60;

    /** 健康检查启动周期(秒). */
    public static final int HEALTH_CHECK_START_PERIOD_SECONDS = 10;

    /** 健康检查失败阈值. */
    public static final int HEALTH_CHECK_FAILURE_THRESHOLD = 5;

    // Memory Values

    /** 堆内存512MB. */
    public static final int HEAP_SIZE_512_MB = 512;

    /** 堆内存1024MB. */
    public static final int HEAP_SIZE_1024_MB = 1024;

    // Percentage Values

    /** 百分比50. */
    public static final int PERCENTAGE_50 = 50;

    /** 百分比80. */
    public static final int PERCENTAGE_80 = 80;

    /** 百分比100. */
    public static final int PERCENTAGE_100 = 100;

    // Monitoring Specific Values

    /** 默认最大缓存大小. */
    public static final int DEFAULT_MAX_CACHE_SIZE = 10000;

    /** 默认最大批处理大小. */
    public static final int DEFAULT_MAX_BATCH_SIZE = 100;

    /** 默认最大存活时间(分钟). */
    public static final int DEFAULT_MAX_AGE_MINUTES = 5;

    /** 每KB字节数. */
    public static final int BYTES_PER_KB = 1024;

    /** 每MB的KB数. */
    public static final int KB_PER_MB = 1024;

    // Kafka Specific Values

    /** Kafka默认端口. */
    public static final int KAFKA_DEFAULT_PORT = 9092;

    /** Kafka默认重试次数. */
    public static final int KAFKA_DEFAULT_RETRIES = 3;

    /** Kafka默认批大小. */
    public static final int KAFKA_DEFAULT_BATCH_SIZE = 16384;

    /** Kafka默认延迟(毫秒). */
    public static final int KAFKA_DEFAULT_LINGER_MS = 10;

    /** Kafka默认缓冲内存. */
    public static final long KAFKA_DEFAULT_BUFFER_MEMORY = 33554432L;

    /** Kafka最大请求数. */
    public static final int KAFKA_MAX_IN_FLIGHT_REQUESTS = 5;

    /** Kafka请求超时(毫秒). */
    public static final int KAFKA_REQUEST_TIMEOUT_MS = 30000;

    // Order Processing Specific Values

    /** 订单处理窗口(分钟). */
    public static final long RATE_CALCULATION_WINDOW_MINUTES = 5;

    /** 最小健康速率. */
    public static final double MIN_HEALTHY_RATE = 10.0;

    /** 警告速率. */
    public static final double WARNING_RATE = 5.0;

    /** 最大队列大小. */
    public static final long MAX_QUEUE_SIZE = 1000;

    /** 调度速率(毫秒). */
    public static final long SCHEDULED_RATE_MS = 60000;

    // Health Check Specific Values

    /** 健康检查间隔值(秒). */
    public static final int HEALTH_CHECK_INTERVAL_SECONDS_VALUE = 30;

    // Tracing Specific Values

    /** 追踪休眠时间(毫秒). */
    public static final long TRACE_SLEEP_MS = 100;

    /** 追踪子休眠时间(毫秒). */
    public static final long TRACE_CHILD_SLEEP_MS = 50;

    /** 慢追踪阈值(毫秒). */
    public static final long SLOW_TRACE_THRESHOLD_MS = 1000;

    /** 错误状态码. */
    public static final int ERROR_STATUS_CODE = 500;

    /** 追踪批大小. */
    public static final int TRACE_BATCH_SIZE = 100;

    /** 追踪批超时(毫秒). */
    public static final long TRACE_BATCH_TIMEOUT_MS = 5000;

    /** 追踪导出队列大小. */
    public static final int TRACE_EXPORT_QUEUE_SIZE = 10000;

    /**
     * 私有构造函数，防止实例化.
     */
    private CommonConstants() {
        // Utility class - prevent instantiation
    }
}
