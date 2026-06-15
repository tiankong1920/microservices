package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class BargainException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_BARGAIN_ERROR";

    public BargainException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static BargainException notOngoing() {
        return new BargainException("Bargain is not ongoing");
    }

    public static BargainException maxCountReached() {
        return new BargainException("Max bargain count reached");
    }
}
