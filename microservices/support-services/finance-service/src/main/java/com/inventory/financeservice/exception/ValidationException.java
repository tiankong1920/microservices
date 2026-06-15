package com.inventory.financeservice.exception;

public class ValidationException extends RuntimeException {
    private final String fieldName;
    private final Object rejectedValue;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = null;
    }

    public ValidationException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
