package com.inventory.inventoryservice.exception;

/**
 * Exception thrown when a warehouse record is not found.
 */
public class WarehouseNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
 * Constructs a new WarehouseNotFoundException with the specified detail message.
     *
 * @param message the detail message
     */
    public WarehouseNotFoundException(String message) {
        super(message);
    }

    /**
 * Constructs a new WarehouseNotFoundException with the specified detail message and cause.
     *
 * @param message the detail message
 * @param cause the cause of the exception
     */
    public WarehouseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
