package com.inventory.salesservice.exception;

public class CustomerNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
