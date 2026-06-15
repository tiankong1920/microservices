package com.inventory.common.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * 统一全局异常处理器测试类。
 */
@ExtendWith(MockitoExtension.class)
class UnifiedGlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final UnifiedGlobalExceptionHandler exceptionHandler = new UnifiedGlobalExceptionHandler();

    private static class TestApplicationException extends BaseApplicationException {
        TestApplicationException(String errorCode, HttpStatus httpStatus, String message) {
            super(errorCode, httpStatus, message);
        }
    }

    @Test
    void testHandleBaseApplicationException() {
        BaseApplicationException ex = new TestApplicationException(
                "TEST_ERROR",
                HttpStatus.BAD_REQUEST,
                "Test error message"
        );

        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiError> response = exceptionHandler.handleBaseApplicationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals("TEST_ERROR", body.getCode());
        assertEquals("Test error message", body.getMessage());
    }

    @Test
    void testHandleBaseApplicationExceptionWithContext() {
        BaseApplicationException ex = new TestApplicationException(
                "TEST_ERROR",
                HttpStatus.BAD_REQUEST,
                "Test error message"
        );

        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiError> response = exceptionHandler.handleBaseApplicationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals("/api/test", body.getPath());
    }
}
