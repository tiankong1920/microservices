package com.inventory.common.template.constant;

public final class TemplateConstants {

    private TemplateConstants() { }

    public static final String TEMPLATE_CODE_PREFIX = "TPL";

    public static final String FIELD_CODE_PREFIX = "FLD";

    public static final String CUSTOM_FIELD_PREFIX = "CF";

    public static final String VERSION_PREFIX = "V";

    public static final int MAX_TEMPLATE_NAME_LENGTH = 128;

    public static final int MAX_FIELD_NAME_LENGTH = 64;

    public static final int MAX_FIELD_LABEL_LENGTH = 255;

    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    public static final int MAX_TEXT_LENGTH = 255;

    public static final int MAX_TEXTAREA_LENGTH = 4000;

    public static final int MAX_CUSTOM_FIELDS_PER_TEMPLATE = 50;

    public static final int MAX_FIELDS_PER_TEMPLATE = 100;

    public static final int MAX_OPTIONS_PER_FIELD = 100;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    public static final int VERSION_HISTORY_RETENTION_DAYS = 365;

    public static final int AUDIT_LOG_RETENTION_DAYS = 365;

    public static final String IMPORT_FORMAT_JSON = "json";

    public static final String IMPORT_FORMAT_EXCEL = "excel";

    public static final String CHANGE_TYPE_MAJOR = "major";

    public static final String CHANGE_TYPE_MINOR = "minor";

    public static final String CHANGE_TYPE_PATCH = "patch";

    public static final String[] BUILTIN_VALIDATION_RULES = {
        "EMAIL",
        "PHONE",
        "URL",
        "ID_CARD",
        "POSTAL_CODE",
        "IP_ADDRESS",
        "DATE",
        "TIME",
        "DATETIME",
        "NUMBER"
    };

    public static final String SENSITIVE_FIELD_MARKER = "***";

    public static final String DEFAULT_TENANT_ID = "default";

    public static final String SYSTEM_USER = "system";

    public static final int MAX_VERSION_ROLLBACK_DAYS = 30;

    public static final int MAX_IMPORT_BATCH_SIZE = 100;

    public static final int EXPORT_TIMEOUT_SECONDS = 60;

    public static final int VALIDATION_TIMEOUT_MS = 300;

    public static final int SYNC_TIMEOUT_MS = 500;
}
