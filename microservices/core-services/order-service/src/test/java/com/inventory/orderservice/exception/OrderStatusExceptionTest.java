package com.inventory.orderservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OrderStatusException Unit Tests")
class OrderStatusExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        OrderStatusException exception = new OrderStatusException("Test message");

        assertEquals("Test message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Original error");
        OrderStatusException exception = new OrderStatusException("Test message", cause);

        assertEquals("Test message", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should propagate exception correctly")
    void shouldPropagateExceptionCorrectly() {
        OrderStatusException original = new OrderStatusException("Original error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            throw original;
        });

        assertSame(original, thrown);
    }
}
