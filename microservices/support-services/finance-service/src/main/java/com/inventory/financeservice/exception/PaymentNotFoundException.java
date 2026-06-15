package com.inventory.financeservice.exception;

import java.util.HashMap;
import java.util.Map;

import com.inventory.common.core.BaseApplicationException;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseApplicationException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "PAYMENT_NOT_FOUND";

    public PaymentNotFoundException(String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }

    public PaymentNotFoundException(Long id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, "Payment not found with id: " + id, createContext(id));
    }

    public PaymentNotFoundException(String field, String value) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, 
              "Payment not found with " + field + ": " + value, createContext(field, value));
    }

    private static Map<String, Object> createContext(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("paymentId", id);
        return context;
    }

    private static Map<String, Object> createContext(String field, String value) {
        Map<String, Object> context = new HashMap<>();
        context.put(field, value);
        return context;
    }
}
