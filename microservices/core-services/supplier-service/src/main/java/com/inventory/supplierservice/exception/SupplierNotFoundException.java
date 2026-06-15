package com.inventory.supplierservice.exception;

/**
 * Supplier Not Found Exception.
 */
public class SupplierNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Supplier not found with id: %d";

    public SupplierNotFoundException(Long id) {
        super(String.format(DEFAULT_MESSAGE, id));
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }

    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
