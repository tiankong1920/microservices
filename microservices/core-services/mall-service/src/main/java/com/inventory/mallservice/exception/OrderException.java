package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class OrderException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_ORDER_ERROR";

    public OrderException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static OrderException noCartItems() {
        return new OrderException("No cart items found");
    }

    public static OrderException cannotCancel(String status) {
        return new OrderException("Cannot cancel order in status: " + status);
    }

    public static OrderException mustBeShipped() {
        return new OrderException("Order must be shipped before confirming receipt");
    }
}
