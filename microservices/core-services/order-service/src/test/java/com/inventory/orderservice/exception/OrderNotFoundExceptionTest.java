package com.inventory.orderservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("null")
class OrderNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        OrderNotFoundException exception = new OrderNotFoundException("Custom message");

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Custom message", exception.getMessage());
    }

    @Test
    void testConstructorWithId() {
        OrderNotFoundException exception = new OrderNotFoundException(42L);

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Order not found with id: 42", exception.getMessage());
        assertNotNull(exception.getContext());
        assertEquals(42L, exception.getContext().get("orderId"));
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        OrderNotFoundException exception = new OrderNotFoundException("Custom message", cause);

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }
}
