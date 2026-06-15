package com.inventory.adminservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseApplicationException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(String message) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND, message);
    }
}
