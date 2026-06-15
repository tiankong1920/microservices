-- 模板系统数据库表结构
-- Template System Database Schema

-- 模板主表
CREATE TABLE IF NOT EXISTS template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(128) NOT NULL,
    description VARCHAR(1000),
    business_domain VARCHAR(32) NOT NULL,
    category VARCHAR(64),
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    version VARCHAR(16) DEFAULT '1.0.0',
    parent_version_id BIGINT,
    default_values JSONB,
    applicable_scenarios TEXT[],
    min_config_items INTEGER DEFAULT 0,
    max_config_items INTEGER,
    allow_custom_fields BOOLEAN DEFAULT false,
    allow_extension BOOLEAN DEFAULT false,
    extension_point VARCHAR(255),
    metadata JSONB,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by VARCHAR(64),
    approved_at TIMESTAMP,
    published_at TIMESTAMP,
    deprecated_at TIMESTAMP,
    change_log TEXT,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    CONSTRAINT uk_template_code UNIQUE (template_code, tenant_id)
);

CREATE INDEX idx_template_tenant ON template(tenant_id);
CREATE INDEX idx_template_domain ON template(business_domain);
CREATE INDEX idx_template_status ON template(status);
CREATE INDEX idx_template_category ON template(category);

-- 模板字段表
CREATE TABLE IF NOT EXISTS template_field (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES template(id) ON DELETE CASCADE,
    field_code VARCHAR(64) NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    field_label VARCHAR(255) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    description VARCHAR(500),
    placeholder VARCHAR(255),
    default_value TEXT,
    required BOOLEAN DEFAULT false,
    unique_field BOOLEAN DEFAULT false,
    searchable BOOLEAN DEFAULT false,
    sortable BOOLEAN DEFAULT false,
    display_order INTEGER DEFAULT 0,
    validation_regex VARCHAR(255),
    min_length INTEGER,
    max_length INTEGER,
    min_value INTEGER,
    max_value INTEGER,
    precision_val INTEGER,
    scale_val INTEGER,
    options TEXT[],
    extra_config JSONB,
    default_permission VARCHAR(16) DEFAULT 'EDITABLE',
    role_permissions JSONB,
    sensitive BOOLEAN DEFAULT false,
    group_id VARCHAR(64),
    dependency_field VARCHAR(64),
    dependency_value VARCHAR(255),
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_field_code UNIQUE (template_id, field_code)
);

CREATE INDEX idx_field_template ON template_field(template_id);
CREATE INDEX idx_field_order ON template_field(template_id, display_order);

-- 模板版本表
CREATE TABLE IF NOT EXISTS template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES template(id) ON DELETE CASCADE,
    version_number VARCHAR(16) NOT NULL,
    major_version INTEGER NOT NULL DEFAULT 1,
    minor_version INTEGER NOT NULL DEFAULT 0,
    patch_version INTEGER NOT NULL DEFAULT 0,
    change_description TEXT,
    change_type VARCHAR(16) NOT NULL,
    changed_fields TEXT[],
    snapshot JSONB NOT NULL,
    changed_by VARCHAR(64),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT,
    is_rollback BOOLEAN DEFAULT false,
    rollback_from_version_id BIGINT,
    CONSTRAINT uk_version_number UNIQUE (template_id, version_number)
);

CREATE INDEX idx_version_template ON template_version(template_id);
CREATE INDEX idx_version_changed_at ON template_version(template_id, changed_at DESC);

-- 自定义字段表
CREATE TABLE IF NOT EXISTS custom_field (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES template(id) ON DELETE CASCADE,
    field_code VARCHAR(64) NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    field_label VARCHAR(255) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    description VARCHAR(500),
    default_value TEXT,
    required BOOLEAN DEFAULT false,
    validation_regex VARCHAR(255),
    min_length INTEGER,
    max_length INTEGER,
    min_value INTEGER,
    max_value INTEGER,
    options TEXT[],
    role_permissions JSONB,
    active BOOLEAN DEFAULT true,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_custom_field_code UNIQUE (template_id, field_code)
);

CREATE INDEX idx_custom_field_template ON custom_field(template_id);
CREATE INDEX idx_custom_field_active ON custom_field(template_id, active);

-- 校验规则表
CREATE TABLE IF NOT EXISTS validation_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    rule_name VARCHAR(128) NOT NULL,
    rule_type VARCHAR(32) NOT NULL,
    regex_pattern VARCHAR(500),
    error_message VARCHAR(255),
    parameters JSONB,
    applicable_field_types TEXT[],
    built_in BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_validation_rule_code ON validation_rule(rule_code);
CREATE INDEX idx_validation_rule_active ON validation_rule(active);

-- 模板字段组表
CREATE TABLE IF NOT EXISTS template_field_group (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES template(id) ON DELETE CASCADE,
    group_id VARCHAR(64) NOT NULL,
    group_name VARCHAR(64) NOT NULL,
    group_label VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    collapsible BOOLEAN DEFAULT true,
    default_collapsed BOOLEAN DEFAULT false,
    field_codes TEXT[],
    CONSTRAINT uk_group_id UNIQUE (template_id, group_id)
);

CREATE INDEX idx_field_group_template ON template_field_group(template_id);

-- 模板操作审计日志表
CREATE TABLE IF NOT EXISTS template_audit_log (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    operation VARCHAR(32) NOT NULL,
    operation_detail TEXT,
    old_value JSONB,
    new_value JSONB,
    operator VARCHAR(64) NOT NULL,
    operator_ip VARCHAR(64),
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default'
);

CREATE INDEX idx_audit_template ON template_audit_log(template_id);
CREATE INDEX idx_audit_time ON template_audit_log(operation_time DESC);
CREATE INDEX idx_audit_tenant ON template_audit_log(tenant_id);

-- 插入内置校验规则
INSERT INTO validation_rule (rule_code, rule_name, rule_type, regex_pattern, error_message, applicable_field_types, built_in, active) VALUES
('EMAIL', '邮箱格式', 'REGEX', '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', '请输入有效的邮箱地址', ARRAY['TEXT'], true, true),
('PHONE', '手机号格式', 'REGEX', '^1[3-9]\d{9}$', '请输入有效的手机号码', ARRAY['TEXT', 'PHONE'], true, true),
('URL', 'URL格式', 'REGEX', '^https?://[\w\-]+(\.[\w\-]+)+[/#?]?.*$', '请输入有效的URL地址', ARRAY['TEXT', 'URL'], true, true),
('ID_CARD', '身份证号格式', 'REGEX', '^\d{17}[\dXx]$', '请输入有效的身份证号码', ARRAY['TEXT'], true, true),
('POSTAL_CODE', '邮政编码', 'REGEX', '^\d{6}$', '请输入有效的邮政编码', ARRAY['TEXT'], true, true),
('IP_ADDRESS', 'IP地址格式', 'REGEX', '^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$', '请输入有效的IP地址', ARRAY['TEXT'], true, true),
('DATE', '日期格式', 'REGEX', '^\d{4}-\d{2}-\d{2}$', '请输入有效的日期(YYYY-MM-DD)', ARRAY['TEXT', 'DATE'], true, true),
('TIME', '时间格式', 'REGEX', '^\d{2}:\d{2}(:\d{2})?$', '请输入有效的时间(HH:mm:ss)', ARRAY['TEXT', 'TIME'], true, true),
('DATETIME', '日期时间格式', 'REGEX', '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$', '请输入有效的日期时间', ARRAY['TEXT', 'DATETIME'], true, true),
('NUMBER', '数字格式', 'REGEX', '^-?\d+(\.\d+)?$', '请输入有效的数字', ARRAY['TEXT', 'NUMBER'], true, true)
ON CONFLICT (rule_code) DO NOTHING;
