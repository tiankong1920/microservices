package com.inventory.mallservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class DistributorException extends BaseApplicationException {

    private static final String ERROR_CODE = "MALL_DISTRIBUTOR_ERROR";

    public DistributorException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static DistributorException alreadyDistributor() {
        return new DistributorException("User is already a distributor");
    }
}
