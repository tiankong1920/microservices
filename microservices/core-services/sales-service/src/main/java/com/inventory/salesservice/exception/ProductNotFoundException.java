package com.inventory.salesservice.exception;

public class ProductNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
