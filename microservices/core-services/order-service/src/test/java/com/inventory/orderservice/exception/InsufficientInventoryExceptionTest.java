package com.inventory.orderservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InsufficientInventoryException Unit Tests")
class InsufficientInventoryExceptionTest {

    @Test
    @DisplayName("Should create exception with product id")
    void shouldCreateExceptionWithProductId() {
        InsufficientInventoryException exception = new InsufficientInventoryException(100L);

        assertTrue(exception.getMessage().contains("100"));
        assertTrue(exception.getMessage().contains("Insufficient inventory"));
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with custom message")
    void shouldCreateExceptionWithCustomMessage() {
        InsufficientInventoryException exception = new InsufficientInventoryException("Custom error message");

        assertEquals("Custom error message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Original error");
        InsufficientInventoryException exception = new InsufficientInventoryException("Custom error", cause);

        assertEquals("Custom error", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should propagate exception correctly")
    void shouldPropagateExceptionCorrectly() {
        InsufficientInventoryException original = new InsufficientInventoryException(50L);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            throw original;
        });

        assertSame(original, thrown);
    }
}
