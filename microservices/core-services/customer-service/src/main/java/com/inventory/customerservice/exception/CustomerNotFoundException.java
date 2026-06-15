package com.inventory.customerservice.exception;

import java.util.HashMap;
import java.util.Map;

import com.inventory.common.core.BaseApplicationException;

import org.springframework.http.HttpStatus;

/**
 * Customer Not Found Exception.
 */
public class CustomerNotFoundException extends BaseApplicationException {

    private static final String ERROR_CODE = "CUSTOMER_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "Customer not found with id: %d";

    public CustomerNotFoundException(Long id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, String.format(DEFAULT_MESSAGE, id), createContext(id));
    }

    public CustomerNotFoundException(String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message, null);
    }

    private static Map<String, Object> createContext(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("customerId", id);
        return context;
    }
}
