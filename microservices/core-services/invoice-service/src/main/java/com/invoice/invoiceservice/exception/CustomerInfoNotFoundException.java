package com.invoice.invoiceservice.exception;

public class CustomerInfoNotFoundException extends RuntimeException {

    public CustomerInfoNotFoundException(Long id) {
        super("客户信息未找到，ID: " + id);
    }

    public CustomerInfoNotFoundException(String message) {
        super(message);
    }
}
