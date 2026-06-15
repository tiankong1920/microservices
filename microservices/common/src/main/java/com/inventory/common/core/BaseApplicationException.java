package com.inventory.common.core;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

/**
 * 应用异常基类.
 *
 * <p>统一的应用异常基类，用于规范化异常处理。
 * 所有自定义业务异常应继承此类以确保一致的错误码和HTTP状态映射。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>统一异常格式：包含错误码、HTTP状态、错误消息、上下文信息、时间戳</li>
 *   <li>支持上下文信息：记录异常发生时的上下文数据</li>
 *   <li>支持HTTP状态映射：自动映射到HTTP状态码</li>
 * </ul></p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>业务异常：所有业务逻辑异常应继承此类</li>
 *   <li>异常处理：全局异常处理器统一处理此类异常</li>
 *   <li>错误日志：记录异常信息和上下文</li>
 * </ul></p>
 *
 * <p>示例：
 * <pre>{@code
 * public class ProductNotFoundException extends BaseApplicationException {
 *     public ProductNotFoundException(Long id) {
 *         super("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND,
 *               "Product not found with id: " + id, createContext(id));
 *     }
 *
 *     private static Map<String, Object> createContext(Long id) {
 *         Map<String, Object> context = new HashMap<>();
 *         context.put("productId", id);
 *         return context;
 *     }
 * }
 * }</pre></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see ApiError
 * @see GlobalExceptionHandler
 */
@RequiredArgsConstructor
public abstract class BaseApplicationException extends RuntimeException {

    /**
     * 错误码.
     *
     * <p>业务错误码，用于标识具体的错误类型。
     * 错误码格式：AAA-XX-XXX（例如：SYS-01-001）。</p>
     *
     * <p>错误码规范：
     * <ul>
     *   <li>SYS：系统级错误</li>
     *   <li>VAL：参数验证错误</li>
     *   <li>BIZ：业务逻辑错误</li>
     *   <li>AUTH：认证授权错误</li>
     *   <li>EXT：外部服务错误</li>
     * </ul></p>
     */
    private final String errorCode;

    /**
     * HTTP状态码.
     *
     * <p>HTTP状态码，用于映射到HTTP响应状态。
     * 例如：404 NOT FOUND、400 BAD REQUEST、500 INTERNAL SERVER ERROR。</p>
     */
    private final HttpStatus httpStatus;

    /**
     * 错误消息.
     *
     * <p>错误的详细描述信息，用于向用户展示或日志记录。
     * 消息应简洁明了，避免包含敏感信息。</p>
     */
    private final String message;

    /**
     * 异常上下文信息.
     *
     * <p>异常发生时的上下文数据，用于问题排查和日志记录。
     * 例如：产品ID、订单号、用户ID等关键信息。</p>
     */
    private final transient Map<String, Object> context;

    /**
     * 异常时间戳.
     *
     * <p>异常发生的时间，用于日志分析和问题排查。
     * 时间格式：yyyy-MM-dd HH:mm:ss。</p>
     */
    private final LocalDateTime timestamp;

    /**
     * 构造函数.
     *
     * <p>创建带有错误码、HTTP状态和消息的异常。
     * 自动设置当前时间戳和空的上下文信息。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>创建简单异常，不需要上下文信息</li>
     *   <li>快速抛出业务异常</li>
     * </ul></p>
     *
     * @param errorCode 错误码，必填，不能为null或空字符串
     * @param httpStatus HTTP状态码，必填，不能为null
     * @param message 错误消息，必填，不能为null或空字符串
     * @throws IllegalArgumentException 当errorCode为null或空字符串时抛出
     * @throws IllegalArgumentException 当httpStatus为null时抛出
     * @throws IllegalArgumentException 当message为null或空字符串时抛出
     * @since 3.0.0
     */
    protected BaseApplicationException(final String errorCode, final HttpStatus httpStatus,
                                    final String message) {
        this(errorCode, httpStatus, message, null);
    }

    /**
     * 构造函数.
     *
     * <p>创建带有完整上下文信息的异常。
     * 自动设置当前时间戳。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>创建包含上下文信息的异常</li>
     *   <li>记录异常发生时的关键数据</li>
     *   <li>便于问题排查和日志分析</li>
     * </ul></p>
     *
     * @param errorCode 错误码，必填，不能为null或空字符串
     * @param httpStatus HTTP状态码，必填，不能为null
     * @param message 错误消息，必填，不能为null或空字符串
     * @param context 异常上下文信息，可选，可以为null
     * @throws IllegalArgumentException 当errorCode为null或空字符串时抛出
     * @throws IllegalArgumentException 当httpStatus为null时抛出
     * @throws IllegalArgumentException 当message为null或空字符串时抛出
     * @since 3.0.0
     */
    protected BaseApplicationException(final String errorCode, final HttpStatus httpStatus,
                                    final String message, final Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.message = message;
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 获取错误码.
     *
     * <p>返回业务错误码，用于标识具体的错误类型。</p>
     *
     * @return 错误码，格式为AAA-XX-XXX
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取HTTP状态码.
     *
     * <p>返回HTTP状态码，用于映射到HTTP响应状态。</p>
     *
     * @return HTTP状态码
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 获取错误消息.
     *
     * <p>返回错误的详细描述信息。</p>
     *
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 获取异常上下文信息.
     *
     * <p>返回异常发生时的上下文数据的不可修改副本，防止外部修改内部状态。</p>
     *
     * @return 上下文信息的不可修改副本，如果未设置则返回空Map
     */
    public Map<String, Object> getContext() {
        if (context == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(context));
    }

    /**
     * 获取异常时间戳.
     *
     * <p>返回异常发生的时间。</p>
     *
     * @return 异常时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
