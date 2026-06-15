package com.inventory.common.core.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BaseApplicationException {

    private static final String ERROR_CODE = "ENTITY_NOT_FOUND";

    public EntityNotFoundException(String entityType, Object id) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND,
            entityType + " not found with id: " + id);
    }

    public EntityNotFoundException(String entityType, String field, Object value) {
        super(ERROR_CODE, HttpStatus.NOT_FOUND,
            entityType + " not found with " + field + ": " + value);
    }

    public static EntityNotFoundException forEntity(String entityType, Object id) {
        return new EntityNotFoundException(entityType, id);
    }

    public static EntityNotFoundException forEntityWithField(String entityType, String field, Object value) {
        return new EntityNotFoundException(entityType, field, value);
    }
}
