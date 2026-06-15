package com.inventory.inventoryservice.exception;

/**
 * Exception thrown when a batch record is not found.
 */
public class BatchNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
 * Constructs a new BatchNotFoundException with the specified detail message.
     *
 * @param message the detail message
     */
    public BatchNotFoundException(String message) {
        super(message);
    }

    /**
 * Constructs a new BatchNotFoundException with the specified detail message and cause.
     *
 * @param message the detail message
 * @param cause the cause of the exception
     */
    public BatchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
