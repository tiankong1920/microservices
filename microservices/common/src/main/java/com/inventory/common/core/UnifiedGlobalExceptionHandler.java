package com.inventory.common.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一的全局异常处理器，用于规范化所有异常的响应格式.
 *
 * <p>替换各模块中分散的异常处理逻辑，确保API错误响应的一致性。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class UnifiedGlobalExceptionHandler {

    /** 验证错误码. */
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    /** 约束违反错误码. */
    private static final String CONSTRAINT_VIOLATION_CODE = "CONSTRAINT_VIOLATION";

    /** 内部服务器错误码. */
    private static final String INTERNAL_SERVER_ERROR_CODE = "INTERNAL_SERVER_ERROR";

    /** 验证失败消息. */
    private static final String VALIDATION_FAILED_MSG = "请求参数校验失败";

    /** 约束校验失败消息. */
    private static final String CONSTRAINT_FAILED_MSG = "约束校验失败";

    /** 服务器错误消息. */
    private static final String SERVER_ERROR_MSG = "服务器内部错误";

    /** 违规信息键. */
    private static final String VIOLATIONS_KEY = "violations";

    /**
     * 处理基础应用异常.
     *
     * @param ex 异常对象
     * @param request HTTP请求
     * @return API错误响应
     */
    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ApiError> handleBaseApplicationException(
            final BaseApplicationException ex, final HttpServletRequest request) {
        log.warn("Application exception: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        final ApiError apiError = ApiError.of(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContext()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(apiError);
    }

    /**
     * 处理方法参数校验异常.
     *
     * @param ex 异常对象
     * @param request HTTP请求
     * @return API错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        final Map<String, Object> details = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        final ApiError apiError = ApiError.of(
                VALIDATION_ERROR_CODE,
                VALIDATION_FAILED_MSG,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * 处理约束违反异常.
     *
     * @param ex 异常对象
     * @param request HTTP请求
     * @return API错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            final ConstraintViolationException ex, final HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        final Map<String, Object> details = new HashMap<>();
        details.put(VIOLATIONS_KEY, ex.getConstraintViolations());

        final ApiError apiError = ApiError.of(
                CONSTRAINT_VIOLATION_CODE,
                CONSTRAINT_FAILED_MSG,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    private static final String BUSINESS_ERROR_CODE = "BUSINESS_ERROR";

    /**
     * 处理业务运行时异常.
     * <p>对于编程错误（NPE等）返回500，对于业务逻辑异常返回400。</p>
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            final RuntimeException ex, final HttpServletRequest request) {
        // 编程错误返回500
        if (isProgrammingError(ex)) {
            log.error("Unexpected runtime error", ex);
            final ApiError apiError = ApiError.of(
                    INTERNAL_SERVER_ERROR_CODE,
                    SERVER_ERROR_MSG,
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
        }

        log.warn("Business exception: {}", ex.getMessage());
        String userMessage = mapToUserFriendlyMessage(ex.getMessage());
        String errorCode = BUSINESS_ERROR_CODE;

        if (ex.getClass().getSimpleName().endsWith("Exception")) {
            errorCode = ex.getClass().getSimpleName().replace("Exception", "_ERROR").toUpperCase();
        }

        final ApiError apiError = ApiError.of(
                errorCode,
                userMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    private static boolean isProgrammingError(final RuntimeException ex) {
        return ex instanceof NullPointerException
                || ex instanceof IndexOutOfBoundsException
                || ex instanceof ArrayIndexOutOfBoundsException
                || ex instanceof ClassCastException
                || ex instanceof ArithmeticException;
    }

    private static final Map<String, String> ERROR_MESSAGE_MAP = new LinkedHashMap<>();

    static {
        ERROR_MESSAGE_MAP.put("cannot cancel a completed order", "无法取消已完成的订单");
        ERROR_MESSAGE_MAP.put("already cancelled", "订单已取消，无需重复操作");
        ERROR_MESSAGE_MAP.put("cannot cancel order in status", "当前订单状态不允许取消操作");
        ERROR_MESSAGE_MAP.put("can only delete orders", "只能删除待处理或已取消的订单");
        ERROR_MESSAGE_MAP.put("can only add items to orders", "只能向待处理状态的订单添加商品");
        ERROR_MESSAGE_MAP.put("can only remove items from orders", "只能从待处理状态的订单中移除商品");
        ERROR_MESSAGE_MAP.put("insufficient inventory", "库存不足，无法完成操作");
        ERROR_MESSAGE_MAP.put("failed to restore inventory", "库存恢复失败");
        ERROR_MESSAGE_MAP.put("invalid status transition", "订单状态转换无效");
    }

    private String mapToUserFriendlyMessage(String message) {
        if (message == null) {
            return "请求处理失败";
        }
        String lowerMessage = message.toLowerCase();
        for (Map.Entry<String, String> entry : ERROR_MESSAGE_MAP.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "请求处理失败，请稍后重试";
    }

    /**
     * 处理通用异常.
     *
     * @param ex 异常对象
     * @param request HTTP请求
     * @return API错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            final Exception ex, final HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        final ApiError apiError = ApiError.of(
                INTERNAL_SERVER_ERROR_CODE,
                SERVER_ERROR_MSG,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
