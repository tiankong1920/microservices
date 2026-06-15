package com.inventory.common.core.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

public class EncryptionException extends BaseApplicationException {

    private static final String ERROR_CODE = "ENCRYPTION_ERROR";

    public EncryptionException(String operation, String reason) {
        super(ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR,
            "Encryption " + operation + " failed: " + reason);
    }

    public static EncryptionException encryptFailed(String reason) {
        return new EncryptionException("encryption", reason);
    }

    public static EncryptionException decryptFailed(String reason) {
        return new EncryptionException("decryption", reason);
    }
}
