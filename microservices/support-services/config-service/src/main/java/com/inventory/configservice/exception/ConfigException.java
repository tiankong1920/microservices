package com.inventory.configservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class ConfigException extends BaseApplicationException {

    private static final String ERROR_CODE = "CONFIG_ERROR";

    public ConfigException(String message) {
        super(ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static ConfigException initializationFailed(String details) {
        return new ConfigException("Failed to initialize Nacos ConfigService: " + details);
    }

    public static ConfigException operationFailed(String operation, String dataId, String group, String details) {
        return new ConfigException(
            "Config " + operation + " failed [" + dataId + ":" + group + "]: " + details
        );
    }
}
