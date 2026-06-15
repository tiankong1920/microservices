package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class CouponException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_COUPON_ERROR";

    public CouponException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static CouponException usageLimitReached() {
        return new CouponException("Coupon usage limit reached");
    }

    public static CouponException userUsageLimitReached() {
        return new CouponException("User coupon usage limit reached");
    }

    public static CouponException notAvailable() {
        return new CouponException("Coupon is not available for use");
    }

    public static CouponException notFound(String couponCode) {
        return new CouponException("Coupon not found with code: " + couponCode);
    }
}
