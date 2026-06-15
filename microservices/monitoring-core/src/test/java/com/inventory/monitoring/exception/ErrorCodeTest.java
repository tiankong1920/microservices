/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorCodeTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void testConstructor() {
        ErrorCode.clear();

        final var code = "TEST_ERROR";
        final var messageKey = "error.test";
        final var defaultMessage = "Test error message";
        final var errorCode = new ErrorCode(code, messageKey, defaultMessage);
        assertEquals(code, errorCode.getCode());
        assertEquals(messageKey, errorCode.getMessageKey());
        assertEquals(defaultMessage, errorCode.getDefaultMessage());
        assertEquals(errorCode, ErrorCode.getByCode(code));
    }

    @Test
    void testRegister() {
        ErrorCode.clear();

        final var code = "TEST_ERROR";
        final var messageKey = "error.test";
        final var defaultMessage = "Test error message";
        final var errorCode = ErrorCode.register(code, messageKey, defaultMessage);
        assertEquals(code, errorCode.getCode());
        assertEquals(messageKey, errorCode.getMessageKey());
        assertEquals(defaultMessage, errorCode.getDefaultMessage());
        assertEquals(errorCode, ErrorCode.getByCode(code));
    }

    @Test
    void testGetByCode() {
        ErrorCode.clear();

        final var code = "TEST_ERROR";
        final var messageKey = "error.test";
        final var defaultMessage = "Test error message";
        final var errorCode = ErrorCode.register(code, messageKey, defaultMessage);
        final var retrievedErrorCode = ErrorCode.getByCode(code);
        assertEquals(errorCode, retrievedErrorCode);
        final var nonExistentErrorCode = ErrorCode.getByCode("NON_EXISTENT_ERROR");
        assertNull(nonExistentErrorCode);
    }

    @Test
    void testGetAllErrorCodes() {
        ErrorCode.clear();

        final var errorCode1 = ErrorCode.register("ERROR_1", "error.one", "Error one message");
        final var errorCode2 = ErrorCode.register("ERROR_2", "error.two", "Error two message");
        final var allErrorCodes = ErrorCode.getAllErrorCodes();
        assertEquals(2, allErrorCodes.size());
        assertEquals(errorCode1, allErrorCodes.get("ERROR_1"));
        assertEquals(errorCode2, allErrorCodes.get("ERROR_2"));
    }

    @Test
    void testClear() {
        final var code = "TEST_ERROR";
        ErrorCode.register(code, "error.test", "Test error message");
        assertNotNull(ErrorCode.getByCode(code));
        ErrorCode.clear();
        assertNull(ErrorCode.getByCode(code));
        assertEquals(0, ErrorCode.getAllErrorCodes().size());
    }

    @Test
    void testDefaultErrorCodes() {
        // Note: Default error codes are registered statically when the ErrorCode class is loaded.
        // If clear() was called in previous tests, we need to re-register them.
        // For this test, we just verify that the static fields exist and have correct values.
        
        assertNotNull(ErrorCode.UNKNOWN_ERROR);
        assertNotNull(ErrorCode.INVALID_PARAMETER);
        assertNotNull(ErrorCode.RESOURCE_NOT_FOUND);
        assertNotNull(ErrorCode.DATABASE_ERROR);
        assertNotNull(ErrorCode.NETWORK_ERROR);
        assertNotNull(ErrorCode.AUTHENTICATION_ERROR);
        assertNotNull(ErrorCode.AUTHORIZATION_ERROR);
        assertNotNull(ErrorCode.SERVICE_UNAVAILABLE);
        assertNotNull(ErrorCode.TIMEOUT_ERROR);
        assertNotNull(ErrorCode.VALIDATION_ERROR);
        assertNotNull(ErrorCode.BUSINESS_OPERATION_FAILED);

        assertEquals("UNKNOWN_ERROR", ErrorCode.UNKNOWN_ERROR.getCode());
        assertEquals("INVALID_PARAMETER", ErrorCode.INVALID_PARAMETER.getCode());
        assertEquals("RESOURCE_NOT_FOUND", ErrorCode.RESOURCE_NOT_FOUND.getCode());
        assertEquals("DATABASE_ERROR", ErrorCode.DATABASE_ERROR.getCode());
        assertEquals("NETWORK_ERROR", ErrorCode.NETWORK_ERROR.getCode());
        assertEquals("AUTHENTICATION_ERROR", ErrorCode.AUTHENTICATION_ERROR.getCode());
        assertEquals("AUTHORIZATION_ERROR", ErrorCode.AUTHORIZATION_ERROR.getCode());
        assertEquals("SERVICE_UNAVAILABLE", ErrorCode.SERVICE_UNAVAILABLE.getCode());
        assertEquals("TIMEOUT_ERROR", ErrorCode.TIMEOUT_ERROR.getCode());
        assertEquals("VALIDATION_ERROR", ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals("BUSINESS_OPERATION_FAILED", ErrorCode.BUSINESS_OPERATION_FAILED.getCode());
    }
}
