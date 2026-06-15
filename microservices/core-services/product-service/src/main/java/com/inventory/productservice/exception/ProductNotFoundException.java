package com.inventory.productservice.exception;

import java.util.HashMap;
import java.util.Map;

import com.inventory.common.core.BaseApplicationException;

import org.springframework.http.HttpStatus;

/**
 * 产品未找到异常.
 *
 * <p>当尝试访问不存在的产品时抛出该异常。
 * 该异常继承自BaseApplicationException，用于统一异常处理和错误响应。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 * @see BaseApplicationException
 */
public class ProductNotFoundException extends BaseApplicationException {

    /** 序列化版本UID. */
    private static final long serialVersionUID = 1L;

    /** 错误码. */
    private static final String ERROR_CODE = "PRODUCT_NOT_FOUND";

    /**
     * 构造一个新的ProductNotFoundException，使用指定的错误消息.
     *
     * @param message 错误消息，描述产品未找到的原因
     */
    public ProductNotFoundException(final String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }

    /**
     * 构造一个新的ProductNotFoundException，使用指定的产品ID.
     *
     * @param id 产品ID
     */
    public ProductNotFoundException(final Long id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND,
                "Product not found with id: " + id, createContext(id));
    }

    /**
     * 构造一个新的ProductNotFoundException，使用指定的错误消息和原因.
     *
     * @param message 错误消息，描述产品未找到的原因
     * @param cause 异常原因，原始异常对象
     */
    public ProductNotFoundException(final String message, final Throwable cause) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message, null);
    }

    /**
     * 创建异常上下文.
     *
     * @param id 产品ID
     * @return 包含产品ID的上下文Map对象
     */
    private static Map<String, Object> createContext(final Long id) {
        final Map<String, Object> context = new HashMap<>();
        context.put("productId", id);
        return context;
    }
}
