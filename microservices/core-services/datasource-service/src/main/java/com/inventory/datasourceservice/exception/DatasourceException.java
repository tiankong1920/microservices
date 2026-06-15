package com.inventory.datasourceservice.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class DatasourceException extends BaseApplicationException {

    private static final String ERROR_CODE = "DATASOURCE_ERROR";

    public DatasourceException(String message) {
        super(ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static DatasourceException encryptionFailed(String reason) {
        return new DatasourceException("Encryption failed: " + reason);
    }

    public static DatasourceException decryptionFailed(String reason) {
        return new DatasourceException("Decryption failed: " + reason);
    }

    public static DatasourceException metadataDiscoveryFailed(String plugin, String reason) {
        return new DatasourceException("Failed to discover metadata using " + plugin + ": " + reason);
    }
}
