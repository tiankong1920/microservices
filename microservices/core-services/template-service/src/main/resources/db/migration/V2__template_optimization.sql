-- 模板系统索引优化和约束增强
-- Template System Index Optimization and Constraint Enhancement

-- 添加全文搜索索引
CREATE INDEX IF NOT EXISTS idx_template_name_search ON template USING gin(to_tsvector('simple', template_name));
CREATE INDEX IF NOT EXISTS idx_template_description_search ON template USING gin(to_tsvector('simple', description));

-- 添加复合索引优化常用查询
CREATE INDEX IF NOT EXISTS idx_template_tenant_status ON template(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_template_tenant_domain ON template(tenant_id, business_domain);
CREATE INDEX IF NOT EXISTS idx_template_tenant_status_domain ON template(tenant_id, status, business_domain);

-- 优化版本查询
CREATE INDEX IF NOT EXISTS idx_version_template_major_minor ON template_version(template_id, major_version DESC, minor_version DESC, patch_version DESC);

-- 添加模板编码前缀索引
CREATE INDEX IF NOT EXISTS idx_template_code_prefix ON template(template_code varchar_pattern_ops);

-- 添加审计日志操作类型索引
CREATE INDEX IF NOT EXISTS idx_audit_operation ON template_audit_log(operation);
CREATE INDEX IF NOT EXISTS idx_audit_operator ON template_audit_log(operator);
CREATE INDEX IF NOT EXISTS idx_audit_template_time ON template_audit_log(template_id, operation_time DESC);

-- 添加字段组显示顺序索引
CREATE INDEX IF NOT EXISTS idx_field_group_order ON template_field_group(template_id, display_order);

-- 创建更新时间自动更新触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为模板表添加触发器
DROP TRIGGER IF EXISTS update_template_updated_at ON template;
CREATE TRIGGER update_template_updated_at
    BEFORE UPDATE ON template
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 为模板字段表添加触发器
DROP TRIGGER IF EXISTS update_template_field_updated_at ON template_field;
CREATE TRIGGER update_template_field_updated_at
    BEFORE UPDATE ON template_field
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 为自定义字段表添加触发器
DROP TRIGGER IF EXISTS update_custom_field_updated_at ON custom_field;
CREATE TRIGGER update_custom_field_updated_at
    BEFORE UPDATE ON custom_field
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 创建模板统计视图
CREATE OR REPLACE VIEW v_template_statistics AS
SELECT 
    t.tenant_id,
    t.business_domain,
    t.status,
    COUNT(*) as template_count,
    COUNT(tf.id) as total_fields,
    COUNT(cf.id) as custom_field_count,
    COUNT(tv.id) as version_count
FROM template t
LEFT JOIN template_field tf ON t.id = tf.template_id
LEFT JOIN custom_field cf ON t.id = cf.template_id
LEFT JOIN template_version tv ON t.id = tv.template_id
GROUP BY t.tenant_id, t.business_domain, t.status;

-- 创建审计日志统计视图
CREATE OR REPLACE VIEW v_audit_statistics AS
SELECT 
    tenant_id,
    operation,
    DATE(operation_time) as operation_date,
    COUNT(*) as operation_count,
    COUNT(DISTINCT operator) as unique_operators,
    COUNT(DISTINCT template_id) as affected_templates
FROM template_audit_log
GROUP BY tenant_id, operation, DATE(operation_time);

-- 添加模板状态检查约束
ALTER TABLE template DROP CONSTRAINT IF EXISTS chk_template_status;
ALTER TABLE template ADD CONSTRAINT chk_template_status 
    CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'DEPRECATED', 'ARCHIVED'));

-- 添加版本变更类型检查约束
ALTER TABLE template_version DROP CONSTRAINT IF EXISTS chk_change_type;
ALTER TABLE template_version ADD CONSTRAINT chk_change_type 
    CHECK (change_type IN ('MAJOR', 'MINOR', 'PATCH'));

-- 添加字段类型检查约束
ALTER TABLE template_field DROP CONSTRAINT IF EXISTS chk_field_type;
ALTER TABLE template_field ADD CONSTRAINT chk_field_type 
    CHECK (field_type IN ('TEXT', 'NUMBER', 'DATE', 'DATETIME', 'SELECT', 'MULTI_SELECT', 
                           'CHECKBOX', 'RADIO', 'TEXTAREA', 'FILE', 'IMAGE', 'URL', 
                           'EMAIL', 'PHONE', 'CURRENCY', 'PERCENTAGE'));

-- 添加权限类型检查约束
ALTER TABLE template_field DROP CONSTRAINT IF EXISTS chk_default_permission;
ALTER TABLE template_field ADD CONSTRAINT chk_default_permission 
    CHECK (default_permission IN ('READONLY', 'EDITABLE', 'HIDDEN'));

-- 创建模板搜索函数
CREATE OR REPLACE FUNCTION search_templates(
    p_tenant_id VARCHAR,
    p_keyword VARCHAR DEFAULT NULL,
    p_domain VARCHAR DEFAULT NULL,
    p_status VARCHAR DEFAULT NULL,
    p_limit INTEGER DEFAULT 20,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    id BIGINT,
    template_code VARCHAR,
    template_name VARCHAR,
    description VARCHAR,
    business_domain VARCHAR,
    status VARCHAR,
    version VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id,
        t.template_code,
        t.template_name,
        t.description,
        t.business_domain,
        t.status,
        t.version
    FROM template t
    WHERE t.tenant_id = p_tenant_id
      AND (p_keyword IS NULL OR 
           t.template_name ILIKE '%' || p_keyword || '%' OR
           t.template_code ILIKE '%' || p_keyword || '%' OR
           t.description ILIKE '%' || p_keyword || '%')
      AND (p_domain IS NULL OR t.business_domain = p_domain)
      AND (p_status IS NULL OR t.status = p_status)
    ORDER BY t.updated_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql;

-- 创建版本兼容性检查函数
CREATE OR REPLACE FUNCTION check_version_compatibility(
    p_template_id BIGINT,
    p_old_version VARCHAR,
    p_new_version VARCHAR
)
RETURNS BOOLEAN AS $$
DECLARE
    v_old_major INTEGER;
    v_old_minor INTEGER;
    v_old_patch INTEGER;
    v_new_major INTEGER;
    v_new_minor INTEGER;
    v_new_patch INTEGER;
BEGIN
    SELECT major_version, minor_version, patch_version INTO v_old_major, v_old_minor, v_old_patch
    FROM template_version WHERE template_id = p_template_id AND version_number = p_old_version;
    
    SELECT major_version, minor_version, patch_version INTO v_new_major, v_new_minor, v_new_patch
    FROM template_version WHERE template_id = p_template_id AND version_number = p_new_version;
    
    IF v_new_major > v_old_major THEN
        RETURN false;
    END IF;
    
    IF v_new_major = v_old_major AND v_new_minor > v_old_minor THEN
        RETURN false;
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- 插入更多内置校验规则
INSERT INTO validation_rule (rule_code, rule_name, rule_type, regex_pattern, error_message, applicable_field_types, built_in, active) VALUES
('POSITIVE_INTEGER', '正整数', 'REGEX', '^[1-9]\d*$', '请输入正整数', ARRAY['TEXT', 'NUMBER'], true, true),
('NON_NEGATIVE', '非负数', 'REGEX', '^\d+(\.\d+)?$', '请输入非负数', ARRAY['TEXT', 'NUMBER'], true, true),
('CHINESE_NAME', '中文姓名', 'REGEX', '^[\u4e00-\u9fa5]{2,20}$', '请输入有效的中文姓名', ARRAY['TEXT'], true, true),
('BANK_CARD', '银行卡号', 'REGEX', '^\d{16,19}$', '请输入有效的银行卡号', ARRAY['TEXT'], true, true),
('CREDIT_CODE', '统一社会信用代码', 'REGEX', '^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$', '请输入有效的统一社会信用代码', ARRAY['TEXT'], true, true),
('PASSWORD_STRONG', '强密码', 'REGEX', '^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$', '密码必须包含大小写字母、数字和特殊字符，至少8位', ARRAY['TEXT', 'PASSWORD'], true, true),
('USERNAME', '用户名', 'REGEX', '^[a-zA-Z][a-zA-Z0-9_]{2,19}$', '用户名必须以字母开头，3-20位字母数字下划线', ARRAY['TEXT'], true, true)
ON CONFLICT (rule_code) DO NOTHING;
