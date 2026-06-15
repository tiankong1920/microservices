package com.inventory.templateservice.security;

public final class PermissionConstants {

    private PermissionConstants() { }

    public static final String TEMPLATE_VIEW = "TEMPLATE_VIEW";
    public static final String TEMPLATE_CREATE = "TEMPLATE_CREATE";
    public static final String TEMPLATE_EDIT = "TEMPLATE_EDIT";
    public static final String TEMPLATE_DELETE = "TEMPLATE_DELETE";
    public static final String TEMPLATE_PUBLISH = "TEMPLATE_PUBLISH";
    public static final String TEMPLATE_APPROVE = "TEMPLATE_APPROVE";
    public static final String TEMPLATE_ARCHIVE = "TEMPLATE_ARCHIVE";
    public static final String TEMPLATE_ROLLBACK = "TEMPLATE_ROLLBACK";
    public static final String TEMPLATE_MANAGE_FIELDS = "TEMPLATE_MANAGE_FIELDS";
    public static final String TEMPLATE_MANAGE_CUSTOM_FIELDS = "TEMPLATE_MANAGE_CUSTOM_FIELDS";
    public static final String TEMPLATE_EXPORT = "TEMPLATE_EXPORT";
    public static final String TEMPLATE_IMPORT = "TEMPLATE_IMPORT";
    public static final String TEMPLATE_MANAGE_VERSIONS = "TEMPLATE_MANAGE_VERSIONS";
    public static final String TEMPLATE_VIEW_AUDIT_LOGS = "TEMPLATE_VIEW_AUDIT_LOGS";

    public static final String ROLE_TEMPLATE_ADMIN = "TEMPLATE_ADMIN";
    public static final String ROLE_TEMPLATE_EDITOR = "TEMPLATE_EDITOR";
    public static final String ROLE_TEMPLATE_VIEWER = "TEMPLATE_VIEWER";
    public static final String ROLE_TEMPLATE_APPROVER = "TEMPLATE_APPROVER";
    public static final String ROLE_TEMPLATE_AUDITOR = "TEMPLATE_AUDITOR";

    public static final String[] ALL_PERMISSIONS = {
        TEMPLATE_VIEW, TEMPLATE_CREATE, TEMPLATE_EDIT, TEMPLATE_DELETE,
        TEMPLATE_PUBLISH, TEMPLATE_APPROVE, TEMPLATE_ARCHIVE, TEMPLATE_ROLLBACK,
        TEMPLATE_MANAGE_FIELDS, TEMPLATE_MANAGE_CUSTOM_FIELDS, TEMPLATE_EXPORT,
        TEMPLATE_IMPORT, TEMPLATE_MANAGE_VERSIONS, TEMPLATE_VIEW_AUDIT_LOGS
    };
}
