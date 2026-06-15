package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class RefundException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_REFUND_ERROR";

    public RefundException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static RefundException notFound(Long id) {
        return new RefundException("Refund application not found with id: " + id);
    }

    public static RefundException notInPendingStatus() {
        return new RefundException("Refund application is not in PENDING status");
    }

    public static RefundException notApproved() {
        return new RefundException("Refund application must be approved before processing");
    }

    public static RefundException onlySuccessfulPaymentsCanBeRefunded() {
        return new RefundException("Only successful payments can be refunded");
    }
}
