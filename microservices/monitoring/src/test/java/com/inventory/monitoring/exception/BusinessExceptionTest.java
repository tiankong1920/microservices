/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessExceptionTest {

    private static final String TEST_MESSAGE = "Test business exception message";
    private static final String TEST_ERROR_CODE = "TEST_ERROR_001";
    private static final String DEFAULT_ERROR_CODE = "UNKNOWN_ERROR";

    @Test
    void testNoArgsConstructor() {
        final BusinessException exception = new BusinessException();
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testMessageConstructor() {
        final BusinessException exception = new BusinessException(TEST_MESSAGE);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testMessageAndErrorCodeConstructor() {
        final BusinessException exception = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testMessageAndCauseConstructor() {
        final RuntimeException cause = new RuntimeException("Test cause");
        final BusinessException exception = new BusinessException(TEST_MESSAGE, cause);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testMessageErrorCodeAndCauseConstructor() {
        final RuntimeException cause = new RuntimeException("Test cause");
        final BusinessException exception = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE, cause);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testCauseConstructor() {
        final RuntimeException cause = new RuntimeException("Test cause");
        final BusinessException exception = new BusinessException(cause);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Test cause");
        assertThat(exception.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testCauseAndErrorCodeConstructor() {
        final RuntimeException cause = new RuntimeException("Test cause");
        final BusinessException exception = new BusinessException(cause, TEST_ERROR_CODE);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Test cause");
        assertThat(exception.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testExceptionThrowing() {
        final BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    throw new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
                }
        );
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
    }

    @Test
    void testExceptionMessagePropagation() {
        final String nestedMessage = "Nested exception message";
        final RuntimeException nestedException = new RuntimeException(nestedMessage);
        final BusinessException exception = new BusinessException(TEST_MESSAGE, nestedException);
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getCause()).isNotNull();
        assertThat(exception.getCause().getMessage()).isEqualTo(nestedMessage);
    }

    @Test
    void testStackTraceIntegrity() {
        final BusinessException exception = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
        assertThat(exception.getStackTrace()[0].getClassName()).isEqualTo(BusinessExceptionTest.class.getName());
        assertThat(exception.getStackTrace()[0].getMethodName()).isEqualTo("testStackTraceIntegrity");
    }

    @Test
    void testDifferentErrorCodes() {
        final String[] errorCodes = {"ERROR_001", "ERROR_002", "ERROR_003"};
        for (final String errorCode : errorCodes) {
            final BusinessException exception = new BusinessException(TEST_MESSAGE, errorCode);
            assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        }
    }

    @Test
    void testSerializationCompatibility() throws IOException, ClassNotFoundException {
        final BusinessException originalException = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalException);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final BusinessException deserializedException = (BusinessException) ois.readObject();
        ois.close();

        assertThat(deserializedException).isNotNull();
        assertThat(deserializedException.getMessage()).isEqualTo(originalException.getMessage());
        assertThat(deserializedException.getErrorCode()).isEqualTo(originalException.getErrorCode());
        assertThat(deserializedException.getCause()).isNull();
    }

    @Test
    void testWithErrorCode() {
        final BusinessException exception = BusinessException.withErrorCode(
            com.inventory.monitoring.exception.ErrorCode.BUSINESS_OPERATION_FAILED);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_OPERATION_FAILED");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testWithErrorCodeAndMessage() {
        final BusinessException exception = BusinessException.withErrorCode(
            com.inventory.monitoring.exception.ErrorCode.BUSINESS_OPERATION_FAILED, 
            TEST_MESSAGE);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_OPERATION_FAILED");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testWithErrorCodeAndCause() {
        final RuntimeException cause = new RuntimeException("Test cause");
        final BusinessException exception = BusinessException.withErrorCode(
            com.inventory.monitoring.exception.ErrorCode.BUSINESS_OPERATION_FAILED, 
            cause);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_OPERATION_FAILED");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testEquals() {
        final BusinessException exception1 = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        final BusinessException exception2 = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        final BusinessException exception3 = new BusinessException("Different message", TEST_ERROR_CODE);
        final BusinessException exception4 = new BusinessException(TEST_MESSAGE, "DIFFERENT_ERROR_CODE");

        assertThat(exception1).isEqualTo(exception1);
        assertThat(exception1).isEqualTo(exception2);
        assertThat(exception1).isNotEqualTo(exception3);
        assertThat(exception1).isNotEqualTo(exception4);
        assertThat(exception1).isNotEqualTo(null);
        assertThat(exception1).isNotEqualTo(new Object());
    }

    @Test
    void testHashCode() {
        final BusinessException exception1 = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        final BusinessException exception2 = new BusinessException(TEST_MESSAGE, TEST_ERROR_CODE);
        assertThat(exception1.hashCode()).isEqualTo(exception2.hashCode());
    }

    private static Stream<Arguments> provideExceptionTestData() {
        return Stream.of(
                Arguments.of(null, DEFAULT_ERROR_CODE, null),
                Arguments.of(TEST_MESSAGE, DEFAULT_ERROR_CODE, null),
                Arguments.of(TEST_MESSAGE, TEST_ERROR_CODE, null),
                Arguments.of(TEST_MESSAGE, DEFAULT_ERROR_CODE, new RuntimeException("Test cause")),
                Arguments.of(TEST_MESSAGE, TEST_ERROR_CODE, new RuntimeException("Test cause")),
                Arguments.of(null, DEFAULT_ERROR_CODE, new RuntimeException("Test cause")),
                Arguments.of(null, TEST_ERROR_CODE, new RuntimeException("Test cause"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionTestData")
    void testExceptionWithDifferentParameters(final String message, final String errorCode, final Throwable cause) {
        final BusinessException exception;
        if (message != null && errorCode != null && cause != null) {
            exception = new BusinessException(message, errorCode, cause);
        } else if (message != null && errorCode != null) {
            exception = new BusinessException(message, errorCode);
        } else if (message != null && cause != null) {
            exception = new BusinessException(message, cause);
        } else if (cause != null && errorCode != null) {
            exception = new BusinessException(cause, errorCode);
        } else if (message != null) {
            exception = new BusinessException(message);
        } else if (cause != null) {
            exception = new BusinessException(cause);
        } else {
            exception = new BusinessException();
        }

        assertThat(exception).isNotNull();
        if (message != null) {
            assertThat(exception.getMessage()).isEqualTo(message);
        }
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }
}

