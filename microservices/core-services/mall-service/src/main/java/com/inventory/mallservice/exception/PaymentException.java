package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class PaymentException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_PAYMENT_ERROR";

    public PaymentException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static PaymentException onlySuccessfulCanBeRefunded() {
        return new PaymentException("Only successful payments can be refunded");
    }
}
