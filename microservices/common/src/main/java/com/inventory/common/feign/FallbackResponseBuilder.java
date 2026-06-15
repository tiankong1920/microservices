package com.inventory.common.feign;

/**
 * Shared builder for standard fallback error details.
 * This is intentionally lightweight to avoid coupling across service DTOs.
 */
public final class FallbackResponseBuilder {
    private FallbackResponseBuilder() { }

    public static String errorCode() {
        return "BIZ-SERVICE-UNAVAILABLE";
    }

    public static String errorMessage() {
        return "Service temporarily unavailable";
    }
}
