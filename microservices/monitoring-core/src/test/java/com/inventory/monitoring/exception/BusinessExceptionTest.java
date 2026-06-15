/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BusinessException类的单元测试，用于测试业务异常的功能。
 */
class BusinessExceptionTest {

    /**
 * 测试前的初始化方法，用于设置测试环境。
     */
    @BeforeEach
    void setUp() {
        // 清除消息缓存，确保测试的独立性
        BusinessException.clearMessageCache();
        // 清除错误码映射，确保测试的独立性
        ErrorCode.clear();
    }

    /**
 * 测试无参构造方法，确保创建的异常对象具有默认值。
     */
    @Test
    void testNoArgsConstructor() {
        final var exception = new BusinessException();
        assertEquals("UNKNOWN_ERROR", exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常消息的构造方法，确保创建的异常对象具有指定的消息。
     */
    @Test
    void testMessageConstructor() {
        final var message = "Test exception message";
        final var exception = new BusinessException(message);
        assertEquals(message, exception.getMessage());
        assertEquals("UNKNOWN_ERROR", exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常消息和错误码的构造方法，确保创建的异常对象具有指定的消息和错误码。
     */
    @Test
    void testMessageAndErrorCodeConstructor() {
        final var message = "Test exception message";
        final var errorCode = "TEST_ERROR";
        final var exception = new BusinessException(message, errorCode);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常消息和异常原因的构造方法，确保创建的异常对象具有指定的消息和原因。
     */
    @Test
    void testMessageAndCauseConstructor() {
        final var message = "Test exception message";
        final var cause = new RuntimeException("Test cause");
        final var exception = new BusinessException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("UNKNOWN_ERROR", exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常消息、错误码和异常原因的构造方法，确保创建的异常对象具有指定的消息、错误码和原因。
     */
    @Test
    void testMessageErrorCodeAndCauseConstructor() {
        final var message = "Test exception message";
        final var errorCode = "TEST_ERROR";
        final var cause = new RuntimeException("Test cause");
        final var exception = new BusinessException(message, errorCode, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常原因的构造方法，确保创建的异常对象具有指定的原因。
     */
    @Test
    void testCauseConstructor() {
        final var cause = new RuntimeException("Test cause");
        final var exception = new BusinessException(cause);
        assertEquals(cause, exception.getCause());
        assertEquals("UNKNOWN_ERROR", exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常原因和错误码的构造方法，确保创建的异常对象具有指定的原因和错误码。
     */
    @Test
    void testCauseAndErrorCodeConstructor() {
        final var cause = new RuntimeException("Test cause");
        final var errorCode = "TEST_ERROR";
        final var exception = new BusinessException(cause, errorCode);
        assertEquals(cause, exception.getCause());
        assertEquals(errorCode, exception.getErrorCode());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
        assertEquals(BusinessException.Severity.ERROR, exception.getSeverity());
    }

    /**
 * 测试带异常消息、错误码和严重程度的构造方法，确保创建的异常对象具有指定的消息、错误码和严重程度。
     */
    @Test
    void testMessageErrorCodeAndSeverityConstructor() {
        final var message = "Test exception message";
        final var errorCode = "TEST_ERROR";
        final var severity = BusinessException.Severity.WARNING;
        final var exception = new BusinessException(message, errorCode, severity);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(severity, exception.getSeverity());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
    }

    /**
 * 测试带异常消息、错误码、异常原因和严重程度的构造方法，确保创建的异常对象具有指定的消息、错误码、原因和严重程度。
     */
    @Test
    void testMessageErrorCodeCauseAndSeverityConstructor() {
        final var message = "Test exception message";
        final var errorCode = "TEST_ERROR";
        final var cause = new RuntimeException("Test cause");
        final var severity = BusinessException.Severity.CRITICAL;
        final var exception = new BusinessException(message, errorCode, cause, severity);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
        assertEquals(severity, exception.getSeverity());
        assertNotNull(exception.getContext());
        assertEquals(0, exception.getContext().size());
    }

    /**
 * 测试添加上下文信息的方法，确保上下文信息被正确添加。
     */
    @Test
    void testAddContext() {
        final var exception = new BusinessException();
        final var key = "testKey";
        final var value = "testValue";
        final var result = exception.addContext(key, value);
        assertSame(exception, result); // 确保返回的是同一个对象，支持链式调用
        assertEquals(value, exception.getContext().get(key));
    }

    /**
 * 测试添加多个上下文信息的方法，确保多个上下文信息被正确添加。
     */
    @Test
    void testAddContextMap() {
        final var exception = new BusinessException();
        final var contextMap = new java.util.HashMap<String, Object>();
        contextMap.put("key1", "value1");
        contextMap.put("key2", "value2");
        final var result = exception.addContext(contextMap);
        assertSame(exception, result); // 确保返回的是同一个对象，支持链式调用
        assertEquals(2, exception.getContext().size());
        assertEquals("value1", exception.getContext().get("key1"));
        assertEquals("value2", exception.getContext().get("key2"));
    }

    /**
 * 测试设置异常严重程度的方法，确保创建的新异常对象具有指定的严重程度，并且上下文信息被复制。
     */
    @Test
    void testWithSeverity() {
        final var exception = new BusinessException("Test message", "TEST_ERROR")
                .addContext("key", "value");
        final var newSeverity = BusinessException.Severity.INFO;
        final var newException = exception.withSeverity(newSeverity);
        assertNotSame(exception, newException); // 确保返回的是新对象
        assertEquals(newSeverity, newException.getSeverity());
        assertEquals(exception.getMessage(), newException.getMessage());
        assertEquals(exception.getErrorCode(), newException.getErrorCode());
        assertEquals(exception.getContext(), newException.getContext());
    }

    /**
 * 测试通过错误码创建异常实例的静态工厂方法，确保创建的异常对象具有错误码对应的消息。
     */
    @Test
    void testWithErrorCode() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertNotNull(exception.getMessage());
    }

    /**
 * 测试通过错误码和自定义消息创建异常实例的静态工厂方法，确保创建的异常对象具有指定的消息和错误码。
     */
    @Test
    void testWithErrorCodeAndMessage() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        final var message = "Custom exception message";
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode, message);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertEquals(message, exception.getMessage());
    }

    /**
 * 测试通过错误码和异常原因创建异常实例的静态工厂方法，确保创建的异常对象具有错误码对应的消息和指定的原因。
     */
    @Test
    void testWithErrorCodeAndCause() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        final var cause = new RuntimeException("Test cause");
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode, cause);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertNotNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    /**
 * 测试通过错误码和严重程度创建异常实例的静态工厂方法，确保创建的异常对象具有错误码对应的消息和指定的严重程度。
     */
    @Test
    void testWithErrorCodeAndSeverity() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        final var severity = BusinessException.Severity.WARNING;
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode, severity);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertNotNull(exception.getMessage());
        assertEquals(severity, exception.getSeverity());
    }

    /**
 * 测试通过错误码、自定义消息和严重程度创建异常实例的静态工厂方法，确保创建的异常对象具有指定的消息、错误码和严重程度。
     */
    @Test
    void testWithErrorCodeMessageAndSeverity() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        final var message = "Custom exception message";
        final var severity = BusinessException.Severity.CRITICAL;
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode, message, severity);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(severity, exception.getSeverity());
    }

    /**
 * 测试通过错误码、异常原因和严重程度创建异常实例的静态工厂方法，确保创建的异常对象具有错误码对应的消息、指定的原因和严重程度。
     */
    @Test
    void testWithErrorCodeCauseAndSeverity() {
        // 注册一个错误码
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        final var cause = new RuntimeException("Test cause");
        final var severity = BusinessException.Severity.INFO;
        // 使用静态工厂方法创建异常
        final var exception = BusinessException.withErrorCode(errorCode, cause, severity);
        assertEquals(errorCode.getCode(), exception.getErrorCode());
        assertNotNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(severity, exception.getSeverity());
    }

    /**
 * 测试获取消息缓存大小的方法，确保消息缓存大小被正确返回。
     */
    @Test
    void testMessageCacheSize() {
        // 初始缓存大小应该为0
        assertEquals(0, BusinessException.getMessageCacheSize());
        // 注册一个错误码并创建异常，触发消息加载
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        BusinessException.withErrorCode(errorCode);
        // 缓存大小应该大于0
        assertTrue(BusinessException.getMessageCacheSize() > 0);
    }

    /**
 * 测试清除消息缓存的方法，确保消息缓存被正确清除。
     */
    @Test
    void testClearMessageCache() {
        // 注册一个错误码并创建异常，触发消息加载
        final var errorCode = ErrorCode.register("TEST_ERROR", "error.unknown", "Unknown error");
        BusinessException.withErrorCode(errorCode);
        // 缓存大小应该大于0
        assertTrue(BusinessException.getMessageCacheSize() > 0);
        // 清除缓存
        BusinessException.clearMessageCache();
        // 缓存大小应该为0
        assertEquals(0, BusinessException.getMessageCacheSize());
    }

    /**
 * 测试equals方法，确保异常对象的比较逻辑正确。
     */
    @Test
    void testEquals() {
        final var exception1 = new BusinessException("Test message", "TEST_ERROR")
                .addContext("key", "value");
        final var exception2 = new BusinessException("Test message", "TEST_ERROR")
                .addContext("key", "value");
        final var exception3 = new BusinessException("Different message", "TEST_ERROR")
                .addContext("key", "value");
        // 相同对象应该相等
        assertEquals(exception1, exception1);
        // 具有相同属性的不同对象应该相等
        assertEquals(exception1, exception2);
        // 具有不同属性的对象应该不相等
        assertNotEquals(exception1, exception3);
        // 与null比较应该不相等
        assertNotEquals(exception1, null);
        // 与不同类型的对象比较应该不相等
        assertNotEquals(exception1, "string");
    }

    /**
 * 测试hashCode方法，确保异常对象的哈希计算逻辑正确。
     */
    @Test
    void testHashCode() {
        final var exception1 = new BusinessException("Test message", "TEST_ERROR")
                .addContext("key", "value");
        final var exception2 = new BusinessException("Test message", "TEST_ERROR")
                .addContext("key", "value");
        // 具有相同属性的对象应该具有相同的哈希值
        assertEquals(exception1.hashCode(), exception2.hashCode());
    }
}
