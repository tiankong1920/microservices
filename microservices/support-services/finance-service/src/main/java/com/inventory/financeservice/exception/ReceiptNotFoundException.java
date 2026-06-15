package com.inventory.financeservice.exception;

import java.util.HashMap;
import java.util.Map;

import com.inventory.common.core.BaseApplicationException;

import org.springframework.http.HttpStatus;

public class ReceiptNotFoundException extends BaseApplicationException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "RECEIPT_NOT_FOUND";

    public ReceiptNotFoundException(String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }

    public ReceiptNotFoundException(Long id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, "Receipt not found with id: " + id, createContext(id));
    }

    public ReceiptNotFoundException(String field, String value) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, 
              "Receipt not found with " + field + ": " + value, createContext(field, value));
    }

    private static Map<String, Object> createContext(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("receiptId", id);
        return context;
    }

    private static Map<String, Object> createContext(String field, String value) {
        Map<String, Object> context = new HashMap<>();
        context.put(field, value);
        return context;
    }
}
