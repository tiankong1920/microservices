package com.inventory.common.template.exception;

import com.inventory.common.core.BaseApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class TemplateException extends BaseApplicationException {

    public TemplateException(String errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }

    public TemplateException(String errorCode, String message, Map<String, Object> context) {
        super(errorCode, HttpStatus.BAD_REQUEST, message, context);
    }

    public TemplateException(String errorCode, HttpStatus status, String message) {
        super(errorCode, status, message);
    }

    public static TemplateException notFound(Long templateId) {
        return new TemplateException("TPL-001", HttpStatus.NOT_FOUND, 
            "Template not found with id: " + templateId);
    }

    public static TemplateException notFoundByCode(String templateCode) {
        return new TemplateException("TPL-002", HttpStatus.NOT_FOUND, 
            "Template not found with code: " + templateCode);
    }

    public static TemplateException alreadyExists(String templateCode) {
        return new TemplateException("TPL-003", HttpStatus.CONFLICT, 
            "Template already exists with code: " + templateCode);
    }

    public static TemplateException invalidStatus(String status) {
        return new TemplateException("TPL-004", HttpStatus.BAD_REQUEST, 
            "Invalid template status: " + status);
    }

    public static TemplateException cannotModify(Long templateId, String reason) {
        return new TemplateException("TPL-005", HttpStatus.FORBIDDEN, 
            "Cannot modify template " + templateId + ": " + reason);
    }

    public static TemplateException validationFailed(String field, String message) {
        return new TemplateException("TPL-006", HttpStatus.BAD_REQUEST, 
            "Validation failed for field '" + field + "': " + message);
    }

    public static TemplateException fieldNotFound(Long fieldId) {
        return new TemplateException("TPL-007", HttpStatus.NOT_FOUND, 
            "Template field not found with id: " + fieldId);
    }

    public static TemplateException versionConflict(Long templateId, String version) {
        return new TemplateException("TPL-008", HttpStatus.CONFLICT, 
            "Version conflict for template " + templateId + " at version " + version);
    }

    public static TemplateException importFailed(String message) {
        return new TemplateException("TPL-009", HttpStatus.BAD_REQUEST, 
            "Template import failed: " + message);
    }

    public static TemplateException exportFailed(String message) {
        return new TemplateException("TPL-010", HttpStatus.INTERNAL_SERVER_ERROR, 
            "Template export failed: " + message);
    }

    public static TemplateException customFieldLimitExceeded(Long templateId, int limit) {
        return new TemplateException("TPL-011", HttpStatus.BAD_REQUEST, 
            "Custom field limit exceeded for template " + templateId + ", max: " + limit);
    }

    public static TemplateException invalidFieldType(String fieldType) {
        return new TemplateException("TPL-012", HttpStatus.BAD_REQUEST, 
            "Invalid field type: " + fieldType);
    }

    public static TemplateException permissionDenied(String operation, Long templateId) {
        return new TemplateException("TPL-013", HttpStatus.FORBIDDEN, 
            "Permission denied for operation '" + operation + "' on template " + templateId);
    }

    public static TemplateException versionNotFound(Long versionId) {
        return new TemplateException("TPL-014", HttpStatus.NOT_FOUND, 
            "Template version not found with id: " + versionId);
    }

    public static TemplateException cannotRollback(Long templateId, Long versionId, String reason) {
        return new TemplateException("TPL-015", HttpStatus.BAD_REQUEST, 
            "Cannot rollback template " + templateId + " to version " + versionId + ": " + reason);
    }

    public static TemplateException validationRuleNotFound(Long ruleId) {
        return new TemplateException("TPL-016", HttpStatus.NOT_FOUND,
            "Validation rule not found with id: " + ruleId);
    }

    public static TemplateException restoreFailed(String reason) {
        return new TemplateException("TPL-017", HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to restore template from snapshot: " + reason);
    }
}
