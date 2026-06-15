package com.inventory.common.core;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.inventory.common.util.DateTimeUtils;
import com.inventory.common.util.SecurityUtils;

/**
 * API响应统一格式.
 *
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> implements Serializable {

    /** 序列化版本号. */
    private static final long serialVersionUID = 1L;

    /** 成功状态码. */
    private static final int SUCCESS_CODE = 200;

    /** 成功消息. */
    private static final String SUCCESS_MESSAGE = "Success";

    /** API版本. */
    private static final String API_VERSION = "1.0";

    /** 最大错误码长度. */
    private static final int MAX_ERROR_CODE_LENGTH = 50;

    /** 最大版本长度. */
    private static final int MAX_VERSION_LENGTH = 50;

    /** 最大上下文大小. */
    private static final int MAX_CONTEXT_SIZE = 100;

    /** 最大消息长度. */
    private static final int MAX_MESSAGE_LENGTH = 1000;

    /** HTTP状态码. */
    private int code;

    /** 响应消息. */
    private String message;

    /** 业务错误码. */
    private String errorCode;

    /** 上下文信息. */
    private transient Map<String, Object> context;

    /** 响应数据. */
    private transient T data;

    /** 时间戳. */
    private Instant timestamp;

    /** 成功标志. */
    private boolean success;

    /** API版本. */
    private String version;

    /**
     * 默认构造函数.
     * 创建一个成功的响应，状态码200，消息为"Success".
     */
    public ApiResponse() {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MESSAGE;
        this.context = null;
        this.timestamp = Instant.now();
        this.success = true;
        this.version = API_VERSION;
    }

    /**
     * 带数据的构造函数.
     *
     * @param data 响应数据
     */
    public ApiResponse(final T data) {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MESSAGE;
        this.context = null;
        this.data = data;
        this.timestamp = Instant.now();
        this.success = true;
        this.version = API_VERSION;
    }

    /**
     * 带状态码和消息的构造函数.
     *
     * @param code HTTP状态码
     * @param message 响应消息
     */
    public ApiResponse(final int code, final String message) {
        this.code = code;
        this.message = message;
        this.context = null;
        this.timestamp = Instant.now();
        this.success = code == SUCCESS_CODE;
        this.version = API_VERSION;
    }

    /**
     * 带状态码消息和数据的构造函数.
     *
     * @param code HTTP状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResponse(final int code, final String message, final T data) {
        this.code = code;
        this.message = message;
        this.context = null;
        this.data = data;
        this.timestamp = Instant.now();
        this.success = code == SUCCESS_CODE;
        this.version = API_VERSION;
    }

    /**
     * 带状态码消息和错误码的构造函数.
     *
     * @param code HTTP状态码
     * @param message 响应消息
     * @param errorCode 业务错误码
     */
    public ApiResponse(final int code, final String message, final String errorCode) {
        this.code = code;
        this.message = message;
        this.errorCode = errorCode;
        this.context = null;
        this.timestamp = Instant.now();
        this.success = code == SUCCESS_CODE;
        this.version = API_VERSION;
    }

    /**
     * 带状态码消息错误码和上下文的构造函数.
     *
     * @param code HTTP状态码
     * @param message 响应消息
     * @param errorCode 业务错误码
     * @param context 上下文信息（会被复制）
     */
    public ApiResponse(final int code, final String message, final String errorCode,
                       final Map<String, Object> context) {
        this.code = code;
        this.message = message;
        this.errorCode = errorCode;
        if (context != null) {
            this.context = new HashMap<>(context);
        } else {
            this.context = null;
        }
        this.timestamp = Instant.now();
        this.success = code == SUCCESS_CODE;
        this.version = API_VERSION;
    }

    /**
     * 带状态码消息错误码上下文和数据的构造函数.
     *
     * @param code HTTP状态码
     * @param message 响应消息
     * @param errorCode 业务错误码
     * @param context 上下文信息（会被复制）
     * @param data 响应数据
     */
    public ApiResponse(final int code, final String message, final String errorCode,
                       final Map<String, Object> context, final T data) {
        this.code = code;
        this.message = message;
        this.errorCode = errorCode;
        if (context != null) {
            this.context = new HashMap<>(context);
        } else {
            this.context = null;
        }
        this.data = data;
        this.timestamp = Instant.now();
        this.success = code == SUCCESS_CODE;
        this.version = API_VERSION;
    }

    /**
     * 使用Builder构造函数.
     *
     * @param builder Builder实例
     */
    public ApiResponse(final Builder<T> builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.errorCode = builder.errorCode;
        this.context = builder.context != null && !builder.context.isEmpty()
                ? new HashMap<>(builder.context) : null;
        this.data = builder.data;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.success = builder.success != null ? builder.success : code == SUCCESS_CODE;
        this.version = builder.version != null ? builder.version : API_VERSION;
    }

    /**
     * 获取HTTP状态码.
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置HTTP状态码.
     *
     * @param newCode 状态码
     * @return ApiResponse实例，支持链式调用
     */
    public ApiResponse<T> setCode(final int newCode) {
        this.code = newCode;
        return this;
    }

    /**
     * 获取响应消息.
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息.
     *
     * @param newMessage 响应消息
     * @return ApiResponse实例，支持链式调用
     * @throws IllegalArgumentException 当消息长度超过1000个字符时抛出
     */
    public ApiResponse<T> setMessage(final String newMessage) {
        if (newMessage != null && newMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("消息长度不能超过" + MAX_MESSAGE_LENGTH + "个字符");
        }
        this.message = SecurityUtils.escapeHtml(newMessage);
        return this;
    }

    /**
     * 获取业务错误码.
     *
     * @return 错误码，可能为null
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 验证错误码格式是否符合规范.
     * 错误码格式应为：AAA-XX-XXX（例如：SYS-01-001）.
     *
     * @param errorCode 要验证的错误码
     * @return 验证结果，true表示格式正确，false表示格式错误
     */
    public static boolean isValidErrorCode(final String errorCode) {
        return errorCode == null || errorCode.matches("^[A-Z]{3}-\\d{2}-\\d{3}$");
    }

    /**
     * 设置业务错误码.
     *
     * @param newErrorCode 错误码
     * @return ApiResponse实例，支持链式调用
     * @throws IllegalArgumentException 如果错误码格式不符合规范或长度超过50个字符
     */
    public ApiResponse<T> setErrorCode(final String newErrorCode) {
        if (newErrorCode != null) {
            if (newErrorCode.length() > MAX_ERROR_CODE_LENGTH) {
                throw new IllegalArgumentException(
                        "错误码长度不能超过" + MAX_ERROR_CODE_LENGTH + "个字符");
            }
            if (!isValidErrorCode(newErrorCode)) {
                throw new IllegalArgumentException("错误码格式不符合规范，应为AAA-XX-XXX格式");
            }
        }
        this.errorCode = newErrorCode;
        return this;
    }

    /**
     * 获取上下文信息的副本.
     *
     * @return 上下文Map的副本，确保不可变
     */
    public Map<String, Object> getContext() {
        return context != null ? new HashMap<>(context) : new HashMap<>();
    }

    /**
     * 设置上下文信息.
     *
     * @param newContext 上下文信息（会被复制）
     * @return ApiResponse实例，支持链式调用
     * @throws IllegalArgumentException 当上下文大小超过100个键值对时抛出
     */
    public ApiResponse<T> setContext(final Map<String, Object> newContext) {
        if (newContext != null && newContext.size() > MAX_CONTEXT_SIZE) {
            throw new IllegalArgumentException(
                    "上下文大小不能超过" + MAX_CONTEXT_SIZE + "个键值对");
        }
        this.context = newContext != null && !newContext.isEmpty()
                ? new HashMap<>(newContext) : null;
        return this;
    }

    /**
     * 向上下文中添加键值对.
     *
     * @param key 键
     * @param value 值
     */
    public void putContext(final String key, final Object value) {
        if (this.context == null) {
            this.context = new HashMap<>();
        }
        this.context.put(key, value);
    }

    /**
     * 获取响应数据.
     *
     * @return 响应数据，可能为null
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据.
     *
     * @param newData 响应数据
     * @return ApiResponse实例，支持链式调用
     */
    public ApiResponse<T> setData(final T newData) {
        this.data = newData;
        return this;
    }

    /**
     * 获取响应时间戳.
     *
     * @return 时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * 设置响应时间戳.
     *
     * @param newTimestamp 时间戳
     * @return ApiResponse实例，支持链式调用
     */
    public ApiResponse<T> setTimestamp(final Instant newTimestamp) {
        this.timestamp = newTimestamp;
        return this;
    }

    /**
     * 获取响应是否成功.
     *
     * @return 成功标志
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置响应是否成功.
     *
     * @param newSuccess 成功标志
     * @return ApiResponse实例，支持链式调用
     */
    public ApiResponse<T> setSuccess(final boolean newSuccess) {
        this.success = newSuccess;
        return this;
    }

    /**
     * 获取API版本.
     *
     * @return API版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置API版本.
     *
     * @param newVersion API版本
     * @return ApiResponse实例，支持链式调用
     * @throws IllegalArgumentException 当版本长度超过50个字符时抛出
     */
    public ApiResponse<T> setVersion(final String newVersion) {
        if (newVersion != null && newVersion.length() > MAX_VERSION_LENGTH) {
            throw new IllegalArgumentException(
                    "版本长度不能超过" + MAX_VERSION_LENGTH + "个字符");
        }
        this.version = newVersion;
        return this;
    }

    /**
     * 创建一个成功的响应，状态码200，消息为"Success".
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>();
    }

    /**
     * 创建一个带数据的成功响应.
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(final T data) {
        return new ApiResponse<>(data);
    }

    /**
     * 创建一个带消息和数据的成功响应.
     *
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(final String message, final T data) {
        final ApiResponse<T> response = new ApiResponse<>(data);
        response.message = message;
        return response;
    }

    /**
     * 创建一个错误响应，状态码500.
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(final String message) {
        return new ApiResponse<>(500, message);
    }

    /**
     * 创建一个自定义状态码的错误响应.
     *
     * @param code HTTP状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(final int code, final String message) {
        return new ApiResponse<>(code, message);
    }

    /**
     * 创建一个带错误码的错误响应.
     *
     * @param message 错误消息
     * @param errorCode 业务错误码
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(final String message, final String errorCode) {
        return new ApiResponse<>(500, message, errorCode);
    }

    /**
     * 创建一个带状态码和错误码的错误响应.
     *
     * @param code HTTP状态码
     * @param message 错误消息
     * @param errorCode 业务错误码
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(final int code, final String message,
                                           final String errorCode) {
        return new ApiResponse<>(code, message, errorCode);
    }

    /**
     * 创建带错误码、消息和Map上下文的错误响应.
     *
     * @param errorCode 业务错误码
     * @param message 错误消息
     * @param context 上下文信息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(final String errorCode, final String message,
                                           final Map<String, Object> context) {
        return new ApiResponse<>(500, message, errorCode, context);
    }

    /**
      * 创建带错误码、消息和布尔值的错误响应.
      *
      * @param errorCode 业务错误码
      * @param message 错误消息
      * @param result 布尔结果
      * @param <T> 数据类型
      * @return 错误响应对象
      */
     public static <T> ApiResponse<T> error(final String errorCode, final String message,
                                            final boolean result) {
         Map<String, Object> context = new HashMap<>();
         context.put("result", result);
         return new ApiResponse<>(500, message, errorCode, context);
     }

     /**
      * 创建一个带数据的响应.
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> withData(final T data) {
        return new ApiResponse<>(data);
    }

    /**
     * Builder类，用于构建ApiResponse对象.
     * 支持链式调用，简化复杂响应的创建.
     *
     * @param <T> 响应数据类型
     */
    public static class Builder<T> {

        /** HTTP状态码. */
        private int code = SUCCESS_CODE;

        /** 响应消息. */
        private String message = SUCCESS_MESSAGE;

        /** 业务错误码. */
        private String errorCode;

        /** 上下文信息. */
        private Map<String, Object> context = null;

        /** 响应数据. */
        private T data;

        /** 时间戳. */
        private Instant timestamp;

        /** 成功标志. */
        private Boolean success;

        /** API版本. */
        private String version;

        /**
         * 设置HTTP状态码.
         *
         * @param newCode 状态码
         * @return Builder实例
         */
        public Builder<T> code(final int newCode) {
            this.code = newCode;
            return this;
        }

        /**
         * 设置响应消息.
         *
         * @param newMessage 响应消息
         * @return Builder实例
         */
        public Builder<T> message(final String newMessage) {
            this.message = escapeHtml(newMessage);
            return this;
        }

        /**
         * 对消息进行HTML转义处理.
         *
         * @param inputMessage 要转义的消息
         * @return 转义后的消息
         */
        private String escapeHtml(final String inputMessage) {
            if (inputMessage == null) {
                return null;
            }
            return inputMessage
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }

        /**
         * 设置业务错误码.
         *
         * @param newErrorCode 错误码
         * @return Builder实例
         * @throws IllegalArgumentException 如果错误码格式不符合规范
         */
        public Builder<T> errorCode(final String newErrorCode) {
            // 验证错误码格式
            if (newErrorCode != null && !ApiResponse.isValidErrorCode(newErrorCode)) {
                throw new IllegalArgumentException("错误码格式不符合规范，应为AAA-XX-XXX格式");
            }
            this.errorCode = newErrorCode;
            return this;
        }

        /**
         * 设置上下文信息.
         *
         * @param newContext 上下文信息（会被复制）
         * @return Builder实例
         */
        public Builder<T> context(final Map<String, Object> newContext) {
            this.context = newContext != null && !newContext.isEmpty()
                    ? new HashMap<>(newContext) : null;
            return this;
        }

        /**
         * 向上下文中添加键值对.
         *
         * @param key 键
         * @param value 值
         * @return Builder实例
         */
        public Builder<T> putContext(final String key, final Object value) {
            if (this.context == null) {
                this.context = new HashMap<>();
            }
            this.context.put(key, value);
            return this;
        }

        /**
         * 设置响应数据.
         *
         * @param newData 响应数据
         * @return Builder实例
         */
        public Builder<T> data(final T newData) {
            this.data = newData;
            return this;
        }

        /**
         * 设置响应时间戳.
         *
         * @param newTimestamp 时间戳
         * @return Builder实例
         */
        public Builder<T> timestamp(final Instant newTimestamp) {
            this.timestamp = newTimestamp;
            return this;
        }

        /**
         * 设置响应是否成功.
         *
         * @param newSuccess 成功标志
         * @return Builder实例
         */
        public Builder<T> success(final boolean newSuccess) {
            this.success = newSuccess;
            return this;
        }

        /**
         * 设置API版本.
         *
         * @param newVersion API版本
         * @return Builder实例
         */
        public Builder<T> version(final String newVersion) {
            this.version = newVersion;
            return this;
        }

        /**
         * 构建ApiResponse对象.
         *
         * @return ApiResponse对象
         */
        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApiResponse<?> that = (ApiResponse<?>) o;
        return isPrimitiveFieldsEqual(that) && isObjectFieldsEqual(that);
    }

    /**
     * 检查基本字段是否相等.
     *
     * @param that 要比较的ApiResponse对象
     * @return 如果基本字段相等则返回true
     */
    private boolean isPrimitiveFieldsEqual(final ApiResponse<?> that) {
        return code == that.code && success == that.success;
    }

    /**
     * 检查对象字段是否相等.
     *
     * @param that 要比较的ApiResponse对象
     * @return 如果所有对象字段相等则返回true
     */
    private boolean isObjectFieldsEqual(final ApiResponse<?> that) {
        return Objects.equals(message, that.message)
                && Objects.equals(errorCode, that.errorCode)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(version, that.version)
                && Objects.equals(context, that.context)
                && Objects.equals(data, that.data);
    }

    /**
     * 基于所有字段计算哈希码.
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, message, errorCode, context, data, timestamp, success, version);
    }

    /**
     * 将对象转换为字符串表示，便于调试和日志记录.
     *
     * @return 包含所有字段的字符串表示，敏感数据已脱敏
     */
    @Override
    public String toString() {
        return "ApiResponse{"
                + "code=" + code
                + ", message='" + (message != null ? message : "null") + "'"
                + ", errorCode='" + (errorCode != null ? errorCode : "null") + "'"
                + ", context=" + (context != null ? "[敏感数据已脱敏]" : "null")
                + ", data=" + (data != null ? SecurityUtils.maskSensitiveData(data) : "null")
                + ", timestamp=" + (timestamp != null ? DateTimeUtils.formatInstant(timestamp) : "null")
                + ", success=" + success
                + ", version='" + version + "'"
                + '}';
    }
}
