package com.inventory.common.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Error code manager for centralized management and validation of error codes.
 * Implements centralized management, unified allocation and version control of error codes.
 */
public final class ErrorCodeManager {

    /**
     * Error code format regex: [ModulePrefix]-[ErrorType]-[Sequence].
     * Module prefix no more than 4 characters,
     * error type 2 digits, sequence 3 digits.
     */
    private static final Pattern ERROR_CODE_PATTERN =
            Pattern.compile("^[A-Z]{1,4}-\\d{2}-\\d{3}$");

    /** Singleton instance. */
    private static final ErrorCodeManager INSTANCE = new ErrorCodeManager();

    /** Store all error codes, key is error code, value is error code description. */
    private final Map<String, String> errorCodeMap = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent external instantiation.
     */
    private ErrorCodeManager() {
        // Initialize common error codes
        initCommonErrorCodes();
    }

    /**
     * Get ErrorCodeManager instance.
     *
     * @return ErrorCodeManager instance
     */
    public static ErrorCodeManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize common error codes.
     */
    private void initCommonErrorCodes() {
        // System errors
        registerErrorCode("SYS-01-001", "System internal error");
        registerErrorCode("SYS-01-002", "Database connection error");
        registerErrorCode("SYS-01-003", "Third-party service call failed");

        // Parameter errors
        registerErrorCode("PAR-02-001", "Parameter format error");
        registerErrorCode("PAR-02-002", "Required parameter missing");
        registerErrorCode("PAR-02-003", "Parameter value out of range");

        // Business logic errors
        registerErrorCode("BUS-03-001", "Business logic error");
        registerErrorCode("BUS-03-002", "Status does not allow this operation");
        registerErrorCode("BUS-03-003", "Insufficient resources");

        // Resource not found errors
        registerErrorCode("NOT-04-001", "Resource not found");
        registerErrorCode("NOT-04-002", "Record not found");

        // Authentication authorization errors
        registerErrorCode("AUTH-05-001", "Unauthorized access");
        registerErrorCode("AUTH-05-002", "Insufficient permissions");
        registerErrorCode("AUTH-05-003", "Token expired");
    }

    /**
     * Register error code.
     *
     * @param errorCode Error code
     * @param description Error description
     * @throws IllegalArgumentException If error code format is invalid
     */
    public void registerErrorCode(final String errorCode, final String description) {
        if (!validateErrorCodeFormat(errorCode)) {
            throw new IllegalArgumentException(
                    "Invalid error code format. "
                            + "Expected format: [ModulePrefix]-[ErrorType]-[Sequence] "
                            + "(e.g., INV-03-001)");
        }
        if (errorCodeMap.containsKey(errorCode)) {
            throw new IllegalArgumentException(
                    "Error code already exists: " + errorCode);
        }
        errorCodeMap.put(errorCode, description);
    }

    /**
     * Validate error code format.
     *
     * @param errorCode Error code
     * @return true if error code is valid, false otherwise
     */
    public boolean validateErrorCodeFormat(final String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return false;
        }
        return ERROR_CODE_PATTERN.matcher(errorCode).matches();
    }

    /**
     * Get error code description.
     *
     * @param errorCode Error code
     * @return error code description, returns null if error code does not exist
     */
    public String getErrorCodeDescription(final String errorCode) {
        return errorCodeMap.get(errorCode);
    }

    /**
     * Check if error code exists.
     *
     * @param errorCode Error code
     * @return true if error code exists, false otherwise
     */
    public boolean containsErrorCode(final String errorCode) {
        return errorCodeMap.containsKey(errorCode);
    }

    /**
     * Get all error codes.
     *
     * @return Map containing all error codes, key is error code, value is error code description
     */
    public Map<String, String> getAllErrorCodes() {
        return new ConcurrentHashMap<>(errorCodeMap);
    }

    /**
     * Generate error code (delegated to ErrorCodeGenerator).
     *
     * @param modulePrefix Module prefix
     * @param errorType Error type
     * @param description Error description
     * @return Generated error code
     */
    public String generateErrorCode(final String modulePrefix, final int errorType,
            final String description) {
        final String errorCode = ErrorCodeGenerator.generateErrorCode(modulePrefix, errorType);
        registerErrorCode(errorCode, description);
        return errorCode;
    }
}
