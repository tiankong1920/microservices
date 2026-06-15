package com.inventory.productservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class CategoryException extends BaseApplicationException {

    private static final String ERROR_CODE = "CATEGORY_ERROR";

    public CategoryException(String message) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, message);
    }

    public static CategoryException cannotDeleteWithSubCategories() {
        return new CategoryException("Cannot delete category with sub-categories");
    }
}
