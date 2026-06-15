package com.inventory.common.core;

public final class MessageConstants {

    public static final String USER_NOT_FOUND = "User not found";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String PAYMENT_NOT_FOUND = "Payment not found";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ACCOUNT_DISABLED = "Account is disabled";
    public static final String ACCOUNT_LOCKED = "Account is locked";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_INVALID = "Invalid token";
    public static final String PERMISSION_DENIED = "Permission denied";
    public static final String OPERATION_SUCCESS = "Operation successful";
    public static final String OPERATION_FAILED = "Operation failed";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String DUPLICATE_ENTRY = "Duplicate entry already exists";

    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String TOKEN_REFRESHED = "Token refreshed successfully";
    public static final String PASSWORD_CHANGED = "Password changed successfully";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String INVALID_MFA_CODE = "Invalid MFA code";
    public static final String PASSWORD_MISMATCH = "New password and confirm password do not match";
    public static final String EMAIL_SENT = "Reset code sent to email";

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RECOVERY_CODE = "recoveryCode";
    public static final String KEY_SECRET = "secret";
    public static final String KEY_QR_CODE_URL = "qrCodeUrl";
    public static final String KEY_MFA_ENABLED = "mfaEnabled";
    public static final String KEY_HAS_RECOVERY_CODE = "hasRecoveryCode";

    private MessageConstants() {
    }
}
