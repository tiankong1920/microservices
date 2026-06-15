package com.inventory.common.security;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 安全日志记录器.
 *
 * <p>用于统一记录安全相关的事件，如认证、授权、密码修改等。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public final class SecurityLogger {

    /** 安全日志记录器. */
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("com.inventory.security");

    /** 未知IP标识. */
    private static final String UNKNOWN = "unknown";

    /** X-Forwarded-For 头. */
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /** Proxy-Client-IP 头. */
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";

    /** WL-Proxy-Client-IP 头. */
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

    /** IP分隔符. */
    private static final String IP_SEPARATOR = ",";

    /** 默认IP地址. */
    private static final String DEFAULT_IP = "N/A";

    /** 事件类型键. */
    private static final String KEY_EVENT_TYPE = "eventType";

    /** 错误消息键. */
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    /** 资源键. */
    private static final String KEY_RESOURCE = "resource";

    /** 用户名键. */
    private static final String KEY_USERNAME = "username";

    /** IP地址键. */
    private static final String KEY_IP_ADDRESS = "ipAddress";

    /** 安全事件日志格式. */
    private static final String SECURITY_EVENT_FORMAT = "[Security Event] {}: {}";

    /** 安全事件前缀. */
    private static final String SECURITY_EVENT_PREFIX = "[Security Event] ";

    private SecurityLogger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void logAuthenticationSuccess(final String username, final String ipAddress,
            final String userAgent) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put("userAgent", userAgent);
        eventData.put(KEY_EVENT_TYPE, "AUTHENTICATION_SUCCESS");

        SECURITY_LOGGER.info(SECURITY_EVENT_PREFIX + "Authentication successful for user: {}", username,
                eventData);
    }

    public static void logAuthenticationFailure(final String username, final String ipAddress,
            final String userAgent, final String errorMessage) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put("userAgent", userAgent);
        eventData.put(KEY_ERROR_MESSAGE, errorMessage);
        eventData.put(KEY_EVENT_TYPE, "AUTHENTICATION_FAILURE");

        SECURITY_LOGGER.warn(SECURITY_EVENT_PREFIX + "Authentication failed for user: {} - Reason: {}",
                username, errorMessage, eventData);
    }

    public static void logMFASuccess(final String username, final String ipAddress) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put(KEY_EVENT_TYPE, "MFA_SUCCESS");

        SECURITY_LOGGER.info(SECURITY_EVENT_PREFIX + "MFA verification successful for user: {}", username,
                eventData);
    }

    public static void logMFADailure(final String username, final String ipAddress,
            final String errorMessage) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put(KEY_ERROR_MESSAGE, errorMessage);
        eventData.put(KEY_EVENT_TYPE, "MFA_FAILURE");

        SECURITY_LOGGER.warn(SECURITY_EVENT_PREFIX + "MFA verification failed for user: {} - Reason: {}",
                username, errorMessage, eventData);
    }

    public static void logAuthorizationSuccess(final String username, final String ipAddress,
            final String resource, final String action) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put(KEY_RESOURCE, resource);
        eventData.put("action", action);
        eventData.put(KEY_EVENT_TYPE, "AUTHORIZATION_SUCCESS");

        SECURITY_LOGGER.info(
                SECURITY_EVENT_PREFIX + "Authorization successful for user: {} on resource: {} with action: {}",
                username, resource, action, eventData);
    }

    public static void logAuthorizationFailure(final String username, final String ipAddress,
            final String resource, final String action, final String errorMessage) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put(KEY_RESOURCE, resource);
        eventData.put("action", action);
        eventData.put(KEY_ERROR_MESSAGE, errorMessage);
        eventData.put(KEY_EVENT_TYPE, "AUTHORIZATION_FAILURE");

        SECURITY_LOGGER.warn(
                SECURITY_EVENT_PREFIX + "Authorization failed for user: {} on resource: {} with action: {} - Reason: {}",
                username, resource, action, errorMessage, eventData);
    }

    public static void logPasswordChange(final String username, final String ipAddress) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put(KEY_EVENT_TYPE, "PASSWORD_CHANGE");

        SECURITY_LOGGER.info(SECURITY_EVENT_PREFIX + "Password changed for user: {}", username, eventData);
    }

    public static void logAccountLocked(final String username, final String ipAddress,
            final String reason) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put("reason", reason);
        eventData.put(KEY_EVENT_TYPE, "ACCOUNT_LOCKED");

        SECURITY_LOGGER.warn(SECURITY_EVENT_PREFIX + "Account locked for user: {} - Reason: {}", username,
                reason, eventData);
    }

    public static void logAccountUnlocked(final String username, final String ipAddress,
            final String reason) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put("reason", reason);
        eventData.put(KEY_EVENT_TYPE, "ACCOUNT_UNLOCKED");

        SECURITY_LOGGER.info(SECURITY_EVENT_PREFIX + "Account unlocked for user: {} - Reason: {}", username,
                reason, eventData);
    }

    public static void logSensitiveOperation(final String username, final String ipAddress,
            final String operation, final String resource) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(KEY_USERNAME, username);
        eventData.put(KEY_IP_ADDRESS, ipAddress);
        eventData.put("operation", operation);
        eventData.put(KEY_RESOURCE, resource);
        eventData.put(KEY_EVENT_TYPE, "SENSITIVE_OPERATION");

        SECURITY_LOGGER.info(
                SECURITY_EVENT_PREFIX + "Sensitive operation performed by user: {} - Operation: {} on Resource: {}",
                username, operation, resource, eventData);
    }

    public static String getClientIP(final HttpServletRequest request) {
        if (request == null) {
            return DEFAULT_IP;
        }

        String ipAddress = request.getHeader(X_FORWARDED_FOR);
        if (isInvalidIp(ipAddress)) {
            ipAddress = request.getHeader(PROXY_CLIENT_IP);
        }
        if (isInvalidIp(ipAddress)) {
            ipAddress = request.getHeader(WL_PROXY_CLIENT_IP);
        }
        if (isInvalidIp(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.contains(IP_SEPARATOR)) {
            ipAddress = ipAddress.split(IP_SEPARATOR)[0].trim();
        }

        return ipAddress;
    }

    private static boolean isInvalidIp(final String ipAddress) {
        return ipAddress == null || ipAddress.isEmpty() || UNKNOWN.equalsIgnoreCase(ipAddress);
    }

    public static HttpServletRequest getCurrentRequest() {
        final ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    public static void logSecurityEvent(final String eventType, final String severity,
            final String message, final Map<String, Object> eventData) {
        final String upperSeverity = severity.toUpperCase();
        switch (upperSeverity) {
            case "DEBUG":
                SECURITY_LOGGER.debug(SECURITY_EVENT_FORMAT, eventType, message, eventData);
                break;
            case "INFO":
                SECURITY_LOGGER.info(SECURITY_EVENT_FORMAT, eventType, message, eventData);
                break;
            case "WARN":
                SECURITY_LOGGER.warn(SECURITY_EVENT_FORMAT, eventType, message, eventData);
                break;
            case "ERROR":
                SECURITY_LOGGER.error(SECURITY_EVENT_FORMAT, eventType, message, eventData);
                break;
            default:
                SECURITY_LOGGER.info(SECURITY_EVENT_FORMAT, eventType, message, eventData);
        }
    }
}
