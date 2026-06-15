package com.inventory.common.core;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API错误响应DTO.
 *
 * <p>统一的API错误响应格式，用于规范化错误返回格式。
 * 所有API错误都应使用此结构确保客户端一致性。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>统一错误响应格式：包含错误码、错误消息、时间戳、请求路径、详细信息</li>
 *   <li>支持Builder模式：简化复杂错误响应的创建</li>
 *   <li>支持静态工厂方法：快速创建常见错误响应</li>
 * </ul></p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>API错误响应：所有API错误都应使用此结构</li>
 *   <li>异常处理：全局异常处理器返回统一错误格式</li>
 *   <li>错误日志：记录错误信息和上下文</li>
 * </ul></p>
 *
 * <p>示例：
 * <pre>{@code
 * ApiError error = ApiError.of("SYS-01-001", "系统错误", "/api/products");
 * ApiError errorWithDetails = ApiError.of("VAL-01-001", "参数验证失败", "/api/products",
 *     Map.of("field", "name", "value", ""));
 * }</pre></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see ApiResponse
 * @see BaseApplicationException
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

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
    private String code;

    /**
     * 错误消息.
     *
     * <p>错误的详细描述信息，用于向用户展示或日志记录。
     * 消息应简洁明了，避免包含敏感信息。</p>
     */
    private String message;

    /**
     * 时间戳.
     *
     * <p>错误发生的时间，用于日志分析和问题排查。
     * 时间格式：yyyy-MM-dd HH:mm:ss。</p>
     */
    private LocalDateTime timestamp;

    /**
     * 请求路径.
     *
     * <p>发生错误的API请求路径，用于定位问题。
     * 例如：/api/products/123。</p>
     */
    private String path;

    /**
     * 详细信息.
     *
     * <p>错误的详细信息，包含额外的上下文数据。
     * 例如：参数验证失败时，包含字段名和错误值。</p>
     */
    private Map<String, Object> details;

    /**
     * 获取详细信息.
     *
     * <p>返回详细信息的不可修改副本，防止外部修改内部状态。</p>
     *
     * @return 详细信息的不可修改副本，如果为null则返回空Map
     */
    public Map<String, Object> getDetails() {
        if (details == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(details));
    }

    /**
     * 设置详细信息.
     *
     * <p>创建参数Map的防御性副本，防止外部修改影响内部状态。</p>
     *
     * @param details 详细信息，可以为null
     */
    public void setDetails(final Map<String, Object> details) {
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }

    /**
     * 创建基本错误响应.
     *
     * <p>静态工厂方法，用于创建基本的错误响应对象。
     * 自动设置当前时间戳和空的详细信息。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>快速创建简单错误响应</li>
     *   <li>不需要详细信息的错误场景</li>
     * </ul></p>
     *
     * @param code 错误码，必填，不能为null或空字符串
     * @param message 错误消息，必填，不能为null或空字符串
     * @param path 请求路径，必填，不能为null或空字符串
     * @return 错误响应对象，包含错误码、错误消息、时间戳、请求路径和空的详细信息
     * @throws IllegalArgumentException 当code为null或空字符串时抛出
     * @throws IllegalArgumentException 当message为null或空字符串时抛出
     * @throws IllegalArgumentException 当path为null或空字符串时抛出
     * @since 3.0.0
     */
    public static ApiError of(final String code, final String message, final String path) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(new HashMap<>())
                .build();
    }

    /**
     * 创建带详细信息的错误响应.
     *
     * <p>静态工厂方法，用于创建包含详细信息的错误响应对象。
     * 自动设置当前时间戳。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>需要额外上下文信息的错误场景</li>
     *   <li>参数验证失败时返回字段错误信息</li>
     *   <li>业务逻辑错误时返回业务上下文</li>
     * </ul></p>
     *
     * @param code 错误码，必填，不能为null或空字符串
     * @param message 错误消息，必填，不能为null或空字符串
     * @param path 请求路径，必填，不能为null或空字符串
     * @param details 详细信息，可选，可以为null
     * @return 错误响应对象，包含错误码、错误消息、时间戳、请求路径和详细信息
     * @throws IllegalArgumentException 当code为null或空字符串时抛出
     * @throws IllegalArgumentException 当message为null或空字符串时抛出
     * @throws IllegalArgumentException 当path为null或空字符串时抛出
     * @since 3.0.0
     */
    public static ApiError of(final String code, final String message, final String path,
                              final Map<String, Object> details) {
        final Map<String, Object> safeDetails = details != null ? new HashMap<>(details) : new HashMap<>();
        return ApiError.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(safeDetails)
                .build();
    }
}
