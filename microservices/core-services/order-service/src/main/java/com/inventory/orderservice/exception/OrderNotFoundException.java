package com.inventory.orderservice.exception;

import java.util.HashMap;
import java.util.Map;

import com.inventory.common.core.BaseApplicationException;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an order is not found.
 */
public class OrderNotFoundException extends BaseApplicationException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ORDER_NOT_FOUND";

    /**
 * Constructs a new OrderNotFoundException with the specified detail message.
     *
 * @param message the detail message
     */
    public OrderNotFoundException(String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }

    /**
 * Constructs a new OrderNotFoundException for an order with the specified ID.
     *
 * @param id the ID of the order that was not found
     */
    public OrderNotFoundException(Long id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, "Order not found with id: " + id, createContext(id));
    }

    /**
 * Constructs a new OrderNotFoundException with the specified detail message and cause.
     *
 * @param message the detail message
 * @param cause the cause of the exception
     */
    public OrderNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message, null);
    }

    private static Map<String, Object> createContext(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("orderId", id);
        return context;
    }
}
