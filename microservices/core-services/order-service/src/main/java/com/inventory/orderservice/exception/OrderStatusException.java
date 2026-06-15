package com.inventory.orderservice.exception;

public class OrderStatusException extends RuntimeException {

    public OrderStatusException(final String message) {
        super(message);
    }

    public OrderStatusException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
