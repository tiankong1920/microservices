package com.inventory.common.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 异常上下文类，用于封装异常相关的上下文信息.
 * 包含请求参数、用户ID、时间戳、业务ID等关键信息.
 */
public class ExceptionContext {

    /** 请求参数. */
    private String requestParams;

    /** 用户ID. */
    private String userId;

    /** 时间戳. */
    private long timestamp;

    /** 业务ID. */
    private String businessId;

    /** 用户友好消息. */
    private String userFriendlyMessage;

    /** 技术细节. */
    private String technicalDetails;

    /** 额外上下文信息. */
    private final Map<String, Object> additionalContext;

    /**
     * 私有构造函数，只能通过Builder创建.
     */
    private ExceptionContext() {
        this.additionalContext = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取请求参数.
     *
     * @return 请求参数
     */
    public String getRequestParams() {
        return requestParams;
    }

    /**
     * 获取用户ID.
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取时间戳.
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取业务ID.
     *
     * @return 业务ID
     */
    public String getBusinessId() {
        return businessId;
    }

    /**
     * 获取用户友好消息.
     *
     * @return 用户友好消息
     */
    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    /**
     * 获取技术细节.
     *
     * @return 技术细节
     */
    public String getTechnicalDetails() {
        return technicalDetails;
    }

    /**
     * 获取所有上下文信息.
     *
     * @return 包含所有上下文信息的Map
     */
    public Map<String, Object> getAllContext() {
        final Map<String, Object> allContext = new HashMap<>(additionalContext);
        allContext.put("requestParams", requestParams);
        allContext.put("userId", userId);
        allContext.put("timestamp", timestamp);
        allContext.put("businessId", businessId);
        allContext.put("userFriendlyMessage", userFriendlyMessage);
        allContext.put("technicalDetails", technicalDetails);
        return allContext;
    }

    /**
     * 获取额外上下文信息.
     *
     * @return 额外上下文信息
     */
    public Map<String, Object> getAdditionalContext() {
        return new HashMap<>(additionalContext);
    }

    /**
     * 获取指定键的额外上下文值.
     *
     * @param key 键
     * @return 值
     */
    public Object getAdditionalContext(final String key) {
        return additionalContext.get(key);
    }

    /**
     * 检查是否包含指定键的额外上下文.
     *
     * @param key 键
     * @return 是否包含
     */
    public boolean hasAdditionalContext(final String key) {
        return additionalContext.containsKey(key);
    }

    /**
     * 创建Builder实例.
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器类，用于创建ExceptionContext对象.
     */
    public static class Builder {

        /** 异常上下文实例. */
        private final ExceptionContext context;

        /**
         * 构造器.
         */
        public Builder() {
            this.context = new ExceptionContext();
        }

        /**
         * 设置请求参数.
         *
         * @param requestParams 请求参数
         * @return 构建器
         */
        public Builder setRequestParams(final String requestParams) {
            context.requestParams = requestParams;
            return this;
        }

        /**
         * 设置用户ID.
         *
         * @param userId 用户ID
         * @return 构建器
         */
        public Builder setUserId(final String userId) {
            context.userId = userId;
            return this;
        }

        /**
         * 设置时间戳.
         *
         * @param timestamp 时间戳
         * @return 构建器
         */
        public Builder setTimestamp(final long timestamp) {
            context.timestamp = timestamp;
            return this;
        }

        /**
         * 设置业务ID.
         *
         * @param businessId 业务ID
         * @return 构建器
         */
        public Builder setBusinessId(final String businessId) {
            context.businessId = businessId;
            return this;
        }

        /**
         * 设置用户友好消息.
         *
         * @param userFriendlyMessage 用户友好消息
         * @return 构建器
         */
        public Builder setUserFriendlyMessage(final String userFriendlyMessage) {
            context.userFriendlyMessage = userFriendlyMessage;
            return this;
        }

        /**
         * 设置技术细节.
         *
         * @param technicalDetails 技术细节
         * @return 构建器
         */
        public Builder setTechnicalDetails(final String technicalDetails) {
            context.technicalDetails = technicalDetails;
            return this;
        }

        /**
         * 添加额外上下文信息.
         *
         * @param key 键
         * @param value 值
         * @return 构建器
         */
        public Builder addAdditionalContext(final String key, final Object value) {
            context.additionalContext.put(key, value);
            return this;
        }

        /**
         * 添加多个额外上下文信息.
         *
         * @param additionalContext 额外上下文信息
         * @return 构建器
         */
        public Builder addAllAdditionalContext(final Map<String, Object> additionalContext) {
            context.additionalContext.putAll(additionalContext);
            return this;
        }

        /**
         * 构建ExceptionContext对象.
         *
         * @return ExceptionContext对象
         */
        public ExceptionContext build() {
            return context;
        }
    }
}
