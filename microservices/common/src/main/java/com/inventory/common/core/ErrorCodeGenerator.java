package com.inventory.common.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Error code generator for automatically generating standardized error codes.
 * Error code format: [ModulePrefix]-[ErrorType]-[Sequence], where module prefix.
 * does not exceed 4 characters, error type is 2 digits, and sequence is 3 digits.
 */
public final class ErrorCodeGenerator {

    /** 存储每个模块+错误类型的最大序列号. */
    private static final Map<String, AtomicInteger> ERROR_CODE_COUNTERS = new ConcurrentHashMap<>();

    /** 最小错误类型值. */
    private static final int MIN_ERROR_TYPE = 1;

    /** 最大错误类型值. */
    private static final int MAX_ERROR_TYPE = 99;

    /** 最大序列值. */
    private static final int MAX_SEQUENCE = 999;

    /** 最小序列值. */
    private static final int MIN_SEQUENCE = 1;

    /** 最大模块前缀长度. */
    private static final int MAX_MODULE_PREFIX_LENGTH = 4;

    /** 错误类型格式位数. */
    private static final int ERROR_TYPE_DIGITS = 2;

    /** 序列格式位数. */
    private static final int SEQUENCE_DIGITS = CommonConstants.MAX_RETRY_ATTEMPTS;

    /**
     * 私有构造函数，防止实例化.
     */
    private ErrorCodeGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generate error code.
     *
     * @param modulePrefix Module prefix (1-4 uppercase letters)
     * @param errorType Error type (1-99 number)
     * @return Generated error code
     */
    public static String generateErrorCode(final String modulePrefix, final int errorType) {
        // Validate module prefix
        if (modulePrefix == null || modulePrefix.isEmpty()
                || modulePrefix.length() > MAX_MODULE_PREFIX_LENGTH
                || !modulePrefix.matches("^[A-Z]+$")) {
            throw new IllegalArgumentException(
                    "Invalid module prefix. Must be 1-4 uppercase letters.");
        }

        // Validate error type
        if (errorType < MIN_ERROR_TYPE || errorType > MAX_ERROR_TYPE) {
            throw new IllegalArgumentException(
                    "Invalid error type. Must be between 1 and 99.");
        }

        // Build counter key
        final String counterKey = modulePrefix + "-"
                + String.format("%0" + ERROR_TYPE_DIGITS + "d", errorType);

        // Get or create counter
        final AtomicInteger counter = ERROR_CODE_COUNTERS.computeIfAbsent(
                counterKey, k -> new AtomicInteger(0));

        // Generate sequence (001-999)
        final int sequence = counter.incrementAndGet();
        if (sequence > MAX_SEQUENCE) {
            throw new IllegalArgumentException(
                    "Error code sequence exceeded maximum value (999) for "
                            + counterKey);
        }

        // Build error code
        return String.format("%s-%0" + ERROR_TYPE_DIGITS + "d-%0" + SEQUENCE_DIGITS + "d",
                modulePrefix, errorType, sequence);
    }

    /**
     * Generate error code (overloaded method, supports specifying start sequence).
     *
     * @param modulePrefix Module prefix
     * @param errorType Error type
     * @param startSequence Start sequence
     * @return Generated error code
     */
    public static String generateErrorCode(final String modulePrefix, final int errorType,
            final int startSequence) {
        // Validate start sequence
        if (startSequence < MIN_SEQUENCE || startSequence > MAX_SEQUENCE) {
            throw new IllegalArgumentException(
                    "Invalid start sequence. Must be between 1 and 999.");
        }

        // Validate module prefix and error type
        if (modulePrefix == null || modulePrefix.isEmpty()
                || modulePrefix.length() > MAX_MODULE_PREFIX_LENGTH
                || !modulePrefix.matches("^[A-Z]+$")) {
            throw new IllegalArgumentException(
                    "Invalid module prefix. Must be 1-4 uppercase letters.");
        }

        if (errorType < MIN_ERROR_TYPE || errorType > MAX_ERROR_TYPE) {
            throw new IllegalArgumentException(
                    "Invalid error type. Must be between 1 and 99.");
        }

        // Build counter key
        final String counterKey = modulePrefix + "-"
                + String.format("%0" + ERROR_TYPE_DIGITS + "d", errorType);

        // Create counter and set initial value
        AtomicInteger counter = new AtomicInteger(startSequence - 1);
        final AtomicInteger existingCounter = ERROR_CODE_COUNTERS.putIfAbsent(
                counterKey, counter);

        if (existingCounter != null) {
            // If counter already exists, use existing counter and ensure start sequence
            // is not less than existing value
            existingCounter.updateAndGet(current -> Math.max(current, startSequence - 1));
            counter = existingCounter;
        }

        // Generate sequence
        final int sequence = counter.incrementAndGet();
        return String.format("%s-%0" + ERROR_TYPE_DIGITS + "d-%0" + SEQUENCE_DIGITS + "d",
                modulePrefix, errorType, sequence);
    }

    /**
     * Reset counter for specified module and error type.
     *
     * @param modulePrefix Module prefix
     * @param errorType Error type
     */
    public static void resetCounter(final String modulePrefix, final int errorType) {
        final String counterKey = modulePrefix + "-"
                + String.format("%0" + ERROR_TYPE_DIGITS + "d", errorType);
        ERROR_CODE_COUNTERS.remove(counterKey);
    }

    /**
     * Reset all counters.
     */
    public static void resetAllCounters() {
        ERROR_CODE_COUNTERS.clear();
    }

    /**
     * Get current counter value.
     *
     * @param modulePrefix Module prefix
     * @param errorType Error type
     * @return Current counter value, returns 0 if not exists
     */
    public static int getCurrentCounter(final String modulePrefix, final int errorType) {
        final String counterKey = modulePrefix + "-"
                + String.format("%0" + ERROR_TYPE_DIGITS + "d", errorType);
        final AtomicInteger counter = ERROR_CODE_COUNTERS.get(counterKey);
        return counter != null ? counter.get() : 0;
    }
}
