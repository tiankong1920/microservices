package com.inventory.common.core.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseApplicationException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static ValidationException forField(String field, String message) {
        return new ValidationException("Validation failed for field '" + field + "': " + message);
    }

    public static ValidationException illegalState(String message) {
        return new ValidationException("Illegal state: " + message);
    }
}
