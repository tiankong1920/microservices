package com.inventory.salesservice.exception;

public class SalesOrderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SalesOrderNotFoundException(String message) {
        super(message);
    }
}
