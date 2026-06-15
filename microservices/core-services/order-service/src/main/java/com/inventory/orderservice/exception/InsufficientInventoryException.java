package com.inventory.orderservice.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long productId) {
        super("Insufficient inventory for product id: " + productId);
    }

    public InsufficientInventoryException(String message) {
        super(message);
    }

    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
